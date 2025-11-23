package com.example.kampai.cardgame.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import com.example.kampai.cardgame.domain.models.*
import com.example.kampai.cardgame.network.ConnectionState
import com.example.kampai.cardgame.network.HostDiscovery
import com.example.kampai.cardgame.ui.viewmodel.LobbyViewModel

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel = hiltViewModel(),
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val availableHosts by viewModel.availableHosts.collectAsState()

    // BoxWithConstraints es la clave para la responsividad
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0B2E), Color(0xFF2D1B4E))
                )
            )
    ) {
        // Calculamos dimensiones dinámicas
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight

        // Padding dinámico: 5% del ancho o mínimo 16dp
        val padding = (screenWidth * 0.05f).coerceAtLeast(16.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Responsivo
            LobbyHeader(
                onBack = onBack,
                screenWidth = screenWidth
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))

            when (connectionState) {
                is ConnectionState.Disconnected -> {
                    DisconnectedViewResponsive(
                        availableHosts = availableHosts,
                        onHostGame = { viewModel.hostGame() },
                        onJoinHost = { host -> viewModel.joinHost(host) },
                        onRefresh = { viewModel.refreshHosts() },
                        isLandscape = isLandscape,
                        parentHeight = screenHeight
                    )
                }

                is ConnectionState.Hosting -> {
                    val state = connectionState as ConnectionState.Hosting
                    HostLobbyView(
                        hostInfo = state.hostInfo,
                        connectedPlayers = state.connectedPlayers,
                        onStartGame = {
                            viewModel.startGame()
                            onStartGame()
                        },
                        onDisconnect = { viewModel.disconnect() }
                    )
                }

                is ConnectionState.Connected -> {
                    val state = connectionState as ConnectionState.Connected
                    ClientLobbyView(
                        playerInfo = state.playerInfo,
                        hostIp = state.hostIp,
                        onDisconnect = { viewModel.disconnect() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LobbyHeader(
    onBack: () -> Unit,
    screenWidth: Dp
) {
    // Tamaño de fuente dinámico basado en el ancho
    val titleSize = (screenWidth * 0.06f).value.coerceIn(20f, 32f).sp
    val iconSize = (screenWidth * 0.12f).coerceIn(40.dp, 56.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(iconSize)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "🃏 Sala de Juego",
            fontSize = titleSize,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
private fun DisconnectedViewResponsive(
    availableHosts: List<HostDiscovery>,
    onHostGame: () -> Unit,
    onJoinHost: (HostDiscovery) -> Unit,
    onRefresh: () -> Unit,
    isLandscape: Boolean,
    parentHeight: Dp
) {
    if (isLandscape) {
        // LAYOUT HORIZONTAL (Tablets o Teléfonos girados)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Panel Izquierdo: Crear Partida
            Column(modifier = Modifier.weight(1f)) {
                CreateGameCard(
                    onClick = onHostGame,
                    modifier = Modifier.weight(1f) // Ocupa todo el alto disponible
                )
            }

            // Panel Derecho: Lista
            Column(modifier = Modifier.weight(1f)) {
                AvailableHostsHeader(onRefresh)
                Spacer(modifier = Modifier.height(8.dp))
                HostsList(availableHosts, onJoinHost)
            }
        }
    } else {
        // LAYOUT VERTICAL (Teléfonos normales)
        Column(modifier = Modifier.fillMaxSize()) {
            // Botón Host ocupa el 25% de la pantalla
            CreateGameCard(
                onClick = onHostGame,
                modifier = Modifier.height(parentHeight * 0.25f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AvailableHostsHeader(onRefresh)

            Spacer(modifier = Modifier.height(16.dp))

            // La lista ocupa el resto
            HostsList(
                availableHosts = availableHosts,
                onJoinHost = onJoinHost,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CreateGameCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎮", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crear Partida",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ser el anfitrión",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun AvailableHostsHeader(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Partidas Disponibles",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        IconButton(onClick = onRefresh) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Actualizar",
                tint = Color(0xFF06B6D4)
            )
        }
    }
}

@Composable
private fun HostsList(
    availableHosts: List<HostDiscovery>,
    onJoinHost: (HostDiscovery) -> Unit,
    modifier: Modifier = Modifier
) {
    if (availableHosts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF06B6D4),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Buscando partidas en tu red...",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availableHosts) { host ->
                HostCard(host = host, onClick = { onJoinHost(host) })
            }
        }
    }
}

@Composable
private fun HostCard(
    host: HostDiscovery,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = host.hostName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = host.hostIp,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF10B981).copy(alpha = 0.2f)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Unirse",
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

// Las vistas de HostLobby y ClientLobby ya son bastante flexibles,
// pero podemos asegurarnos que usen el espacio disponible con weights

@Composable
private fun HostLobbyView(
    hostInfo: PlayerInfo,
    connectedPlayers: List<PlayerInfo>,
    onStartGame: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tu Sala (Host)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = "Código IP: ${connectedPlayers.size + 1} jugadores",
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista con peso para ocupar espacio disponible
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { PlayerListItem(hostInfo, isHost = true) }
            items(connectedPlayers) { player ->
                PlayerListItem(player, isHost = false)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartGame,
            enabled = connectedPlayers.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🎮 Iniciar Partida", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar Sala", color = Color.Red.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun ClientLobbyView(
    playerInfo: PlayerInfo,
    hostIp: String,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            // Animación de pulso
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = "⏳",
                fontSize = 64.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Conectado",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Esperando al anfitrión...",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text("Salir", color = Color.White)
            }
        }
    }
}

@Composable
private fun PlayerListItem(player: PlayerInfo, isHost: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHost) Color(0xFF6A1B9A).copy(alpha = 0.3f)
            else Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isHost) "👑" else "👤",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = player.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isHost) {
                Text(
                    text = "HOST",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )
            }
        }
    }
}