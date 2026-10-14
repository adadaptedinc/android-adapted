package com.adadapted.androidadapted.ui.zonelab

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.adadapted.androidadapted.di.AppContainer
import com.adadapted.androidadapted.ui.navigation.ListManagerEventsRoute
import com.adadapted.androidadapted.ui.navigation.OffScreenZonesRoute
import com.adadapted.androidadapted.ui.navigation.ScrollerRoute
import com.adadapted.androidadapted.ui.navigation.ZoneLabGraph
import com.adadapted.androidadapted.ui.navigation.ZoneLabRoute
import com.adadapted.androidadapted.ui.zonelab.events.ListManagerEventsScreen
import com.adadapted.androidadapted.ui.zonelab.events.ListManagerEventsViewModel
import com.adadapted.androidadapted.ui.zonelab.recipe.RecipeScreen
import com.adadapted.androidadapted.ui.zonelab.recipe.RecipeViewModel
import com.adadapted.androidadapted.ui.zonelab.scroller.ScrollerScreen
import com.adadapted.androidadapted.ui.zonelab.scroller.ScrollerViewModel

fun NavGraphBuilder.zoneLabGraph(container: AppContainer, navController: NavController) {
    navigation<ZoneLabGraph>(startDestination = ZoneLabRoute) {
        composable<ZoneLabRoute> {
            ZoneLabScreen(
                config = container.adAdaptedConfig,
                onOpenScenario = { scenario ->
                    navController.navigate(
                        when (scenario) {
                            ZoneLabScenario.SCROLLER -> ScrollerRoute
                            ZoneLabScenario.OFF_SCREEN -> OffScreenZonesRoute
                            ZoneLabScenario.LIST_EVENTS -> ListManagerEventsRoute
                        },
                    )
                },
            )
        }
        composable<ScrollerRoute> {
            ScrollerScreen(
                viewModel = viewModel { ScrollerViewModel(container.adZoneHandlerFactory) },
                onBack = navController::navigateUp,
            )
        }
        composable<OffScreenZonesRoute> {
            RecipeScreen(
                viewModel = viewModel {
                    RecipeViewModel(container.groceryRepository, container.activityLog, container.adZoneHandlerFactory)
                },
                onBack = navController::navigateUp,
            )
        }
        composable<ListManagerEventsRoute> {
            ListManagerEventsScreen(
                viewModel = viewModel { ListManagerEventsViewModel(container.sdkListEventTracker, container.activityLog) },
                onBack = navController::navigateUp,
            )
        }
    }
}
