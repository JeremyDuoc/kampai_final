package com.example.kampai.cardgame.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kampai.cardgame.ui.screens.GameScreen
import com.example.kampai.cardgame.ui.screens.LobbyScreen
import com.example.kampai.ui.theme.KampaiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KampaiTheme {
                CardGameNavigation()
            }
        }
    }
}

@Composable
fun CardGameNavigation() {
    val navController = rememberNavController()

    // 1. Obtenemos la referencia a la Actividad actual para poder cerrarla
    val activity = LocalContext.current as? Activity

    NavHost(
        navController = navController,
        startDestination = "lobby"
    ) {
        composable("lobby") {
            LobbyScreen(
                onStartGame = {
                    navController.navigate("game")
                },
                onBack = {
                    // 2. AQUÍ ESTABA EL ERROR: Antes estaba vacío.
                    // Ahora llamamos a finish() para cerrar esta pantalla y volver al Home
                    activity?.finish()
                }
            )
        }

        composable("game") {
            GameScreen(
                onBack = {
                    // Si estamos en el juego, volvemos al lobby
                    navController.popBackStack()
                }
            )
        }
    }
}