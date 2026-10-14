package com.adadapted.androidadapted.ui.activity

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.adadapted.androidadapted.di.AppContainer
import com.adadapted.androidadapted.ui.navigation.ActivityGraph
import com.adadapted.androidadapted.ui.navigation.ActivityRoute

fun NavGraphBuilder.activityGraph(container: AppContainer) {
    navigation<ActivityGraph>(startDestination = ActivityRoute) {
        composable<ActivityRoute> {
            ActivityScreen(viewModel = viewModel { ActivityViewModel(createSavedStateHandle(), container.activityLog) })
        }
    }
}
