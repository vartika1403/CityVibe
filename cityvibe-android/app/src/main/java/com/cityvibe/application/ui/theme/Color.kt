package com.cityvibe.application.ui.theme

import androidx.compose.ui.graphics.Color

/** Compose mirror of res/values/colors.xml. */
object CityVibeColors {
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF111114)

    val Brand = Color(0xFFFF5A5F)
    val BrandDark = Color(0xFFE03B53)
    val BrandAmber = Color(0xFFFF8E53)

    val Bg = Color(0xFFF6F6FA)
    val Surface = Color(0xFFFFFFFF)

    val TextPrimary = Color(0xFF16161A)
    val TextSecondary = Color(0xFF6B6B76)

    val CatMusic = Color(0xFF6C5CE7)
    val CatComedy = Color(0xFFFF9F1C)
    val CatMeetup = Color(0xFF2EC4B6)
    val CatGathering = Color(0xFFFF5A5F)

    /** Accent for a category tag — mirrors Formatters.categoryColor. */
    fun categoryColor(category: String?): Color = when (category?.lowercase()) {
        "music" -> CatMusic
        "comedy" -> CatComedy
        "meetup", "meetups" -> CatMeetup
        else -> CatGathering
    }
}
