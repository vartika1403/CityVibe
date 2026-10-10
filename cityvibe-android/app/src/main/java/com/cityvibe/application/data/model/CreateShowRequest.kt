package com.cityvibe.application.data.model

/**
 * Body of `POST api/events` — the show a user submits from the Create Show screen.
 *
 * Field names mirror [Event] so the backend can bind it straight onto its entity.
 * The backend has no dedicated city column, so the city is sent as [venueName].
 */
data class CreateShowRequest(
    val title: String,
    val category: String,
    val description: String,
    /** ISO-8601, e.g. "2026-08-22T19:00:00" — same shape as [Event.dateTime]. */
    val dateTime: String,
    val duration: String,
    val venueName: String,
    val price: String,
    /** Cover image chosen on the device, or null if the host skipped it. */
    val imageUrl: String?
)
