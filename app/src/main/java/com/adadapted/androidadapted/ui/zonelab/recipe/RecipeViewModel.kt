package com.adadapted.androidadapted.ui.zonelab.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.AdZoneHandlerFactory
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.data.GroceryRepository
import com.adadapted.androidadapted.data.model.ItemOrigin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeUiState(
    val recipe: Recipe = Recipe.TomatoSauce,
    val contextId: String = "",
    val message: RecipeMessage? = null,
)

/**
 * Shown once, then cleared with [RecipeViewModel.onMessageShown]. Not data classes, so a repeat of
 * the same message is a new value and shows again.
 */
sealed interface RecipeMessage {
    class IngredientsAdded(val count: Int) : RecipeMessage
    class NoList : RecipeMessage
}

class RecipeViewModel(
    private val repository: GroceryRepository,
    private val activityLog: ActivityLog,
    adZoneHandlerFactory: AdZoneHandlerFactory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    /** Resolves the list when content arrives, so ads always land in whichever list is first. */
    val zoneHandler: AdZoneHandler = adZoneHandlerFactory.create { names ->
        repository.addItemsToDefaultList(names, ItemOrigin.AD)
    }

    fun setContextId(contextId: String) {
        if (contextId == _uiState.value.contextId) return
        _uiState.update { it.copy(contextId = contextId) }
        activityLog.log(
            ActivityCategory.ZONE,
            if (contextId.isEmpty()) "Zone context cleared" else "Zone context set to \"$contextId\"",
            "Zones ${AdZoneCatalog.OFF_SCREEN_TOP}, ${AdZoneCatalog.OFF_SCREEN_BOTTOM}",
        )
    }

    fun addIngredientsToList() {
        val ingredients = _uiState.value.recipe.ingredients
        viewModelScope.launch {
            val isAdded = repository.addItemsToDefaultList(ingredients, ItemOrigin.RECIPE)
            val message = if (isAdded) RecipeMessage.IngredientsAdded(ingredients.size) else RecipeMessage.NoList()
            _uiState.update { it.copy(message = message) }
        }
    }

    /** Ignored when a newer message has replaced [message]. */
    fun onMessageShown(message: RecipeMessage) {
        _uiState.update { if (it.message === message) it.copy(message = null) else it }
    }

    companion object {
        val ContextChoices = listOf("", AdZoneCatalog.RECIPE_CONTEXT_ID)
    }
}
