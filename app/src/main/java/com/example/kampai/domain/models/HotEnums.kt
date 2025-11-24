package com.example.kampai.domain.models

// Define los niveles de temperatura del juego
enum class HotIntensity {
    SOFT,   // Fase 1: Rompehielos, preguntas, miradas
    MEDIUM, // Fase 2: Tensión, contacto leve, masajes
    HOT,    // Fase 3: Acción, besos, hielo
    EXTREME // Fase 4: Peligro (Solo si el usuario lo activa explícitamente)
}

// Define a quién va dirigido (para la lógica inteligente)
enum class HotTarget {
    SOLO,   // El jugador hace algo solo
    COUPLE, // Involucra a otro jugador específico
    GROUP   // Todos participan
}