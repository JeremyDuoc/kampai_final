package com.example.kampai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kampai.ui.theme.KampaiTheme
import com.example.kampai.ui.theme.SplashScreen
import com.example.kampai.ui.theme.ThemeManager
import com.example.kampai.ui.theme.bomb.BombGameScreen
import com.example.kampai.ui.theme.charades.CharadesGameScreen
import com.example.kampai.ui.theme.culture.CultureGameScreen
import com.example.kampai.ui.theme.culture.CultureSelectionScreen
import com.example.kampai.ui.theme.highlow.HighLowGameScreen
import com.example.kampai.ui.theme.home.ClassicsScreen
import com.example.kampai.ui.theme.home.HomeScreen
import com.example.kampai.ui.theme.impostor.ImpostorGameScreen
import com.example.kampai.ui.theme.kingscup.KingsCupGameScreen
import com.example.kampai.ui.theme.likely.MostLikelyScreen
import com.example.kampai.ui.theme.never.NeverGameScreen
import com.example.kampai.ui.theme.partymanager.PartyManagerScreen
import com.example.kampai.ui.theme.roulette.RouletteGameScreen
import com.example.kampai.ui.theme.settings.SettingsScreen
import com.example.kampai.ui.theme.truth.TruthGameScreen
import com.example.kampai.ui.theme.warmup.WarmupGameScreen
import com.example.kampai.utils.navigateSafe
import com.example.kampai.utils.popBackStackSafe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = true)

            KampaiTheme(isDarkMode = isDarkMode) {
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
                    navController.navigateSafe(route)
                },
                onNavigateToClassics = {
                    navController.navigateSafe("classics_screen")
                },
                onPartyManager = {
                    navController.navigateSafe("party_manager")
                },
                onNavigateToSettings = {
                    navController.navigateSafe("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("classics_screen") {
            ClassicsScreen(
                onGameSelected = { route ->
                    navController.navigateSafe(route)
                },
                onBack = {
                    navController.popBackStackSafe()
                }
            )
        }

        composable("party_manager") {
            PartyManagerScreen(
                onBack = { navController.popBackStackSafe() }
            )
        }

        composable("culture_selection") {
            CultureSelectionScreen(
                onNavigateToBomb = { navController.navigateSafe("game_bomb") },
                onNavigateToClassic = { navController.navigateSafe("game_culture") },
                onBack = { navController.popBackStackSafe() }
            )
        }

        composable("game_bomb") {
            BombGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_culture") {
            CultureGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_warmup") {
            WarmupGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_kingscup") {
            KingsCupGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_likely") {
            MostLikelyScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_impostor") {
            ImpostorGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_never") {
            NeverGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_truth") {
            TruthGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_highlow") {
            HighLowGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_charades") {
            CharadesGameScreen(onBack = { navController.popBackStackSafe() })
        }

        composable("game_roulette") {
            RouletteGameScreen(onBack = { navController.popBackStackSafe() })
        }
    }
}