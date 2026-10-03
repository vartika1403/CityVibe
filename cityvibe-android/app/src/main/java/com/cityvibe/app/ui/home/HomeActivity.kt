package com.cityvibe.app.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.ui.createshow.CreateShowActivity
import com.cityvibe.app.ui.details.DetailsActivity
import com.cityvibe.app.util.Resource

class HomeActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    /** Refreshes the feed when a show was submitted, so it appears right away. */
    private val createShow =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) viewModel.refresh()
        }

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
                onCreateShowClick = {
                    createShow.launch(Intent(this, CreateShowActivity::class.java))
                },
            )
        }
    }

    private fun openDetails(event: Event) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.id)
        startActivity(intent)
    }
}
