package com.adadapted.androidadapted.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.adadapted.androidadapted.R
import kotlinx.serialization.Serializable

// Lists tab
@Serializable data object ListsGraph
@Serializable data object ListsRoute
@Serializable data class GroceryListRoute(val listId: String)

// Zone Lab tab
@Serializable data object ZoneLabGraph
@Serializable data object ZoneLabRoute
@Serializable data object ScrollerRoute
@Serializable data object OffScreenZonesRoute
@Serializable data object ListManagerEventsRoute

// Activity tab
@Serializable data object ActivityGraph
@Serializable data object ActivityRoute

/** A tab in the bottom bar. Each keeps its own back stack. */
enum class TopLevelDestination(
    val graph: Any,
    val startRoute: Any,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    LISTS(ListsGraph, ListsRoute, R.string.tab_lists, Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
    ZONE_LAB(ZoneLabGraph, ZoneLabRoute, R.string.tab_zone_lab, Icons.Outlined.Science, Icons.Filled.Science),
    ACTIVITY(ActivityGraph, ActivityRoute, R.string.tab_activity, Icons.Outlined.MonitorHeart, Icons.Filled.MonitorHeart),
}
