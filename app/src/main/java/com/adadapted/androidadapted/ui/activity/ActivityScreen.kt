package com.adadapted.androidadapted.ui.activity

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.AdUnits
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityEntry
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.EmptyState
import com.adadapted.androidadapted.ui.components.IconBadge
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ActivityScreen(
        uiState = uiState,
        onFilterChange = viewModel::setFilter,
        onShare = { context.shareTranscript(viewModel.transcript()) },
        onClear = viewModel::clear,
    )
}

/** A live tail of every SDK callback and every list event the app reported. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActivityScreen(
    uiState: ActivityUiState,
    onFilterChange: (ActivityCategory?) -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Follow new entries while the newest is in view, but don't yank someone reading further down.
    val newestId = uiState.entries.firstOrNull()?.id
    LaunchedEffect(newestId) {
        if (listState.firstVisibleItemIndex <= 1) listState.scrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = appTopBarColors(),
                title = { Text(stringResource(R.string.tab_activity)) },
                actions = {
                    IconButton(onClick = onShare, enabled = uiState.hasEntries) {
                        Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.activity_share))
                    }
                    IconButton(onClick = onClear, enabled = uiState.hasEntries) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = stringResource(R.string.activity_clear))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FilterChips(selected = uiState.filter, onSelect = onFilterChange)
            if (uiState.entries.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.MonitorHeart,
                    title = stringResource(
                        if (uiState.hasEntries) R.string.activity_filtered_empty_title else R.string.activity_empty_title,
                    ),
                    message = stringResource(R.string.activity_empty_message),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(uiState.entries, key = ActivityEntry::id) { entry ->
                        ActivityRow(entry, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChips(selected: ActivityCategory?, onSelect: (ActivityCategory?) -> Unit) {
    val choices = listOf<ActivityCategory?>(null) + ActivityCategory.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(choices) { category ->
            val isSelected = category == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(category) },
                label = { Text(category?.label ?: stringResource(R.string.activity_filter_all)) },
                leadingIcon = category?.let {
                    {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = null,
                            tint = if (isSelected) LocalContentColor.current else it.tint(),
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry, modifier: Modifier = Modifier) {
    ContentCard(shape = MaterialTheme.shapes.large, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconBadge(
                icon = entry.category.icon,
                color = entry.category.tint(),
                size = 36.dp,
                shape = CircleShape,
                modifier = Modifier.semantics { contentDescription = entry.category.label },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.message, style = MaterialTheme.typography.bodyLarge)
                entry.detail?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = TimeFormatter.format(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private val ActivityCategory.icon: ImageVector
    get() = when (this) {
        ActivityCategory.SESSION -> Icons.Outlined.Bolt
        ActivityCategory.ZONE -> Icons.Outlined.AdUnits
        ActivityCategory.CONTENT -> Icons.Outlined.MoveToInbox
        ActivityCategory.LIST -> Icons.Outlined.Checklist
        ActivityCategory.KEYWORD -> Icons.AutoMirrored.Outlined.ManageSearch
        ActivityCategory.ERROR -> Icons.Outlined.WarningAmber
    }

@Composable
private fun ActivityCategory.tint(): Color = when (this) {
    ActivityCategory.SESSION -> AccentColors.Mint
    ActivityCategory.ZONE -> AccentColors.Sky
    ActivityCategory.CONTENT -> AccentColors.Seafoam
    ActivityCategory.LIST -> AccentColors.Jade
    ActivityCategory.KEYWORD -> AccentColors.Lime
    ActivityCategory.ERROR -> MaterialTheme.colorScheme.error
}

private val TimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

private fun Context.shareTranscript(transcript: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.activity_share_subject))
        putExtra(Intent.EXTRA_TEXT, transcript)
    }
    startActivity(Intent.createChooser(send, getString(R.string.activity_share)))
}

@Preview
@Composable
private fun ActivityScreenPreview() {
    val now = Instant.now()
    AndroidAdaptedTheme {
        ActivityScreen(
            uiState = remember {
                ActivityUiState(
                    entries = listOf(
                        ActivityEntry(3, now, ActivityCategory.LIST, "Added \"Oranges\"", "Groceries"),
                        ActivityEntry(2, now, ActivityCategory.ZONE, "Ad loaded", "Zone 102110"),
                        ActivityEntry(1, now, ActivityCategory.SESSION, "SDK started", "Development · app 7D58810X6333241C"),
                    ),
                    hasEntries = true,
                )
            },
            onFilterChange = {},
            onShare = {},
            onClear = {},
        )
    }
}
