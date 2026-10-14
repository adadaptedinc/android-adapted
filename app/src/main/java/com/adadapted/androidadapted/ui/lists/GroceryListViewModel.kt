package com.adadapted.androidadapted.ui.lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.AdZoneHandlerFactory
import com.adadapted.androidadapted.adadapted.KeywordSuggestion
import com.adadapted.androidadapted.adadapted.KeywordSuggestionSource
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.data.GroceryRepository
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.ui.STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ListContent {
    data object Loading : ListContent

    /** The list was deleted, or never existed. */
    data object Missing : ListContent

    data class Ready(val list: GroceryList) : ListContent
}

data class GroceryListUiState(
    val content: ListContent = ListContent.Loading,
    val suggestions: List<KeywordSuggestion> = emptyList(),
    val isRecipeContextEnabled: Boolean = false,
    /** Changes whenever a fresh ad is requested, so the zone can be recreated. */
    val zoneReloadCount: Int = 0,
    /** The item open in the editor, as it was when the editor opened. */
    val editingItem: GroceryItem? = null,
) {
    val list: GroceryList?
        get() = (content as? ListContent.Ready)?.list

    val zoneContextId: String
        get() = if (isRecipeContextEnabled) AdZoneCatalog.RECIPE_CONTEXT_ID else ""
}

sealed interface GroceryListAction {
    data class DraftChanged(val text: String) : GroceryListAction
    data object AddDraft : GroceryListAction
    data class SuggestionShown(val suggestion: KeywordSuggestion) : GroceryListAction
    data class SuggestionSelected(val suggestion: KeywordSuggestion) : GroceryListAction
    data class ToggleCrossedOff(val item: GroceryItem) : GroceryListAction
    data class DeleteItem(val item: GroceryItem) : GroceryListAction
    data class EditItem(val item: GroceryItem) : GroceryListAction
    data class SaveEdits(val edited: GroceryItem) : GroceryListAction
    data object DismissEditor : GroceryListAction
    data object ClearCrossedOff : GroceryListAction
    data class SetRecipeContext(val isEnabled: Boolean) : GroceryListAction
    data object ReloadZone : GroceryListAction
}

class GroceryListViewModel(
    private val listId: String,
    private val repository: GroceryRepository,
    private val suggestionSource: KeywordSuggestionSource,
    private val activityLog: ActivityLog,
    adZoneHandlerFactory: AdZoneHandlerFactory,
) : ViewModel() {

    /** Compose state rather than a flow, so the text field never falls behind what was typed. */
    var draft by mutableStateOf("")
        private set

    private val query = MutableStateFlow("")
    private val zoneSettings = MutableStateFlow(ZoneSettings())
    private val editingItem = MutableStateFlow<GroceryItem?>(null)

    /** Ads in this list's zone add their items to this list. */
    val zoneHandler: AdZoneHandler = adZoneHandlerFactory.create(viewModelScope) { names ->
        repository.addItems(listId, names, ItemOrigin.AD)
    }

    private val content: Flow<ListContent> = repository.lists
        .map { lists -> lists.firstOrNull { it.id == listId }?.let(ListContent::Ready) ?: ListContent.Missing }
        .distinctUntilChanged()

    /**
     * Clears as soon as the query is too short to match, and otherwise waits for typing to pause.
     * Keeps running while the screen is away, so coming back doesn't match the same query again.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val suggestions: StateFlow<List<KeywordSuggestion>> = query
        .map(String::trim)
        .distinctUntilChanged()
        .transformLatest { trimmed ->
            if (trimmed.length < suggestionSource.minimumQueryLength) {
                emit(emptyList())
                return@transformLatest
            }
            delay(SUGGESTION_DEBOUNCE_MILLIS)
            emit(suggestionSource.match(trimmed))
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uiState: StateFlow<GroceryListUiState> =
        combine(content, suggestions, zoneSettings, editingItem) { content, suggestions, zone, editing ->
            GroceryListUiState(
                content = content,
                suggestions = suggestions,
                isRecipeContextEnabled = zone.isRecipeContextEnabled,
                zoneReloadCount = zone.reloadCount,
                editingItem = editing,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GroceryListUiState())

    fun onAction(action: GroceryListAction) {
        when (action) {
            is GroceryListAction.DraftChanged -> updateDraft(action.text)
            GroceryListAction.AddDraft -> addItem(draft, ItemOrigin.MANUAL)
            is GroceryListAction.SuggestionShown -> suggestionSource.reportPresented(action.suggestion)
            is GroceryListAction.SuggestionSelected -> selectSuggestion(action.suggestion)
            is GroceryListAction.ToggleCrossedOff -> viewModelScope.launch {
                repository.setCrossedOff(listId, action.item.id, !action.item.isCrossedOff)
            }
            is GroceryListAction.DeleteItem -> {
                if (editingItem.value?.id == action.item.id) editingItem.value = null
                viewModelScope.launch { repository.deleteItems(listId, setOf(action.item.id)) }
            }
            is GroceryListAction.EditItem -> editingItem.value = action.item
            is GroceryListAction.SaveEdits -> saveEdits(action.edited)
            GroceryListAction.DismissEditor -> editingItem.value = null
            GroceryListAction.ClearCrossedOff -> viewModelScope.launch { repository.clearCrossedOff(listId) }
            is GroceryListAction.SetRecipeContext -> setRecipeContext(action.isEnabled)
            GroceryListAction.ReloadZone -> reloadZone()
        }
    }

    private fun updateDraft(text: String) {
        draft = text
        query.value = text
    }

    private fun selectSuggestion(suggestion: KeywordSuggestion) {
        suggestionSource.reportSelected(suggestion)
        addItem(suggestion.name, ItemOrigin.SUGGESTION)
    }

    private fun addItem(name: String, origin: ItemOrigin) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addItems(listId, listOf(name), origin) }
        updateDraft("")
    }

    private fun saveEdits(edited: GroceryItem) {
        val original = editingItem.value ?: return
        editingItem.value = null
        viewModelScope.launch { repository.updateItem(listId, original, edited) }
    }

    private fun setRecipeContext(isEnabled: Boolean) {
        zoneSettings.update { it.copy(isRecipeContextEnabled = isEnabled) }
        activityLog.log(ActivityCategory.ZONE, if (isEnabled) "Recipe context on" else "Recipe context off", zoneDetail())
    }

    private fun reloadZone() {
        zoneSettings.update { it.copy(reloadCount = it.reloadCount + 1) }
        activityLog.log(ActivityCategory.ZONE, "Zone reloaded", zoneDetail())
    }

    private fun zoneDetail(): String = "Zone ${uiState.value.list?.zoneId ?: "—"}"

    private data class ZoneSettings(val isRecipeContextEnabled: Boolean = false, val reloadCount: Int = 0)

    companion object {
        const val SUGGESTION_DEBOUNCE_MILLIS = 200L
    }
}
