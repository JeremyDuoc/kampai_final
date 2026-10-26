package com.jeremy.kampai.domain.models

enum class FeaturedGameSource {
    DEFAULT,
    COMMUNITY
}

data class FeaturedGameSnapshot(
    val gameId: String,
    val source: FeaturedGameSource = FeaturedGameSource.DEFAULT,
    val totalSessions7d: Int = 0
)
