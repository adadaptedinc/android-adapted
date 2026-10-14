package com.adadapted.androidadapted.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.EmptyState
import com.adadapted.androidadapted.ui.components.EmptyStateAction
import com.adadapted.androidadapted.ui.components.IconBadge
import com.adadapted.androidadapted.ui.components.ListRowSpacing
import com.adadapted.androidadapted.ui.components.MatteCard
import com.adadapted.androidadapted.ui.components.OverflowMenu
import com.adadapted.androidadapted.ui.components.SectionSpacing
import com.adadapted.androidadapted.ui.components.wash
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors

@Composable
fun ListsScreen(viewModel: ListsViewModel, onOpenList: (listId: String) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ListsScreen(
        uiState = uiState,
        onOpenList = onOpenList,
        onAddList = viewModel::addList,
        onDeleteList = viewModel::deleteList,
        onMoveList = viewModel::moveList,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsScreen(
    uiState: ListsUiState,
    onOpenList: (listId: String) -> Unit,
    onAddList: (NewListDraft) -> Unit,
    onDeleteList: (listId: String) -> Unit,
    onMoveList: (listId: String, toIndex: Int) -> Unit,
) {
    var isAddingList by rememberSaveable { mutableStateOf(false) }
    var listIdPendingDeletion by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lists = (uiState as? ListsUiState.Ready)?.lists.orEmpty()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.lists_title)) },
                colors = appTopBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (lists.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { isAddingList = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.new_list)) },
                )
            }
        },
    ) { padding ->
        when {
            uiState is ListsUiState.Loading -> Unit
            lists.isEmpty() -> EmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = stringResource(R.string.lists_empty_title),
                message = stringResource(R.string.lists_empty_message),
                action = EmptyStateAction(stringResource(R.string.new_list)) { isAddingList = true },
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + FabClearance,
                ),
                verticalArrangement = Arrangement.spacedBy(ListRowSpacing),
            ) {
                item(key = "summary") {
                    SummaryCard(lists, Modifier.padding(bottom = SectionSpacing))
                }
                itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
                    ListRow(
                        list = list,
                        onClick = { onOpenList(list.id) },
                        onMoveUp = if (index > 0) ({ onMoveList(list.id, index - 1) }) else null,
                        onMoveDown = if (index < lists.lastIndex) ({ onMoveList(list.id, index + 1) }) else null,
                        onDelete = { listIdPendingDeletion = list.id },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (isAddingList) {
        NewListSheet(
            onDismiss = { isAddingList = false },
            onAdd = { draft ->
                onAddList(draft)
                isAddingList = false
            },
        )
    }

    lists.firstOrNull { it.id == listIdPendingDeletion }?.let { list ->
        DeleteListDialog(
            list = list,
            onConfirm = {
                onDeleteList(list.id)
                listIdPendingDeletion = null
            },
            onDismiss = { listIdPendingDeletion = null },
        )
    }
}

@Composable
private fun ListRow(
    list: GroceryList,
    onClick: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = list.icon.accent
    val crossedOff = list.items.count { it.isCrossedOff }
    ContentCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(list.icon.imageVector, accent, size = 48.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(list.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listSubtitle(list.items.size, crossedOff),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (list.items.isNotEmpty()) {
                    val progress = crossedOff.toFloat() / list.items.size
                    LinearProgressIndicator(
                        progress = { progress },
                        color = accent,
                        trackColor = accent.wash(),
                        drawStopIndicator = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, end = 8.dp),
                    )
                }
            }
            ListRowMenu(listName = list.name, onMoveUp = onMoveUp, onMoveDown = onMoveDown, onDelete = onDelete)
        }
    }
}

/** How much is left to buy across every list, on a splash of brand color. */
@Composable
private fun SummaryCard(lists: List<GroceryList>, modifier: Modifier = Modifier) {
    val remaining = lists.sumOf { list -> list.items.count { !it.isCrossedOff } }
    MatteCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = pluralStringResource(R.plurals.summary_items_to_buy, remaining, remaining),
                style = MaterialTheme.typography.headlineMedium,
                color = AccentColors.Mint,
            )
            Text(
                text = pluralStringResource(R.plurals.summary_across_lists, lists.size, lists.size),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalContentColor.current.copy(alpha = 0.85f),
            )
        }
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = AccentColors.Mint.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .size(64.dp),
        )
    }
}

@Composable
private fun ListRowMenu(
    listName: String,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    OverflowMenu(contentDescription = stringResource(R.string.list_options, listName)) {
        MenuItem(stringResource(R.string.move_up), Icons.Outlined.ArrowUpward, onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null)
        MenuItem(stringResource(R.string.move_down), Icons.Outlined.ArrowDownward, onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null)
        MenuItem(stringResource(R.string.delete), Icons.Outlined.Delete, onClick = onDelete, isDestructive = true)
    }
}

@Composable
private fun listSubtitle(itemCount: Int, crossedOff: Int): String {
    if (itemCount == 0) return stringResource(R.string.list_subtitle_empty)

    val remaining = itemCount - crossedOff
    val remainingCount = pluralStringResource(R.plurals.item_count, remaining, remaining)
    return if (crossedOff == 0) remainingCount else stringResource(R.string.list_subtitle_progress, remainingCount, crossedOff)
}

@Composable
private fun DeleteListDialog(list: GroceryList, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val itemCount = list.items.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_list_title, list.name)) },
        text = {
            Text(
                if (itemCount == 0) {
                    stringResource(R.string.delete_list_message_empty)
                } else {
                    pluralStringResource(R.plurals.delete_list_message, itemCount, itemCount)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

/** Keeps the last row clear of the floating action button. */
private val FabClearance = 88.dp

@Preview
@Composable
private fun ListsScreenPreview() {
    AndroidAdaptedTheme {
        ListsScreen(
            uiState = ListsUiState.Ready(
                listOf(
                    GroceryList(
                        name = "Groceries",
                        icon = ListIcon.PRODUCE,
                        zoneId = "102110",
                        items = listOf(
                            GroceryItem(name = "Eggs"),
                            GroceryItem(name = "Milk", isCrossedOff = true),
                        ),
                    ),
                    GroceryList(name = "Household", icon = ListIcon.HOUSEHOLD, zoneId = "102110"),
                ),
            ),
            onOpenList = {},
            onAddList = {},
            onDeleteList = {},
            onMoveList = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun ListsScreenEmptyPreview() {
    AndroidAdaptedTheme {
        ListsScreen(ListsUiState.Ready(emptyList()), {}, {}, {}, { _, _ -> })
    }
}
