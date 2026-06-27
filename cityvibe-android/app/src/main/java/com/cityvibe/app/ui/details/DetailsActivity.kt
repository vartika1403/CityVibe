package com.cityvibe.app.ui.details

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cityvibe.app.R
import com.cityvibe.app.data.model.Event
import com.cityvibe.app.databinding.ActivityDetailsBinding
import com.cityvibe.app.util.Formatters
import com.cityvibe.app.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: DetailsViewModel by viewModels()
    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) {
            finish()
            return
        }

        observeViewModel()
        viewModel.loadEvent(eventId)

        binding.btnBook.setOnClickListener { showBookingConfirmation() }
    }

    private fun observeViewModel() {
        viewModel.event.observe(this) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentGroup.visibility = View.GONE
                    binding.bottomBar.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentGroup.visibility = View.VISIBLE
                    binding.bottomBar.visibility = View.VISIBLE
                    bind(state.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }
    }

    private fun bind(event: Event) {
        currentEvent = event
        binding.collapsingToolbar.title = event.title
        binding.tvTitle.text = event.title
        binding.tvDescription.text = event.description ?: ""
        binding.tvDateTime.text = Formatters.formatDateTime(event.dateTime)
        binding.tvDuration.text = event.duration ?: "-"
        binding.tvVenueName.text = event.venueName ?: ""
        binding.tvVenueAddress.text = event.venueAddress ?: ""
        binding.tvOrganizer.text = event.organizer ?: ""
        binding.tvPrice.text = event.price ?: ""

        binding.tvCategory.text = event.category
        binding.tvCategory.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Formatters.categoryColor(event.category))

        Glide.with(this)
            .load(event.imageUrl)
            .placeholder(R.drawable.bg_image_placeholder)
            .error(R.drawable.bg_image_placeholder)
            .centerCrop()
            .into(binding.ivCover)
    }

    private fun showBookingConfirmation() {
        val event = currentEvent ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Booking Confirmed 🎉")
            .setMessage("You're all set for \"${event.title}\".\n\nA confirmation has been reserved for ${event.venueName}.")
            .setPositiveButton("Done", null)
            .show()
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}
