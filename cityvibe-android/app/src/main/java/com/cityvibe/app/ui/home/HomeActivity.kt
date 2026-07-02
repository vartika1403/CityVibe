package com.cityvibe.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.ui.details.DetailsActivity
import com.cityvibe.app.util.Resource

class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.loadEvents("All")
        }

        setContent {
            val state by viewModel.events.observeAsState(Resource.Loading)

            HomeScreen(
                state = state,
                selectedCategory = viewModel.selectedCategory,
                isRefreshing = viewModel.isRefreshing,
                onCategorySelected = { viewModel.loadEvents(it) },
                onRefresh = { viewModel.refresh() },
                onEventClick = ::openDetails,
            )
        }
    }

    private fun openDetails(event: Event) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.id)
        startActivity(intent)
    }
}
