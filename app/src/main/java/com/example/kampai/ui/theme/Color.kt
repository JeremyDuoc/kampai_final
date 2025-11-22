package com.example.kampai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============ COLORES OSCUROS ============
val PrimaryVioletDark = Color(0xFF6A1B9A)
val SecondaryPinkDark = Color(0xFFE100FF)
val BackgroundDarkMode = Color(0xFF121212)
val SurfaceDarkMode = Color(0xFF1E1E1E)
val AccentCyanDark = Color(0xFF06B6D4)
val AccentRedDark = Color(0xFFEF4444)
val AccentAmberDark = Color(0xFFF59E0B)
val TextWhiteDark = Color(0xFFF0F8FF)
val TextGrayDark = Color(0xFF94A3B8)

// ============ COLORES CLAROS - MEJORADOS PARA LEGIBILIDAD ============
val PrimaryVioletLight = Color(0xFF5B2670)      // Violeta oscuro
val SecondaryPinkLight = Color(0xFFC20080)      // Rosa oscuro saturado
val BackgroundLightMode = Color(0xFFF8F5FB)     // Fondo ligeramente violáceo
val SurfaceLightMode = Color(0xFFFFFFFF)        // Blanco puro
val AccentCyanLight = Color(0xFF0077A8)         // Cyan oscuro
val AccentRedLight = Color(0xFFD32F2F)          // Rojo oscuro
val AccentAmberLight = Color(0xFFD87F1A)        // Ámbar oscuro
val TextBlackLight = Color(0xFF1A1A1A)          // Negro real
val TextGrayLight = Color(0xFF424242)           // Gris oscuro

// ============ COLORES ADICIONALES PARA MODO CLARO ============
val AccentGreenLight = Color(0xFF0B7A1A)        // Verde oscuro
val AccentBlueLight = Color(0xFF1A47A1)         // Azul oscuro
val TextLightSecondary = Color(0xFF616161)      // Gris secundario

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