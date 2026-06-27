package com.cityvibe.app.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.data.repository.EventRepository
import com.cityvibe.app.util.Resource
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _event = MutableLiveData<Resource<Event>>()
    val event: LiveData<Resource<Event>> = _event

    fun loadEvent(id: Long) {
        _event.value = Resource.Loading
        viewModelScope.launch {
            try {
                _event.value = Resource.Success(repository.getEvent(id))
            } catch (e: Exception) {
                _event.value = Resource.Error(e.localizedMessage ?: "Unable to load event")
            }
        }
    }
}
