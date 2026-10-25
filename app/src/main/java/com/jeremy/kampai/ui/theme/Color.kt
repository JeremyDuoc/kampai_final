package com.jeremy.kampai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============ COLORES OSCUROS ============
val PrimaryVioletDark = Color(0xFF6A1B9A)
val SecondaryPinkDark = Color(0xFFE100FF)
val BackgroundDarkMode = Color(0xFF07070A) // Deeper OLED black
val SurfaceDarkMode = Color(0xFF12121A)
val AccentCyanDark = Color(0xFF06B6D4)
val AccentRedDark = Color(0xFFEF4444)
val AccentAmberDark = Color(0xFFF59E0B)
val TextWhiteDark = Color(0xFFF8FAFC)
val TextGrayDark = Color(0xFF94A3B8)

// ============ NEO-ARCADE & AFTERHOURS PALETTE ============
object KColor {
    val Base = Color(0xFF07070A)
    val Raised = Color(0xFF101018)
    val Strong = Color(0xFF1C1A27)
    val Foreground = Color(0xFFF7F6FF)
    val Secondary = Color(0xFFB9B6C9)
    val Muted = Color(0xFF918DA3)
    val Purple = Color(0xFFA76BFF)
    val Cyan = Color(0xFF4CEBFF)
    val Amber = Color(0xFFFFC46B)
    val Pink = Color(0xFFFF5DAA)
    val Green = Color(0xFF9AFF72)
    val InkOnAccent = Color(0xFF160D22)
    val Danger = Color(0xFFFF728F)
    val Info = Color(0xFF8CC9FF)
}

val OledBlack = KColor.Base
val CyberSurface = KColor.Raised
val CyberSurfaceElevated = KColor.Strong
val CyberBorder = Color(0xFF2E2E44)
val NeonPurple = KColor.Purple
val NeonPink = KColor.Pink
val CyberCyan = KColor.Cyan
val SolarAmber = KColor.Amber
val ToxicGreen = KColor.Green
val NeonFlame = Color(0xFFFF5252)


// ============ COLORES CLAROS - MEJORADOS PARA LEGIBILIDAD ============
val PrimaryVioletLight = Color(0xFF4A148C)      // Violeta muy oscuro
val SecondaryPinkLight = Color(0xFFAD1457)      // Rosa muy oscuro
val BackgroundLightMode = Color(0xFFFAFAFA)     // Gris muy claro (casi blanco)
val SurfaceLightMode = Color(0xFFFFFFFF)        // Blanco puro
val AccentCyanLight = Color(0xFF006064)         // Cyan muy oscuro
val AccentRedLight = Color(0xFFB71C1C)          // Rojo muy oscuro
val AccentAmberLight = Color(0xFFE65100)        // Naranja muy oscuro
val TextBlackLight = Color(0xFF000000)          // Negro puro
val TextGrayLight = Color(0xFF212121)           // Gris muy oscuro

// ============ COLORES ADICIONALES PARA MODO CLARO ============
val AccentGreenLight = Color(0xFF1B5E20)        // Verde muy oscuro
val AccentBlueLight = Color(0xFF0D47A1)         // Azul muy oscuro
val TextLightSecondary = Color(0xFF424242)      // Gris oscuro para texto secundario

// ============ GRADIENTES OSCUROS ============
val BombGradientDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFEE5A6F),
        Color(0xFFC06C84)
    )
)

val MedusaGradientDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF06B6D4),
        Color(0xFF0891B2),
        Color(0xFF155E75)
    )
)

val VioletGradientDark = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6A1B9A),
        Color(0xFFE100FF)
    )
)

// ============ GRADIENTES CLAROS - MEJORADOS ============
val BombGradientLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFC62828),
        Color(0xFFB71C1C),
        Color(0xFF8B0000)
    )
)

val MedusaGradientLight = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF004B87),
        Color(0xFF006BA3),
        Color(0xFF0077B6)
    )
)

val VioletGradientLight = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF5B2670),
        Color(0xFFC20080)
    )
)

// ============ NOMBRES CORTOS PARA COMPATIBILIDAD ============
val PrimaryViolet = PrimaryVioletDark
val SecondaryPink = SecondaryPinkDark
val BackgroundDark = BackgroundDarkMode
val SurfaceDark = SurfaceDarkMode
val AccentCyan = AccentCyanDark
val AccentRed = AccentRedDark
val AccentAmber = AccentAmberDark
val TextWhite = TextWhiteDark
val TextGray = TextGrayDark
val BombGradient = BombGradientDark
val MedusaGradient = MedusaGradientDark
val VioletGradient = VioletGradientDark