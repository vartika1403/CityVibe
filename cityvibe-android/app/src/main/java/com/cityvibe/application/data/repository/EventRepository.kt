package com.cityvibe.application.data.repository

import com.cityvibe.application.data.model.CreateShowRequest
import com.cityvibe.application.data.model.Event
import com.cityvibe.application.data.remote.RetrofitClient

class EventRepository {

    private val api = RetrofitClient.api

    suspend fun getEvents(category: String?): List<Event> = api.getEvents(category)

    suspend fun getEvent(id: Long): Event = api.getEvent(id)

    suspend fun createEvent(request: CreateShowRequest): Event = api.createEvent(request)
}
