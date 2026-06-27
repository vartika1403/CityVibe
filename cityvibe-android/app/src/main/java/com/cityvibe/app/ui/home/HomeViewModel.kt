package com.cityvibe.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.data.repository.EventRepository
import com.cityvibe.app.util.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>> = _events

    var selectedCategory: String = "All"
        private set

    fun loadEvents(category: String = selectedCategory) {
        selectedCategory = category
        _events.value = Resource.Loading
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
            }
        }
    }
}
