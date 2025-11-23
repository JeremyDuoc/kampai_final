package com.example.kampai.cardgame.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.cardgame.domain.engine.GameEngine
import com.example.kampai.cardgame.domain.models.*
import com.example.kampai.cardgame.network.ConnectionState
import com.example.kampai.cardgame.network.HostDiscovery
import com.example.kampai.cardgame.network.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val networkManager: NetworkManager,
    private val gameEngine: GameEngine
) : ViewModel() {

    val connectionState = networkManager.connectionState

    private val _availableHosts = MutableStateFlow<List<HostDiscovery>>(emptyList())
    val availableHosts: StateFlow<List<HostDiscovery>> = _availableHosts.asStateFlow()

    private val _playerName = MutableStateFlow("Jugador ${(1000..9999).random()}")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    init {
        startHostDiscovery()
    }

    private fun startHostDiscovery() {
        viewModelScope.launch {
            networkManager.discoverHosts().collect { host ->
                // Solo actualizamos si la IP no es la nuestra (NetworkManager ya filtra, pero doble check)
                // y evitamos el parpadeo actualizando la lista de forma acumulativa

                val currentList = _availableHosts.value.toMutableList()

                // Buscamos si ya existe este host por IP
                val existingIndex = currentList.indexOfFirst { it.hostIp == host.hostIp }

                if (existingIndex != -1) {
                    // Si existe, actualizamos el nombre por si cambió
                    currentList[existingIndex] = host
                } else {
                    // Si no existe, lo añadimos
                    currentList.add(host)
                }

                // Limpieza opcional: Podríamos eliminar hosts antiguos si tuviéramos timestamp,
                // pero por ahora esto evita el parpadeo visual.

                _availableHosts.value = currentList
            }
        }
    }

    fun hostGame() {
        viewModelScope.launch {
            val playerInfo = PlayerInfo(
                id = UUID.randomUUID().toString(),
                name = _playerName.value,
                designId = GameDesign.CLASSIC.getDesignId(),
                isHost = true
            )

            val result = networkManager.startHost(playerInfo)

            if (result.isFailure) {
                // Handle error
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun joinHost(host: HostDiscovery) {
        viewModelScope.launch {
            val playerInfo = PlayerInfo(
                id = UUID.randomUUID().toString(),
                name = _playerName.value,
                designId = GameDesign.CLASSIC.getDesignId(),
                isHost = false
            )

            val result = networkManager.connectToHost(host.hostIp, playerInfo)

            if (result.isFailure) {
                // Handle error
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun startGame() {
        val state = connectionState.value
        if (state is ConnectionState.Hosting) {
            val allPlayers = listOf(state.hostInfo) + state.connectedPlayers
            val ruleConfig = RuleConfig.DEFAULT

            // Initialize game engine
            gameEngine.initializeGame(allPlayers, ruleConfig)
        }
    }

    fun disconnect() {
        networkManager.disconnect()
        _availableHosts.value = emptyList()
    }

    fun refreshHosts() {
        _availableHosts.value = emptyList()
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    override fun onCleared() {
        super.onCleared()
        networkManager.disconnect()
    }
}

// ==================== CONFIGURACIÓN ANDROIDMANIFEST.XML ====================
/*
Agrega esto a tu AndroidManifest.xml:

<manifest>
    <!-- Permisos de red -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />

    <application>
        <!-- Actividad del juego de cartas -->
        <activity
            android:name=".cardgame.ui.CardGameActivity"
            android:exported="false"
            android:screenOrientation="landscape"
            android:configChanges="orientation|screenSize"
            android:theme="@style/Theme.Kampai" />
    </application>
</manifest>
*/

// ==================== MÓDULO HILT ====================
/*
Crea este archivo: cardgame/di/CardGameModule.kt

package com.example.kampai.cardgame.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CardGameModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
    }
}
*/

// ==================== AGREGAR A build.gradle.kts ====================
/*
dependencies {
    // Networking
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines (si no están ya)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
*/

// ==================== NAVEGACIÓN DESDE HOME ====================
/*
En tu HomeScreen.kt, agrega un nuevo botón para el juego de cartas:

@Composable
fun HomeScreen(...) {
    val context = LocalContext.current

    // ... existing code ...

    // Agregar este botón en tu grid/lista de juegos
    GameModel(
        id = "card_game",
        title = "Juego de Cartas",
        description = "Multijugador local tipo UNO",
        iconRes = R.drawable.cards, // Necesitas este drawable
        color = Color(0xFFE53935),
        route = "card_game"
    )
}

// En tu composable de navegación principal (MainActivity.kt):
composable("card_game") {
    val intent = Intent(context, CardGameActivity::class.java)
    context.startActivity(intent)
}
*/

// ==================== ESTRUCTURA DE CARPETAS FINAL ====================
/*
app/src/main/java/com/example/kampai/cardgame/
├── di/
│   └── CardGameModule.kt
├── domain/
│   ├── engine/
│   │   └── GameEngine.kt
│   └── models/
│       └── [Todos los modelos de datos]
├── network/
│   └── NetworkManager.kt
├── ui/
│   ├── components/
│   │   └── CardComponents.kt
│   ├── screens/
│   │   ├── GameScreen.kt
│   │   └── LobbyScreen.kt
│   ├── viewmodel/
│   │   ├── GameViewModel.kt
│   │   └── LobbyViewModel.kt
│   └── CardGameActivity.kt
└── README.md (Documentación del juego)
*/

// ==================== PRÓXIMOS PASOS ====================
/*
1. Crear la estructura de carpetas como se muestra arriba
2. Copiar todos los archivos de código a sus ubicaciones
3. Agregar permisos al AndroidManifest.xml
4. Agregar la actividad CardGameActivity al manifest
5. Agregar dependencias a build.gradle.kts
6. Crear el CardGameModule.kt para Hilt
7. Agregar el drawable para el icono del juego
8. Integrar la navegación desde HomeScreen
9. Compilar y probar

TESTING:
- Para probar multijugador local, necesitarás 2+ dispositivos en la misma red WiFi
- O usa un emulador + dispositivo físico configurados en la misma red
- El host aparecerá automáticamente en la lista de partidas disponibles

CARACTERÍSTICAS IMPLEMENTADAS:
✅ Sistema completo de cartas UNO
✅ Motor de reglas dinámicas configurable
✅ Sistema KAMPAI de penalización
✅ Multijugador LAN (Host-Cliente)
✅ Descubrimiento automático de partidas
✅ UI horizontal responsiva
✅ Sistema de capas para skins personalizados
✅ Sincronización de estado en red
✅ Privacidad de manos (solo el jugador activo ve todas sus cartas)
✅ Temporizadores de turno y penalidad
✅ Pantalla de transición de turno (Guard Screen)
✅ Detección forzada de orientación horizontal

FUTURAS MEJORAS:
- Añadir más diseños de skins
- Sonidos y efectos visuales
- Animaciones de cartas jugadas
- Chat entre jugadores
- Estadísticas de partida
- Modo espectador
- Reconexión automática
- Guardado de preferencias de jugador
*/