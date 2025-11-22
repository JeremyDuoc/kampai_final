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
    onSurface = TextWhiteDark
)

// ========== ESQUEMA DE COLORES CLAROS MEJORADO ==========
private val LightColorScheme = lightColorScheme(
    primary = PrimaryVioletLight,
    secondary = SecondaryPinkLight,
    background = BackgroundLightMode,
    surface = SurfaceLightMode,
    onPrimary = Color(0xFFFFFFFF),      // Texto blanco sobre primario
    onSecondary = Color(0xFFFFFFFF),    // Texto blanco sobre secundario
    onBackground = TextBlackLight,      // Texto negro sobre fondo claro
    onSurface = TextBlackLight          // Texto negro sobre superficie
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