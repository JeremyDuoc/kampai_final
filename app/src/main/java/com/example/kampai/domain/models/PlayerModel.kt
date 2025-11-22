package com.example.kampai.domain.models

import androidx.compose.ui.graphics.Color
import com.example.kampai.ui.theme.AccentAmber
import com.example.kampai.ui.theme.AccentCyan
import com.example.kampai.ui.theme.AccentRed
import com.example.kampai.ui.theme.PrimaryViolet
import com.example.kampai.ui.theme.SecondaryPink

enum class Gender {
    MALE,
    FEMALE,
    OTHER;

    fun getDisplayName(): String = when (this) {
        MALE -> "Hombre"
        FEMALE -> "Mujer"
        OTHER -> "Otro"
    }

    fun getEmoji(): String = when (this) {
        MALE -> "👨"
        FEMALE -> "👩"
        OTHER -> "🧑"
    }
}

// NUEVO: Lista de avatares disponibles
object AvatarEmojis {
    val animals = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
        "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔",
        "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🦇", "🐺",
        "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞",
        "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦐",
        "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋",
        "🦈", "🐊", "🐅", "🐆", "🦓", "🦍", "🦧", "🐘",
        "🦛", "🦏", "🐪", "🐫", "🦒", "🦘", "🦬", "🐃"
    )

    val faces = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
        "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
        "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜",
        "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏",
        "😒", "😞", "😔", "😟", "😕", "🙁", "😣", "😖",
        "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡"
    )

    val fantasy = listOf(
        "👽", "👾", "🤖", "👻", "💀", "☠️", "👹", "👺",
        "🎃", "😈", "👿", "🧙", "🧚", "🧛", "🧜", "🧝",
        "🧞", "🧟", "🦸", "🦹", "🧑‍🎄", "🧌"
    )

    val sports = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉",
        "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
        "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿"
    )

    fun getAllEmojis() = animals + faces + fantasy + sports

    fun getRandomEmoji() = getAllEmojis().random()
}

data class PlayerModel(
    val id: String,
    val name: String,
    val gender: Gender,
    val colorIndex: Int = 0,
    val avatarEmoji: String = AvatarEmojis.getRandomEmoji()
) {
    companion object {
        private val avatarColors = listOf(
            PrimaryViolet,
            SecondaryPink,
            AccentCyan,
            AccentRed,
            AccentAmber,
            Color(0xFF10B981), // Green
            Color(0xFF8B5CF6), // Purple
            Color(0xFFF59E0B), // Orange
            Color(0xFF06B6D4), // Cyan
            Color(0xFFEC4899)  // Pink
        )

        fun getColorForIndex(index: Int): Color {
            return avatarColors[index % avatarColors.size]
        }
    }

    fun getAvatarColor(): Color = getColorForIndex(colorIndex)

    // Método para obtener el emoji del avatar
    fun getDisplayEmoji(): String = avatarEmoji
}