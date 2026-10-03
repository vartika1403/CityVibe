package com.cityvibe.app.util

import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    /** Parses an ISO-8601 string like "2026-07-18T19:00:00" into "Sat, 18 Jul · 7:00 PM". */
    fun formatDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser.parse(iso)
            val out = SimpleDateFormat("EEE, dd MMM · h:mm a", Locale.getDefault())
            out.format(date!!)
        } catch (e: Exception) {
            iso
        }
    }

    /** Formats epoch millis as local ISO-8601 like "2026-07-18T19:00:00" — the shape [formatDateTime] parses. */
    fun toIsoDateTime(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(millis))

    /** A solid accent color for each event category tag. */
    fun categoryColor(category: String?): Int = when (category?.lowercase()) {
        "music" -> Color.parseColor("#6C5CE7")
        "comedy" -> Color.parseColor("#FF9F1C")
        "meetup", "meetups" -> Color.parseColor("#2EC4B6")
        else -> Color.parseColor("#FF5A5F")
    }
}
