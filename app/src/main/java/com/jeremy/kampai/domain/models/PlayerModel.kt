package com.jeremy.kampai.domain.models

import androidx.compose.ui.graphics.Color
import com.jeremy.kampai.R
import com.jeremy.kampai.ui.theme.AccentAmber
import com.jeremy.kampai.ui.theme.AccentCyan
import com.jeremy.kampai.ui.theme.AccentRed
import com.jeremy.kampai.ui.theme.PrimaryViolet
import com.jeremy.kampai.ui.theme.SecondaryPink

enum class Gender(val emoji: String, val nameRes: Int) {
    MALE("👨", R.string.gender_male),
    FEMALE("👩", R.string.gender_female),
    OTHER("👽", R.string.gender_other);
}


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
    val avatarEmoji: String = AvatarEmojis.getRandomEmoji(),
    val isReady: Boolean = true
) {
    companion object {
        private val avatarColors = listOf(
            PrimaryViolet, SecondaryPink, AccentCyan, AccentRed, AccentAmber,
            Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFFF59E0B),
            Color(0xFF06B6D4), Color(0xFFEC4899)
        )
        fun getColorForIndex(index: Int): Color = avatarColors[index % avatarColors.size]
    }
    fun getAvatarColor(): Color = getColorForIndex(colorIndex)
    fun getDisplayEmoji(): String = avatarEmoji
}