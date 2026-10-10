package com.cityvibe.application.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.cityvibe.application.R
import com.cityvibe.application.data.model.Event
import com.cityvibe.application.ui.theme.CityVibeColors
import com.cityvibe.application.util.Formatters
import com.cityvibe.application.util.Resource

/** UI label -> shown on the chip. Order matches activity_home.xml. */
private val CATEGORIES = listOf("All", "Music", "Comedy", "Meetups")

/**
 * Compose equivalent of activity_home.xml: gradient header, single-select
 * category filter chips, and a pull-to-refresh feed with loading / error /
 * empty states, with a compact "Create Show" button in the header. Stateless — all state is hoisted to the caller.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    state: Resource<List<Event>>,
    selectedCategory: String,
    isRefreshing: Boolean,
    onCategorySelected: (String) -> Unit,
    onRefresh: () -> Unit,
    onEventClick: (Event) -> Unit,
    onCreateShowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CityVibeColors.Bg)
    ) {
        Header(onCreateShowClick = onCreateShowClick)

        CategoryChips(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
        )

        val pullState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullState)
        ) {
            when (state) {
                is Resource.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(
                            color = CityVibeColors.Brand,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is Resource.Error -> CenteredMessage(state.message)

                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        CenteredMessage(stringResource(R.string.empty_events))
                    } else {
                        EventFeed(events = state.data, onEventClick = onEventClick)
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = CityVibeColors.Brand,
            )
        }
    }
}

@Composable
private fun Header(onCreateShowClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(CityVibeColors.BrandAmber, CityVibeColors.BrandDark)
                )
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.app_name),
                color = CityVibeColors.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            CreateShowButton(onClick = onCreateShowClick)
        }
        Text(
            text = stringResource(R.string.city_header),
            color = Color(0xFFFFE9E9),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = stringResource(R.string.tagline),
            color = Color(0xCCFFFFFF),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Small pill that sizes to its label, so it sits beside the title without crowding it. */
@Composable
private fun CreateShowButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CityVibeColors.White)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null,
            tint = CityVibeColors.BrandDark,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.create_show),
            color = CityVibeColors.BrandDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CityVibeColors.Surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CATEGORIES.forEach { label ->
            val selected = label == selectedCategory
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(label) },
                label = {
                    Text(label, fontWeight = FontWeight.Bold)
                },
                shape = RoundedCornerShape(22.dp),
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CityVibeColors.Surface,
                    labelColor = CityVibeColors.TextSecondary,
                    selectedContainerColor = CityVibeColors.Brand,
                    selectedLabelColor = CityVibeColors.White,
                ),
            )
        }
    }
}

@Composable
private fun EventFeed(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Bottom padding includes the navigation bar so the last card can scroll clear of it.
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(event = event, onClick = { onEventClick(event) })
        }
    }
}

/** Compose equivalent of item_event.xml. */
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CityVibeColors.Surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                // Compose equivalent of the bg_image_placeholder shape gradient,
                // shown behind the image while it loads or if it fails.
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFE0E0EA), Color(0xFFCFCFDD))
                    )
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.cd_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Category tag (top start)
            Text(
                text = event.category,
                color = CityVibeColors.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CityVibeColors.categoryColor(event.category))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )

            // Price tag (top end)
            event.price?.takeIf { it.isNotBlank() }?.let { price ->
                Text(
                    text = price,
                    color = CityVibeColors.BrandDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE6FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                color = CityVibeColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            IconLabelRow(
                iconRes = R.drawable.ic_calendar,
                text = Formatters.formatDateTime(event.dateTime),
                topPadding = 10.dp,
            )
            IconLabelRow(
                iconRes = R.drawable.ic_location,
                text = event.venueName ?: "",
                topPadding = 6.dp,
                singleLine = true,
            )
        }
    }
}

@Composable
private fun IconLabelRow(
    iconRes: Int,
    text: String,
    topPadding: androidx.compose.ui.unit.Dp,
    singleLine: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(top = topPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = CityVibeColors.TextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = CityVibeColors.TextSecondary,
            fontSize = 13.sp,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.CenteredMessage(message: String) {
    Text(
        text = message,
        color = CityVibeColors.TextSecondary,
        fontSize = 15.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 32.dp),
    )
}
