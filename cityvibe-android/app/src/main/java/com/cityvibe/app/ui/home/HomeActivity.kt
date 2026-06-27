package com.cityvibe.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.databinding.ActivityHomeBinding
import com.cityvibe.app.ui.details.DetailsActivity
import com.cityvibe.app.util.Resource

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupChips()
        observeViewModel()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadEvents()
        }

        if (savedInstanceState == null) {
            viewModel.loadEvents("All")
        }
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event -> openDetails(event) }
        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = adapter
    }

    private fun setupChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val category = when (checkedIds.first()) {
                binding.chipMusic.id -> "Music"
                binding.chipComedy.id -> "Comedy"
                binding.chipMeetups.id -> "Meetups"
                else -> "All"
            }
            viewModel.loadEvents(category)
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.tvError.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvError.visibility = View.GONE
                    adapter.submitList(state.data)
                    binding.tvEmpty.visibility =
                        if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.tvEmpty.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    private fun openDetails(event: Event) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.id)
        startActivity(intent)
    }
}
