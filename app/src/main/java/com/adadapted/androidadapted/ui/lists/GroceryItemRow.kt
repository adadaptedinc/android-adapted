package com.adadapted.androidadapted.ui.lists

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.ListRowSpacing
import com.adadapted.androidadapted.ui.components.Pill
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme

/** Tap to cross off or back on, long-press to edit, swipe toward the start to delete. */
@Composable
internal fun GroceryItemRow(
    item: GroceryItem,
    onToggleCrossedOff: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState()
    val toggleLabel = stringResource(if (item.isCrossedOff) R.string.item_uncross_action else R.string.item_cross_off_action)
    val editLabel = stringResource(R.string.item_edit)
    val deleteLabel = stringResource(R.string.delete)
    val state = stringResource(if (item.isCrossedOff) R.string.item_state_crossed_off else R.string.item_state_to_buy)

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = { onDelete() },
        backgroundContent = { DeleteBackground(isActive = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) },
        modifier = modifier,
    ) {
        ContentCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClickLabel = toggleLabel,
                        onClick = {
                            haptics.performHapticFeedback(
                                if (item.isCrossedOff) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn,
                            )
                            onToggleCrossedOff()
                        },
                        onLongClickLabel = editLabel,
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEdit()
                        },
                    )
                    .semantics {
                        stateDescription = state
                        customActions = listOf(CustomAccessibilityAction(deleteLabel) { onDelete(); true })
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CheckCircle(isChecked = item.isCrossedOff)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val textColor by animateColorAsState(
                        if (item.isCrossedOff) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        label = "itemText",
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        textDecoration = if (item.isCrossedOff) TextDecoration.LineThrough else null,
                    )
                    item.origin.badge?.let { badge ->
                        Pill(text = stringResource(badge.label), color = badge.color, icon = badge.icon)
                    }
                }
                if (item.quantity > 1) {
                    Pill(
                        text = stringResource(R.string.item_quantity, item.quantity),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckCircle(isChecked: Boolean) {
    val fill by animateColorAsState(
        if (isChecked) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "checkFill",
    )
    val outline by animateColorAsState(
        if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "checkOutline",
    )
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(fill, CircleShape)
            .border(2.dp, outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = isChecked, enter = scaleIn(), exit = scaleOut()) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun DeleteBackground(isActive: Boolean) {
    val color by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
        label = "deleteBackground",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, MaterialTheme.shapes.medium)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (isActive) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

internal data class OriginBadge(@StringRes val label: Int, val icon: ImageVector, val color: Color)

/** How the item's origin is shown, or null for items the user typed. */
internal val ItemOrigin.badge: OriginBadge?
    get() = when (this) {
        ItemOrigin.MANUAL -> null
        ItemOrigin.SUGGESTION -> OriginBadge(R.string.origin_suggestion, Icons.AutoMirrored.Outlined.ManageSearch, AccentColors.Sky)
        ItemOrigin.AD -> OriginBadge(R.string.origin_ad, Icons.Outlined.Campaign, AccentColors.Cocoa)
        ItemOrigin.PAYLOAD -> OriginBadge(R.string.origin_payload, Icons.Outlined.Inventory2, AccentColors.Seafoam)
        ItemOrigin.RECIPE -> OriginBadge(R.string.origin_recipe, Icons.Outlined.RestaurantMenu, AccentColors.Lime)
    }

@Preview(showBackground = true)
@Composable
private fun GroceryItemRowPreview() {
    val items = listOf(
        GroceryItem(name = "Oranges", quantity = 4),
        GroceryItem(name = "Granola Bars", origin = ItemOrigin.AD),
        GroceryItem(name = "Milk", isCrossedOff = true),
    )
    AndroidAdaptedTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(ListRowSpacing)) {
            items.forEach { item ->
                GroceryItemRow(item, {}, {}, {})
            }
        }
    }
}
