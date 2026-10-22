package com.adadapted.androidadapted.ui.lists

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.adadapted.androidadapted.di.AppContainer
import com.adadapted.androidadapted.ui.navigation.GroceryListRoute
import com.adadapted.androidadapted.ui.navigation.ListsGraph
import com.adadapted.androidadapted.ui.navigation.ListsRoute

fun NavGraphBuilder.listsGraph(container: AppContainer, navController: NavController) {
    navigation<ListsGraph>(startDestination = ListsRoute) {
        composable<ListsRoute> {
            ListsScreen(
                viewModel = viewModel { ListsViewModel(container.groceryRepository) },
                onOpenList = { listId -> navController.navigate(GroceryListRoute(listId)) },
            )
        }
        composable<GroceryListRoute> { entry ->
            val listId = entry.toRoute<GroceryListRoute>().listId
            GroceryListScreen(
                viewModel = viewModel {
                    GroceryListViewModel(
                        listId = listId,
                        repository = container.groceryRepository,
                        suggestionSource = container.createKeywordSuggestionSource(),
                        activityLog = container.activityLog,
                        adZoneHandlerFactory = container.adZoneHandlerFactory,
                    )
                },
                onBack = navController::navigateUp,
            )
        }
    }
}
