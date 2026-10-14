package com.adadapted.androidadapted.ui.zonelab.scroller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.ui.components.BackButton
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.IconBadge
import com.adadapted.androidadapted.ui.components.InstrumentedAdZone
import com.adadapted.androidadapted.ui.components.PreviewAdZoneHandler
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun ScrollerScreen(viewModel: ScrollerViewModel, onBack: () -> Unit) {
    val rowCount by viewModel.rowCount.collectAsStateWithLifecycle()
    ScrollerScreen(
        rowCount = rowCount,
        zoneHandler = viewModel.zoneHandler,
        onLoadMore = viewModel::loadMore,
        onBack = onBack,
    )
}

/**
 * A feed with a zone every [ScrollerViewModel.AD_INTERVAL] rows. Each ad row reports its own
 * visibility to the SDK. Rows are recycled: a row scrolled far enough away leaves the composition,
 * which stops its zone, and scrolling back starts a fresh one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScrollerScreen(
    rowCount: Int,
    zoneHandler: AdZoneHandler,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
) {
    val listState = rememberLazyListState()
    // Visibility is measured against the list, not the window, so rows under the bars count as hidden.
    val viewport = remember { LayoutBoundsHolder() }

    LoadMoreEffect(listState, onLoadMore)

    Scaffold(
        topBar = {
            TopAppBar(
                colors = appTopBarColors(),
                title = { Text(stringResource(R.string.scenario_scroller)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .layoutBounds(viewport),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = rowCount,
                key = { it },
                contentType = { if (ScrollerViewModel.isAdRow(it)) AD_ROW else CONTENT_ROW },
            ) { index ->
                if (ScrollerViewModel.isAdRow(index)) {
                    InstrumentedAdZone(
                        title = stringResource(R.string.scroller_ad_row, index),
                        zoneId = AdZoneCatalog.SCROLLER,
                        handler = zoneHandler,
                        viewport = viewport,
                    )
                } else {
                    ContentRow(index = index)
                }
            }
        }
    }
}

@Composable
private fun ContentRow(index: Int) {
    ContentCard(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.AutoMirrored.Outlined.Article, AccentColors.cycle(index), size = 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.scroller_content_title, index), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.scroller_content_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Asks for more rows as the end of the list comes into view. */
@Composable
private fun LoadMoreEffect(listState: LazyListState, onLoadMore: () -> Unit) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow false
            lastVisible >= layout.totalItemsCount - LOAD_MORE_THRESHOLD
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { currentOnLoadMore() }
    }
}

private const val AD_ROW = "ad"
private const val CONTENT_ROW = "content"
private const val LOAD_MORE_THRESHOLD = 8

@Preview
@Composable
private fun ScrollerScreenPreview() {
    AndroidAdaptedTheme {
        ScrollerScreen(rowCount = 12, zoneHandler = PreviewAdZoneHandler, onLoadMore = {}, onBack = {})
    }
}
