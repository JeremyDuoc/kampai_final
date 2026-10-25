package com.jeremy.kampai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jeremy.kampai.ui.theme.KampaiTheme
import com.jeremy.kampai.ui.theme.SplashScreen
import com.jeremy.kampai.ui.theme.ThemeManager
import com.jeremy.kampai.ui.theme.bomb.BombGameScreen
import com.jeremy.kampai.ui.theme.culture.CultureGameScreen
import com.jeremy.kampai.ui.theme.culture.CultureSelectionScreen
import com.jeremy.kampai.ui.theme.home.HomeScreen
import android.widget.Toast
import com.jeremy.kampai.ui.theme.impostor.ImpostorGameScreen
import com.jeremy.kampai.ui.theme.kingscup.KingsCupGameScreen
import com.jeremy.kampai.ui.theme.likely.MostLikelyScreen
import com.jeremy.kampai.ui.theme.never.NeverGameScreen
import com.jeremy.kampai.ui.theme.partymanager.PartyManagerScreen
import com.jeremy.kampai.ui.theme.settings.SettingsScreen
import com.jeremy.kampai.ui.theme.truth.TruthGameScreen
import com.jeremy.kampai.ui.theme.warmup.WarmupGameScreen
import com.jeremy.kampai.utils.navigateSafe
import com.jeremy.kampai.utils.popBackStackSafe
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var themeManager: ThemeManager

    private var mInterstitialAd: InterstitialAd? = null
    private var classicsClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this) {}
        loadInterstitial()
        RewardedAdManager.loadAd(this)
        setContent {
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = true)
            KampaiTheme(isDarkMode = isDarkMode) {
                KampaiApp(
                    onShowAdBeforeClassics = { onComplete ->
                        showInterstitial(onComplete)
                    },
                    onSupportCreator = {
                        RewardedAdManager.showAd(this) {
                            runOnUiThread {
                                Toast.makeText(
                                    this,
                                    "¡Muchas gracias por tu apoyo! ❤️ Esto ayuda a seguir mejorando Kampai.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    }
                )
            }
        }
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-5499036778442602/5842492147", adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showInterstitial(onAdFinished: () -> Unit) {
        classicsClickCount++
        if (mInterstitialAd != null && classicsClickCount % 3 == 0) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitial()
                    onAdFinished()
                }
                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    mInterstitialAd = null
                    onAdFinished()
                }
            }
            mInterstitialAd?.show(this)
        } else {
            onAdFinished()
        }
    }
}

@Composable
fun KampaiApp(onShowAdBeforeClassics: (() -> Unit) -> Unit, onSupportCreator: () -> Unit) {
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
                onPartyManager = {
                    navController.navigateSafe("party_manager")
                },
                onNavigateToSettings = {
                    navController.navigateSafe("settings")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStackSafe() },
                onSupportClick = {
                    onSupportCreator()
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
            WarmupGameScreen(
                onShowAd = { onAdDone ->
                    onShowAdBeforeClassics(onAdDone)
                },
                onBack = { navController.popBackStackSafe() }
            )
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
    }
}