package com.cityvibe.app.data.remote

import com.cityvibe.app.data.model.Event
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/events")
    suspend fun getEvents(@Query("category") category: String? = null): List<Event>

    @GET("api/events/{id}")
    suspend fun getEvent(@Path("id") id: Long): Event
}
