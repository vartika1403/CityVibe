package com.cityvibe.application.ui.createshow

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cityvibe.application.data.model.CreateShowRequest
import com.cityvibe.application.data.model.Event
import com.cityvibe.application.data.repository.EventRepository
import com.cityvibe.application.util.Formatters
import com.cityvibe.application.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Holds the Create Your Show form. Fields are Compose-observable (same style as
 * [com.cityvibe.application.ui.home.HomeViewModel.isRefreshing]) so they survive rotation.
 *
 * Submitting validates the form and POSTs it to the backend; the result is exposed
 * as [submitState] using the usual Loading / Success / Error wrapper.
 */
class CreateShowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EventRepository()

    var title by mutableStateOf("")
    var category by mutableStateOf(CATEGORIES.first())
    var description by mutableStateOf("")
    var duration by mutableStateOf("")
    var city by mutableStateOf("Bengaluru")
    var price by mutableStateOf("")

    /** Chosen start date + time, or null until the user picks one. */
    var startsAtMillis by mutableStateOf<Long?>(null)
        private set

    /** Cover image picked from the device, or null. Optional — no validation error. */
    var imageUri by mutableStateOf<Uri?>(null)

    /**
     * Hosted cover image URL, typed or pasted. Optional, and takes precedence over
     * [imageUri]: only an http(s) URL is reachable by other devices, since a picked
     * file lives in this app's private storage.
     */
    var imageUrlInput by mutableStateOf("")

    /** Errors stay hidden until the first submit attempt. */
    private var submitAttempted by mutableStateOf(false)

    /** Null until the first submit; then Loading / Success / Error for that request. */
    var submitState by mutableStateOf<Resource<Event>?>(null)
        private set

    val titleError: String? get() = errorIf(title.isBlank(), "Give your show a name")
    val descriptionError: String?
        get() = errorIf(description.isBlank(), "Tell people what the show is about")
    val startsAtError: String? get() = errorIf(startsAtMillis == null, "Pick when the show starts")
    val durationError: String? get() = errorIf(duration.isBlank(), "How long does it run?")
    val cityError: String? get() = errorIf(city.isBlank(), "Which city is it in?")
    val priceError: String? get() = errorIf(price.isBlank(), "Enter a price (0 for a free show)")
    val imageUrlError: String?
        get() = errorIf(
            imageUrlInput.isNotBlank() && !imageUrlInput.trim().startsWith("http", true),
            "Use a full image URL starting with http:// or https://",
        )

    /**
     * What the cover preview renders: a pasted URL wins over a picked file. The URL is
     * only handed to the image loader once a full scheme is typed, so partial input
     * doesn't fire a request per keystroke.
     */
    val coverPreview: Any?
        get() = imageUrlInput.trim()
            .takeIf { it.startsWith("http://", true) || it.startsWith("https://", true) }
            ?: imageUri

    /** Clears both cover sources so the placeholder comes back. */
    fun clearCover() {
        imageUri = null
        imageUrlInput = ""
    }

    /** Human-readable start time for the read-only date field, e.g. "Sat, 18 Jul 2026 · 7:00 PM". */
    val startsAtDisplay: String
        get() = startsAtMillis?.let { displayFormat().format(Date(it)) } ?: ""

    fun setStartsAt(millis: Long) {
        startsAtMillis = millis
    }

    fun submit() {
        submitAttempted = true
        val startsAt = startsAtMillis ?: return
        if (titleError != null || descriptionError != null || durationError != null ||
            cityError != null || priceError != null || imageUrlError != null
        ) {
            return
        }
        if (submitState is Resource.Loading) return

        val pastedUrl = imageUrlInput.trim()
        val pickedCover = imageUri

        submitState = Resource.Loading
        viewModelScope.launch {
            submitState = try {
                // A pasted http(s) URL is the only cover other devices can load. Falling
                // back to a picked file: the picker's content:// grant dies with the
                // process, so copy the bytes into app storage and send that stable URI.
                val coverUrl = pastedUrl.ifBlank {
                    pickedCover?.let { persistCover(it)?.toString() }.orEmpty()
                }.ifBlank { null }
                val request = CreateShowRequest(
                    title = title.trim(),
                    category = category,
                    description = description.trim(),
                    dateTime = Formatters.toIsoDateTime(startsAt),
                    duration = duration.trim(),
                    venueName = city.trim(),
                    price = displayPrice(price),
                    imageUrl = coverUrl,
                )
                Resource.Success(repository.createEvent(request))
            } catch (e: Exception) {
                Resource.Error(errorMessageFor(e))
            }
        }
    }

    /**
     * Copies the picked image into app-private storage and returns a `file://` URI
     * that stays readable across restarts. Returns null if the copy fails.
     */
    private suspend fun persistCover(source: Uri): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            val context = getApplication<Application>()
            val dir = File(context.filesDir, "covers").apply { mkdirs() }
            val file = File(dir, "cover_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(source)!!.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            Uri.fromFile(file)
        }.getOrNull()
    }

    private fun errorMessageFor(e: Exception): String = when {
        e is HttpException && (e.code() == 404 || e.code() == 405) ->
            "This backend doesn't accept new shows yet (HTTP ${e.code()}) — it needs a " +
                "POST /api/events endpoint."
        e is HttpException -> "Couldn't submit your show (HTTP ${e.code()})."
        e is IOException -> "No connection. Check your network and try again."
        else -> e.localizedMessage ?: "Couldn't submit your show. Try again."
    }

    /** Summary of the submitted show, for the confirmation dialog. */
    fun summaryOf(event: Event): String = buildString {
        append("\"${event.title}\" — ${event.category}\n\n")
        append("${Formatters.formatDateTime(event.dateTime)}\n")
        append("${event.duration ?: duration.trim()} · ${event.venueName ?: city.trim()}\n")
        append(event.price ?: displayPrice(price))
    }

    /** Clears a delivered result so it isn't handled again after a config change. */
    fun consumeResult() {
        submitState = null
    }

    private fun errorIf(invalid: Boolean, message: String): String? =
        message.takeIf { submitAttempted && invalid }

    /** "0" -> "Free", otherwise a rupee amount — matches how `Event.price` reads in the feed. */
    private fun displayPrice(raw: String): String {
        val amount = raw.trim().toIntOrNull()
        return when {
            amount == null -> raw.trim()
            amount <= 0 -> "Free"
            else -> "₹$amount"
        }
    }

    companion object {
        /** "What it's about" — mirrors the backend categories used by the home feed. */
        val CATEGORIES = listOf("Music", "Comedy", "Meetup", "Gathering")

        /**
         * Built per call rather than held in a static: Locale.getDefault() read once at class
         * init would keep formatting in the old locale if the user changes theirs mid-session.
         */
        private fun displayFormat() =
            SimpleDateFormat("EEE, dd MMM yyyy · h:mm a", Locale.getDefault())
    }
}
