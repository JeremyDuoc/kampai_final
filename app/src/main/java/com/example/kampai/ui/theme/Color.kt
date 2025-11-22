package com.example.kampai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cores revisadas para estética Ilustrativa/Playful Dark
val PrimaryViolet = Color(0xFF6A1B9A) // Un violeta más profundo y rico
val SecondaryPink = Color(0xFFE100FF)
val BackgroundDark = Color(0xFF121212) // Fondo muy oscuro para contraste
val SurfaceDark = Color(0xFF1E1E1E) // Fondo de tarjetas más sutil
val AccentCyan = Color(0xFF06B6D4)
val AccentRed = Color(0xFFEF4444)
val AccentAmber = Color(0xFFF59E0B)
val TextWhite = Color(0xFFF0F8FF)
val TextGray = Color(0xFF94A3B8)

// Gradientes para efectos visuales (se mantienen los originales)
val BombGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFFEE5A6F),
        Color(0xFFC06C84)
    )
)

val MedusaGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF06B6D4),
        Color(0xFF0891B2),
        Color(0xFF155E75)
    )
)

val VioletGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6A1B9A), // Usando el nuevo PrimaryViolet
        Color(0xFFE100FF)
    )
)