package com.adadapted.androidadapted.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adadapted.androidadapted.data.GroceryRepository
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.ui.STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ListsUiState {
    data object Loading : ListsUiState
    data class Ready(val lists: List<GroceryList>) : ListsUiState
}

class ListsViewModel(private val repository: GroceryRepository) : ViewModel() {

    val uiState: StateFlow<ListsUiState> = repository.lists
        .map { ListsUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ListsUiState.Loading)

    /** Ignored unless the draft is valid. */
    fun addList(draft: NewListDraft) {
        if (!draft.canSubmit) return
        viewModelScope.launch { repository.addList(draft.name, draft.icon, draft.zoneId) }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch { repository.deleteList(listId) }
    }

    fun moveList(listId: String, toIndex: Int) {
        viewModelScope.launch { repository.moveList(listId, toIndex) }
    }
}
