package com.cityvibe.application.ui.details

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import coil.load
import com.cityvibe.application.R
import com.cityvibe.application.data.model.Event
import com.cityvibe.application.databinding.ActivityDetailsBinding
import com.cityvibe.application.ui.enableCityVibeEdgeToEdge
import com.cityvibe.application.util.Formatters
import com.cityvibe.application.util.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: DetailsViewModel by viewModels()
    private var currentEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableCityVibeEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        padBottomBarForNavigationBar()

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

    /** Keeps the Book Now bar's background under the nav bar but its button above it. */
    private fun padBottomBarForNavigationBar() {
        val bar = binding.bottomBar
        val basePadding = bar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bar) { view, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = basePadding + navBar.bottom)
            insets
        }
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

        binding.ivCover.load(event.imageUrl) {
            placeholder(R.drawable.bg_image_placeholder)
            error(R.drawable.bg_image_placeholder)
            crossfade(true)
        }
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
