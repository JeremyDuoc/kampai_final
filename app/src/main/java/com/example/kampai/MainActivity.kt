package com.example.kampai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kampai.ui.theme.KampaiTheme
import com.example.kampai.ui.theme.SplashScreen
import com.example.kampai.ui.theme.home.HomeScreen
// Importamos la NUEVA pantalla de selección
import com.example.kampai.ui.theme.culture.CultureSelectionScreen
import com.example.kampai.ui.theme.bomb.BombGameScreen
import com.example.kampai.ui.theme.never.NeverGameScreen
import com.example.kampai.ui.theme.truth.TruthGameScreen
import com.example.kampai.ui.theme.culture.CultureGameScreen
import com.example.kampai.ui.theme.highlow.HighLowGameScreen
import com.example.kampai.ui.theme.medusa.MedusaGameScreen
import com.example.kampai.ui.theme.charades.CharadesGameScreen
import com.example.kampai.ui.theme.home.ClassicsScreen
import com.example.kampai.ui.theme.impostor.ImpostorGameScreen
import com.example.kampai.ui.theme.roulette.RouletteGameScreen
import com.example.kampai.ui.theme.judge.JudgeGameScreen
import com.example.kampai.ui.theme.likely.MostLikelyScreen
import com.example.kampai.ui.theme.staring.StaringGameScreen
// Importante: Importación del PartyManager
import com.example.kampai.ui.theme.partymanager.PartyManagerScreen
import com.example.kampai.ui.theme.warmup.WarmupGameScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KampaiTheme {
                KampaiApp()
            }
        }
    }
}

@Composable
fun KampaiApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(
                onGameSelected = { route ->
                    navController.navigate(route)
                },
                onNavigateToClassics = {
                    navController.navigate("classics_screen") // Navegar a clásicos
                },
                onPartyManager = {
                    navController.navigate("party_manager")
                }
            )
        }

        // --- AÑADE ESTE BLOQUE ---
        composable("classics_screen") {
            ClassicsScreen(
                onGameSelected = { route ->
                    navController.navigate(route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // --- RUTA DEL PARTY MANAGER (Agregada) ---
        composable("party_manager") {
            PartyManagerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // --- NUEVA RUTA INTERMEDIA ---
        composable("culture_selection") {
            CultureSelectionScreen(
                onNavigateToBomb = { navController.navigate("game_bomb") },
                onNavigateToClassic = { navController.navigate("game_culture") },
                onBack = { navController.popBackStack() }
            )
        }

        // JUEGOS (Se mantienen sus rutas originales internas para que funcione la navegación)

        composable("game_bomb") {
            BombGameScreen(onBack = { navController.popBackStack() })
        }

        composable("game_culture") {
            CultureGameScreen(onBack = { navController.popBackStack() })
        }

        composable("game_warmup") {
            WarmupGameScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("game_likely") {
            MostLikelyScreen(onBack = { navController.popBackStack() })
        }
        composable("game_impostor") {
            ImpostorGameScreen(onBack = { navController.popBackStack() })
        }

        // ... resto de juegos ...
        composable("game_never") { NeverGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_truth") { TruthGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_highlow") { HighLowGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_medusa") { MedusaGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_charades") { CharadesGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_roulette") { RouletteGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_judge") { JudgeGameScreen(onBack = { navController.popBackStack() }) }
        composable("game_staring") { StaringGameScreen(onBack = { navController.popBackStack() }) }
    }
}