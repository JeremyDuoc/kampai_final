package com.example.kampai.domain.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class GameModel(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color,
    val route: String
)