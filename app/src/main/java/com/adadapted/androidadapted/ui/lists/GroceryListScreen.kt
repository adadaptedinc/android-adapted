package com.adadapted.androidadapted.ui.lists

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdZone
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.KeywordSuggestion
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.ui.components.BackButton
import com.adadapted.androidadapted.ui.components.EmptyState
import com.adadapted.androidadapted.ui.components.ListRowSpacing
import com.adadapted.androidadapted.ui.components.OverflowMenu
import com.adadapted.androidadapted.ui.components.PreviewAdZoneHandler
import com.adadapted.androidadapted.ui.components.SectionHeader
import com.adadapted.androidadapted.ui.components.SectionSpacing
import com.adadapted.androidadapted.ui.components.cardBorder
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors

@Composable
fun GroceryListScreen(viewModel: GroceryListViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroceryListScreen(
        uiState = uiState,
        draft = { viewModel.draft },
        zoneHandler = viewModel.zoneHandler,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroceryListScreen(
    uiState: GroceryListUiState,
    // Read lazily, so typing recomposes only the field and not the whole screen.
    draft: () -> String,
    zoneHandler: AdZoneHandler,
    onAction: (GroceryListAction) -> Unit,
    onBack: () -> Unit,
) {
    val list = uiState.list
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = appTopBarColors(),
                title = { Text(list?.name.orEmpty()) },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    if (list != null) {
                        ListOptionsMenu(
                            isRecipeContextEnabled = uiState.isRecipeContextEnabled,
                            canClearCrossedOff = list.crossedOffItems.isNotEmpty(),
                            onAction = onAction,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            if (list != null) {
                AddItemBar(draft = draft, onAction = onAction)
            }
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.content) {
                    ListContent.Loading -> Unit
                    ListContent.Missing -> EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.list_missing_title),
                        message = stringResource(R.string.list_missing_message),
                    )
                    is ListContent.Ready -> if (uiState.content.list.items.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.ShoppingCart,
                            title = stringResource(R.string.list_empty_title),
                            message = stringResource(R.string.list_empty_message),
                        )
                    } else {
                        ItemList(list = uiState.content.list, onAction = onAction)
                    }
                }
                // Drops down over the list, so the list doesn't jump while typing.
                if (uiState.suggestions.isNotEmpty()) {
                    SuggestionDropdown(
                        suggestions = uiState.suggestions,
                        onShown = { onAction(GroceryListAction.SuggestionShown(it)) },
                        onSelect = { onAction(GroceryListAction.SuggestionSelected(it)) },
                    )
                }
            }
            // Inside the ime-padded column rather than the bottom bar, so it stays above the keyboard.
            if (list != null) {
                ListAdZone(list = list, uiState = uiState, zoneHandler = zoneHandler)
            }
        }
    }

    uiState.editingItem?.let { item ->
        ItemEditorSheet(
            item = item,
            onSave = { onAction(GroceryListAction.SaveEdits(it)) },
            onDelete = { onAction(GroceryListAction.DeleteItem(item)) },
            onDismiss = { onAction(GroceryListAction.DismissEditor) },
        )
    }
}

@Composable
private fun ItemList(list: GroceryList, onAction: (GroceryListAction) -> Unit) {
    val (remaining, crossedOff) = remember(list.items) { list.items.partition { !it.isCrossedOff } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ListRowSpacing),
    ) {
        section(R.string.section_to_buy, "remaining", remaining, onAction)
        section(R.string.section_crossed_off, "crossed-off", crossedOff, onAction)
        item(key = "hint") {
            Text(
                text = stringResource(R.string.item_gesture_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SectionSpacing)
                    .animateItem(),
            )
        }
    }
}

/** A titled group of rows. Rows keep their key across groups, so crossing off animates the move. */
private fun LazyListScope.section(
    @StringRes title: Int,
    key: String,
    items: List<GroceryItem>,
    onAction: (GroceryListAction) -> Unit,
) {
    if (items.isEmpty()) return
    item(key = "header-$key") {
        SectionHeader(stringResource(title), Modifier.animateItem(), count = items.size)
    }
    items(items, key = { it.id }) { item ->
        GroceryItemRow(
            item = item,
            onToggleCrossedOff = { onAction(GroceryListAction.ToggleCrossedOff(item)) },
            onEdit = { onAction(GroceryListAction.EditItem(item)) },
            onDelete = { onAction(GroceryListAction.DeleteItem(item)) },
            modifier = Modifier.animateItem(),
        )
    }
}

