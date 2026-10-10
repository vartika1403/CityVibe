package com.cityvibe.application.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityvibe.application.data.model.Event
import com.cityvibe.application.data.repository.EventRepository
import com.cityvibe.application.util.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>> = _events

    var selectedCategory: String = "All"
        private set

    /** Compose-observable: true while a pull-to-refresh is in flight. */
    var isRefreshing by mutableStateOf(false)
        private set

    /** Re-fetch the current category as a pull-to-refresh (shows the swipe spinner). */
    fun refresh() = loadEvents(selectedCategory, refreshing = true)

    fun loadEvents(category: String = selectedCategory, refreshing: Boolean = false) {
        selectedCategory = category
        if (refreshing) isRefreshing = true else _events.value = Resource.Loading
        viewModelScope.launch {
            try {
                // "All" -> no filter. UI label "Meetups" maps to backend category "Meetup".
                val query = when (category) {
                    "All" -> null
                    "Meetups" -> "Meetup"
                    else -> category
                }
                _events.value = Resource.Success(repository.getEvents(query))
            } catch (e: Exception) {
                _events.value = Resource.Error(e.localizedMessage ?: "Unable to load events")
            } finally {
                isRefreshing = false
            }
        }
    }
}
