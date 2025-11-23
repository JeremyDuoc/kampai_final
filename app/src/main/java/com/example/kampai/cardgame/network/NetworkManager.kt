package com.example.kampai.cardgame.network

import android.util.Log
import com.example.kampai.cardgame.domain.models.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.*
import java.net.*
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    private val gson: Gson
) {
    companion object {
        private const val DEFAULT_PORT = 8888
        private const val DISCOVERY_PORT = 8887
        private const val TAG = "NetworkManager"
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _receivedMessages = MutableSharedFlow<NetworkMessage>()
    val receivedMessages: SharedFlow<NetworkMessage> = _receivedMessages.asSharedFlow()

    private var serverSocket: ServerSocket? = null
    private val clientSockets = Collections.synchronizedMap(mutableMapOf<String, Socket>())
    private val outputStreams = Collections.synchronizedMap(mutableMapOf<String, PrintWriter>())

    private var listenJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var myIpAddress: String = ""
        private set

    init {
        myIpAddress = getWifiIpAddress()
    }

    // ==================== HOST FUNCTIONS ====================

    suspend fun startHost(hostInfo: PlayerInfo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            myIpAddress = getWifiIpAddress()
            Log.d(TAG, "Starting Host on IP: $myIpAddress")

            serverSocket = ServerSocket(DEFAULT_PORT)
            _connectionState.value = ConnectionState.Hosting(hostInfo, emptyList())

            startDiscoveryBroadcast(hostInfo)

            listenJob = scope.launch {
                acceptConnections()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting host", e)
            Result.failure(e)
        }
    }

    private suspend fun acceptConnections() {
        try {
            while (currentCoroutineContext().isActive) {
                Log.d(TAG, "Waiting for connections...")
                val socket = serverSocket?.accept() ?: break
                Log.d(TAG, "New connection accepted from: ${socket.inetAddress.hostAddress}")

                scope.launch {
                    handleNewClient(socket)
                }
            }
        } catch (e: Exception) {
            if (currentCoroutineContext().isActive) Log.e(TAG, "Error accepting connections", e)
        }
    }

    private suspend fun handleNewClient(socket: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = PrintWriter(socket.getOutputStream(), true)

            Log.d(TAG, "Reading initial message...")

            // --- LECTURA SIMPLE DE LÍNEA ---
            val initialMessage = input.readLine()

            if (initialMessage == null) {
                Log.e(TAG, "Client disconnected immediately or sent null")
                socket.close()
                return
            }

            Log.d(TAG, "Initial message received: $initialMessage")

            // --- DESERIALIZACIÓN (La causa de la falla anterior) ---
            val connectMessage = gson.fromJson(initialMessage, NetworkMessage.Connect::class.java)

            val playerId = connectMessage.playerInfo.id
            clientSockets[playerId] = socket
            outputStreams[playerId] = output

            updateHostState { currentPlayers ->
                val list = currentPlayers.filter { it.id != connectMessage.playerInfo.id }.toMutableList()
                list.add(connectMessage.playerInfo)
                list
            }

            _receivedMessages.emit(connectMessage)

            // Loop de escucha de mensajes
            while (currentCoroutineContext().isActive) {
                val line = input.readLine() ?: break
                Log.d(TAG, "Message received from $playerId: $line")
                val message = parseMessage(line)
                message?.let { _receivedMessages.emit(it) }
            }
        } catch (e: Exception) {
            // El Host falla al parsear el JSON y cierra la conexión, dando SocketClosed al cliente.
            Log.e(TAG, "Error handling client: connection dropped.", e)
        } finally {
            removeClient(socket)
        }
    }

    private fun startDiscoveryBroadcast(hostInfo: PlayerInfo) {
        scope.launch {
            val socket = DatagramSocket()
            socket.broadcast = true

            val message = "KAMPAI_HOST:${hostInfo.name}:$myIpAddress"
            val buffer = message.toByteArray()

            Log.d(TAG, "Broadcasting discovery: $message")

            while (currentCoroutineContext().isActive) {
                try {
                    val packet = DatagramPacket(
                        buffer,
                        buffer.size,
                        InetAddress.getByName("255.255.255.255"),
                        DISCOVERY_PORT
                    )
                    socket.send(packet)
                    delay(1500)
                } catch (e: Exception) {
                    Log.e(TAG, "Broadcast error", e)
                    delay(2000)
                }
            }
            socket.close()
        }
    }

    // ==================== CLIENT FUNCTIONS ====================

    suspend fun discoverHosts(): Flow<HostDiscovery> = flow {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket(DISCOVERY_PORT)
            socket.soTimeout = 3000

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            Log.d(TAG, "Listening for hosts on port $DISCOVERY_PORT...")

            while (currentCoroutineContext().isActive) {
                try {
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length)
                    val senderIp = packet.address.hostAddress

                    if (senderIp == myIpAddress) continue

                    if (message.startsWith("KAMPAI_HOST:")) {
                        val parts = message.split(":")
                        if (parts.size >= 3) {
                            val hostName = parts[1]
                            val hostIp = if (parts[2].isNotEmpty()) parts[2] else senderIp

                            emit(HostDiscovery(
                                hostName = hostName,
                                hostIp = hostIp!!,
                                port = DEFAULT_PORT
                            ))
                        }
                    }
                } catch (e: SocketTimeoutException) {
                    // Timeout normal
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Discovery error", e)
        } finally {
            socket?.close()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun connectToHost(
        hostIp: String,
        playerInfo: PlayerInfo
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Connecting to host $hostIp:$DEFAULT_PORT")
            val socket = Socket()
            socket.connect(InetSocketAddress(hostIp, DEFAULT_PORT), 5000)

            val output = PrintWriter(socket.getOutputStream(), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

            clientSockets[playerInfo.id] = socket
            outputStreams[playerInfo.id] = output

            val connectMessage = NetworkMessage.Connect(playerInfo)
            val jsonMsg = gson.toJson(connectMessage)
            Log.d(TAG, "Sending connect message: $jsonMsg")
            output.println(jsonMsg)
            output.flush() // Flush explícito

            _connectionState.value = ConnectionState.Connected(hostIp, playerInfo)

            listenJob = scope.launch {
                try {
                    while (currentCoroutineContext().isActive) {
                        val line = input.readLine() ?: break
                        Log.d(TAG, "Client received: $line")
                        val message = parseMessage(line)
                        message?.let { _receivedMessages.emit(it) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Connection lost", e)
                    _connectionState.value = ConnectionState.Disconnected
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect", e)
            Result.failure(e)
        }
    }

    // ==================== MESSAGING & UTILITIES ====================

    suspend fun sendMessage(message: NetworkMessage, targetId: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(message)
                Log.d(TAG, "Sending message: $json to ${targetId ?: "ALL"}")

                if (targetId != null) {
                    outputStreams[targetId]?.println(json)
                } else {
                    val streams = ArrayList(outputStreams.values)
                    streams.forEach { it.println(json) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    suspend fun broadcastGameState(gameState: GameState) {
        sendMessage(NetworkMessage.GameStateSync(gameState))
    }

    suspend fun sendHandToPlayer(playerId: String, hand: PlayerHand) {
        sendMessage(NetworkMessage.HandSync(hand), playerId)
    }

    private fun parseMessage(json: String): NetworkMessage? {
        return try {
            when {
                json.contains("\"Connect\"") -> gson.fromJson(json, NetworkMessage.Connect::class.java)
                json.contains("\"Disconnect\"") -> gson.fromJson(json, NetworkMessage.Disconnect::class.java)
                json.contains("\"GameStateSync\"") -> gson.fromJson(json, NetworkMessage.GameStateSync::class.java)
                json.contains("\"HandSync\"") -> gson.fromJson(json, NetworkMessage.HandSync::class.java)
                json.contains("\"ActionRequest\"") -> gson.fromJson(json, NetworkMessage.ActionRequest::class.java)
                json.contains("\"ActionResult\"") -> gson.fromJson(json, NetworkMessage.ActionResult::class.java)
                json.contains("\"KampaiWindowOpened\"") -> gson.fromJson(json, NetworkMessage.KampaiWindowOpened::class.java)
                json.contains("\"TurnTransition\"") -> gson.fromJson(json, NetworkMessage.TurnTransition::class.java)
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: $json", e)
            null
        }
    }

    private fun removeClient(socket: Socket) {
        var playerIdToRemove: String? = null
        synchronized(clientSockets) {
            for ((id, s) in clientSockets) {
                if (s == socket) {
                    playerIdToRemove = id
                    break
                }
            }
        }

        playerIdToRemove?.let { id ->
            Log.d(TAG, "Removing client: $id")
            clientSockets.remove(id)
            outputStreams.remove(id)

            updateHostState { currentPlayers ->
                currentPlayers.filterNot { player -> player.id == id }
            }

            scope.launch {
                _receivedMessages.emit(NetworkMessage.Disconnect(id))
            }
        }
    }

    private fun updateHostState(update: (List<PlayerInfo>) -> List<PlayerInfo>) {
        val current = _connectionState.value
        if (current is ConnectionState.Hosting) {
            _connectionState.value = current.copy(
                connectedPlayers = update(current.connectedPlayers)
            )
        }
    }

    private fun getWifiIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()

                if (networkInterface.name.contains("wlan") || networkInterface.name.contains("ap")) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            return address.hostAddress ?: ""
                        }
                    }
                }
            }

            val fallbackInterfaces = NetworkInterface.getNetworkInterfaces()
            while (fallbackInterfaces.hasMoreElements()) {
                val networkInterface = fallbackInterfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    fun disconnect() {
        listenJob?.cancel()

        try {
            serverSocket?.close()
        } catch (e: Exception) {}

        clientSockets.values.forEach {
            try { it.close() } catch (e: Exception) {}
        }

        clientSockets.clear()
        outputStreams.clear()

        _connectionState.value = ConnectionState.Disconnected
    }

    fun isHost(): Boolean {
        return _connectionState.value is ConnectionState.Hosting
    }
}

// ==================== CLASES DE ESTADO (NECESARIAS) ====================

sealed class ConnectionState {
    object Disconnected : ConnectionState()

    data class Hosting(
        val hostInfo: PlayerInfo,
        val connectedPlayers: List<PlayerInfo>
    ) : ConnectionState()

    data class Connected(
        val hostIp: String,
        val playerInfo: PlayerInfo
    ) : ConnectionState()
}

data class HostDiscovery(
    val hostName: String,
    val hostIp: String,
    val port: Int
)