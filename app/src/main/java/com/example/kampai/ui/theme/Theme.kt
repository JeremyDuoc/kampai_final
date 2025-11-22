package com.example.kampai.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryVioletDark,
    secondary = SecondaryPinkDark,
    background = BackgroundDarkMode,
    surface = SurfaceDarkMode,
    onPrimary = TextWhiteDark,
    onSecondary = TextWhiteDark,
    onBackground = TextWhiteDark,
    onSurface = TextWhiteDark,
    error = AccentRedDark,
    onError = Color.White
)

// ========== ESQUEMA DE COLORES CLAROS - TOTALMENTE REVISADO ==========
private val LightColorScheme = lightColorScheme(
    primary = PrimaryVioletLight,
    secondary = SecondaryPinkLight,
    tertiary = AccentCyanLight,
    background = BackgroundLightMode,
    surface = SurfaceLightMode,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextBlackLight,
    onSurface = TextBlackLight,
    onSurfaceVariant = TextLightSecondary,
    error = AccentRedLight,
    onError = Color.White,
    outline = TextGrayLight,
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun KampaiTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}