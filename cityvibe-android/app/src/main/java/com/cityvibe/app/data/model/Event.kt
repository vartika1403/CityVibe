package com.cityvibe.app.data.model

import java.io.Serializable

data class Event(
    val id: Long,
    val title: String,
    val category: String,
    val description: String?,
    val imageUrl: String?,
    val dateTime: String?,
    val duration: String?,
    val venueName: String?,
    val venueAddress: String?,
    val organizer: String?,
    val price: String?
) : Serializable
