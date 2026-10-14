package com.adadapted.androidadapted.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adadapted.androidadapted.di.AppContainer
import com.adadapted.androidadapted.ui.activity.activityGraph
import com.adadapted.androidadapted.ui.lists.listsGraph
import com.adadapted.androidadapted.ui.navigation.ListsGraph
import com.adadapted.androidadapted.ui.navigation.TopLevelDestination
import com.adadapted.androidadapted.ui.theme.AppTheme
import com.adadapted.androidadapted.ui.zonelab.zoneLabGraph

/** The app shell: a bottom bar over one back stack per tab. */
@Composable
fun AndroidAdaptedApp(container: AppContainer) {
    val navController = rememberNavController()
    val colors = AppTheme.colors

    Scaffold(
        bottomBar = {
            // Read here, so navigating recomposes only the bar.
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination
            val itemColors = NavigationBarItemDefaults.colors(
                indicatorColor = colors.navBarSelected,
                selectedIconColor = colors.navBar,
                selectedTextColor = colors.navBarSelected,
                unselectedIconColor = colors.onNavBar.copy(alpha = 0.7f),
                unselectedTextColor = colors.onNavBar.copy(alpha = 0.7f),
            )
            NavigationBar(containerColor = colors.navBar, contentColor = colors.onNavBar) {
                TopLevelDestination.entries.forEach { destination ->
                    val isSelected = currentDestination.isIn(destination)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navController.navigateToTab(destination, isSelected) },
                        icon = {
                            Icon(if (isSelected) destination.selectedIcon else destination.icon, contentDescription = null)
                        },
                        label = { Text(stringResource(destination.label)) },
                        colors = itemColors,
                    )
                }
            }
        },
        // Each screen draws its own top bar under the status bar.
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ListsGraph,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            listsGraph(container, navController)
            zoneLabGraph(container, navController)
            activityGraph(container)
        }
    }
}

private fun NavDestination?.isIn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.graph::class) } == true

/** Switches tabs, keeping each tab's back stack. Reselecting the current tab returns to its root. */
private fun NavHostController.navigateToTab(destination: TopLevelDestination, isSelected: Boolean) {
    if (isSelected) {
        popBackStack(destination.startRoute, inclusive = false)
        return
    }
    navigate(destination.graph) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
