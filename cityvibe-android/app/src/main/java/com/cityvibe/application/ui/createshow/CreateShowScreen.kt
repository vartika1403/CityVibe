package com.cityvibe.application.ui.createshow

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cityvibe.application.R
import com.cityvibe.application.ui.theme.CityVibeColors
import com.cityvibe.application.util.Resource
import java.util.Calendar

private val FieldBorder = Color(0xFFDDDDE5)

/**
 * Form for hosting a new show: name, what it's about, description, date & time,
 * duration, city and price, with a sticky submit bar. State lives in
 * [CreateShowViewModel]; submitting shows a local confirmation dialog.
 */
@Composable
fun CreateShowScreen(
    viewModel: CreateShowViewModel,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // System photo picker — no storage permission needed, and it falls back to
    // ACTION_OPEN_DOCUMENT on devices without the Android 13+ picker.
    val pickImage = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.imageUri = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityVibeColors.Bg)
            .imePadding()
    ) {
        Header(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            CoverImagePicker(
                model = viewModel.coverPreview,
                onPick = { pickImage.launch(PickVisualMediaRequest(ImageOnly)) },
                onClear = viewModel::clearCover,
            )

            FormField(
                label = stringResource(R.string.label_image_url),
                value = viewModel.imageUrlInput,
                onValueChange = { viewModel.imageUrlInput = it },
                error = viewModel.imageUrlError,
                placeholder = stringResource(R.string.hint_image_url),
                keyboardType = KeyboardType.Uri,
            )

            FormField(
                label = stringResource(R.string.label_show_name),
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                error = viewModel.titleError,
            )

            SectionLabel(stringResource(R.string.label_about))
            CategoryChips(
                selected = viewModel.category,
                onSelected = { viewModel.category = it },
            )

            FormField(
                label = stringResource(R.string.label_description),
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                error = viewModel.descriptionError,
                placeholder = stringResource(R.string.hint_description),
                singleLine = false,
                minLines = 4,
                imeAction = ImeAction.Default,
            )

            DateTimeField(
                value = viewModel.startsAtDisplay,
                error = viewModel.startsAtError,
                onClick = {
                    pickDateTime(context, viewModel.startsAtMillis, viewModel::setStartsAt)
                },
            )

            FormField(
                label = stringResource(R.string.label_duration),
                value = viewModel.duration,
                onValueChange = { viewModel.duration = it },
                error = viewModel.durationError,
                placeholder = stringResource(R.string.hint_duration),
            )

            FormField(
                label = stringResource(R.string.label_city),
                value = viewModel.city,
                onValueChange = { viewModel.city = it },
                error = viewModel.cityError,
            )

            FormField(
                label = stringResource(R.string.label_price),
                value = viewModel.price,
                onValueChange = { input -> viewModel.price = input.filter { it.isDigit() } },
                error = viewModel.priceError,
                placeholder = stringResource(R.string.hint_price),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            )
        }

        val state = viewModel.submitState
        SubmitBar(
            isSubmitting = state is Resource.Loading,
            errorMessage = (state as? Resource.Error)?.message,
            onSubmit = viewModel::submit,
        )
    }

    (viewModel.submitState as? Resource.Success)?.let { success ->
        val dismiss = {
            viewModel.consumeResult()
            onSubmitted()
        }
        AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(R.string.show_submitted_title)) },
            text = { Text(viewModel.summaryOf(success.data)) },
            confirmButton = {
                TextButton(onClick = dismiss) {
                    Text("Done", color = CityVibeColors.Brand, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CityVibeColors.Surface,
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(CityVibeColors.BrandAmber, CityVibeColors.BrandDark)
                )
            )
            .statusBarsPadding()
            .padding(start = 8.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.cd_back),
                tint = CityVibeColors.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = stringResource(R.string.create_your_show),
                color = CityVibeColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.create_show_subtitle),
                color = Color(0xCCFFFFFF),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * Tap-to-pick cover image. Shows a dashed placeholder until an image is chosen,
 * then a preview with "Change" / "Remove" actions.
 */
@Composable
private fun CoverImagePicker(
    model: Any?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CityVibeColors.Surface)
                .border(
                    width = 1.dp,
                    color = if (model == null) FieldBorder else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center,
        ) {
            if (model == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        tint = CityVibeColors.TextSecondary,
                        modifier = Modifier.size(28.dp),
                    )
                    Text(
                        text = stringResource(R.string.add_cover_image),
                        color = CityVibeColors.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.add_cover_image_hint),
                        color = CityVibeColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            } else {
                AsyncImage(
                    model = model,
                    contentDescription = stringResource(R.string.cd_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (model != null) {
            Row(modifier = Modifier.padding(top = 6.dp)) {
                TextButton(onClick = onPick) {
                    Text(
                        text = stringResource(R.string.change_image),
                        color = CityVibeColors.Brand,
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.remove_image),
                        color = CityVibeColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = CityVibeColors.TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
private fun CategoryChips(
    selected: String,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CreateShowViewModel.CATEGORIES.forEach { label ->
            FilterChip(
                selected = label == selected,
                onClick = { onSelected(label) },
                label = { Text(label, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(22.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CityVibeColors.Surface,
                    labelColor = CityVibeColors.TextSecondary,
                    selectedContainerColor = CityVibeColors.categoryColor(label),
                    selectedLabelColor = CityVibeColors.White,
                ),
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it, color = CityVibeColors.TextSecondary) } },
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Read-only field that opens the date + time pickers when tapped. */
@Composable
private fun DateTimeField(
    value: String,
    error: String?,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_time)) },
                placeholder = {
                    Text(stringResource(R.string.hint_time), color = CityVibeColors.TextSecondary)
                },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = CityVibeColors.TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            // Read-only field: this overlay takes the tap so no keyboard opens.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}

@Composable
private fun SubmitBar(
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
) {
    Surface(color = CityVibeColors.Surface, shadowElevation = 12.dp) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = CityVibeColors.BrandDark,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CityVibeColors.Brand,
                    contentColor = CityVibeColors.White,
                    disabledContainerColor = CityVibeColors.Brand.copy(alpha = 0.6f),
                    disabledContentColor = CityVibeColors.White,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = CityVibeColors.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.submit_show),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CityVibeColors.Brand,
    unfocusedBorderColor = FieldBorder,
    focusedLabelColor = CityVibeColors.Brand,
    unfocusedLabelColor = CityVibeColors.TextSecondary,
    focusedTextColor = CityVibeColors.TextPrimary,
    unfocusedTextColor = CityVibeColors.TextPrimary,
    cursorColor = CityVibeColors.Brand,
    focusedContainerColor = CityVibeColors.Surface,
    unfocusedContainerColor = CityVibeColors.Surface,
    errorContainerColor = CityVibeColors.Surface,
)

/** Date picker followed by a time picker; reports the combined instant. */
private fun pickDateTime(context: Context, initialMillis: Long?, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply {
        if (initialMillis != null) timeInMillis = initialMillis
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(year, month, dayOfMonth, hour, minute, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onPicked(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false,
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1_000
    }.show()
}