/** The search-or-add field, pinned above the list. */
@Composable
private fun AddItemBar(draft: () -> String, onAction: (GroceryListAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = draft(),
            onValueChange = { onAction(GroceryListAction.DraftChanged(it)) },
            placeholder = { Text(stringResource(R.string.add_item_placeholder)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAction(GroceryListAction.AddDraft) }),
            modifier = Modifier
                .weight(1f)
                .border(cardBorder(), CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = { onAction(GroceryListAction.AddDraft) },
            enabled = draft().isNotBlank(),
            modifier = Modifier.size(56.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
        }
    }
}

/** Keyword suggestions, dropped down over the list. Each result set reports what it showed. */
@Composable
private fun SuggestionDropdown(
    suggestions: List<KeywordSuggestion>,
    onShown: (KeywordSuggestion) -> Unit,
    onSelect: (KeywordSuggestion) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
            items(suggestions, key = KeywordSuggestion::termId) { suggestion ->
                LaunchedEffect(suggestion) { onShown(suggestion) }
                ListItem(
                    headlineContent = { Text(suggestion.name) },
                    leadingContent = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onSelect(suggestion) },
                )
            }
        }
    }
}

/** The list's ad zone, pinned below the list. */
@Composable
private fun ListAdZone(list: GroceryList, uiState: GroceryListUiState, zoneHandler: AdZoneHandler) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0), bottomEnd = CornerSize(0)),
    ) {
        Column {
            Text(
                text = if (uiState.isRecipeContextEnabled) {
                    stringResource(R.string.list_zone_caption_with_context, list.zoneId, uiState.zoneContextId)
                } else {
                    stringResource(R.string.list_zone_caption, list.zoneId)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 2.dp),
            )
            // A new key recreates the zone, which is the only way to request a fresh ad.
            key(uiState.zoneReloadCount) {
                AdZone(zoneId = list.zoneId, handler = zoneHandler, contextId = uiState.zoneContextId)
            }
        }
    }
}

@Composable
private fun ListOptionsMenu(
    isRecipeContextEnabled: Boolean,
    canClearCrossedOff: Boolean,
    onAction: (GroceryListAction) -> Unit,
) {
    OverflowMenu(contentDescription = stringResource(R.string.list_options_menu)) {
        MenuItem(
            text = stringResource(R.string.recipe_context),
            icon = Icons.Outlined.RestaurantMenu,
            onClick = { onAction(GroceryListAction.SetRecipeContext(!isRecipeContextEnabled)) },
            trailingIcon = { Checkbox(checked = isRecipeContextEnabled, onCheckedChange = null) },
        )
        MenuItem(stringResource(R.string.reload_ad_zone), Icons.Outlined.Refresh, onClick = { onAction(GroceryListAction.ReloadZone) })
        if (canClearCrossedOff) {
            HorizontalDivider()
            MenuItem(
                text = stringResource(R.string.clear_crossed_off),
                icon = Icons.Outlined.DeleteSweep,
                onClick = { onAction(GroceryListAction.ClearCrossedOff) },
                isDestructive = true,
            )
        }
    }
}

@Preview
@Composable
private fun GroceryListScreenPreview() {
    AndroidAdaptedTheme {
        GroceryListScreen(
            uiState = GroceryListUiState(
                content = ListContent.Ready(
                    GroceryList(
                        name = "Groceries",
                        icon = ListIcon.PRODUCE,
                        zoneId = AdZoneCatalog.LIST,
                        items = listOf(
                            GroceryItem(name = "Oranges", quantity = 4),
                            GroceryItem(name = "Granola Bars", origin = ItemOrigin.AD),
                            GroceryItem(name = "Milk", isCrossedOff = true),
                        ),
                    ),
                ),
                suggestions = listOf(KeywordSuggestion("1", "Mighty Milk", matchId = 1), KeywordSuggestion("2", "Milk Duds", matchId = 1)),
            ),
            draft = { "Mil" },
            zoneHandler = PreviewAdZoneHandler,
            onAction = {},
            onBack = {},
        )
    }
}
