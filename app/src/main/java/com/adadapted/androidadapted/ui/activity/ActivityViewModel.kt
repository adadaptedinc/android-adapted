package com.adadapted.androidadapted.ui.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityEntry
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.core.activity.toTranscript
import com.adadapted.androidadapted.ui.STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ActivityUiState(
    /** Newest first, narrowed to [filter]. */
    val entries: List<ActivityEntry> = emptyList(),
    /** Null shows every category. */
    val filter: ActivityCategory? = null,
    /** Whether anything has been logged, filtered or not. */
    val hasEntries: Boolean = false,
)

class ActivityViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val activityLog: ActivityLog,
) : ViewModel() {

    val uiState: StateFlow<ActivityUiState> =
        combine(activityLog.entries, savedStateHandle.getStateFlow<String?>(FILTER_KEY, null)) { entries, filterName ->
            val filter = filterName?.let { name -> ActivityCategory.entries.firstOrNull { it.name == name } }
            ActivityUiState(
                entries = if (filter == null) entries else entries.filter { it.category == filter },
                filter = filter,
                hasEntries = entries.isNotEmpty(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ActivityUiState())

    fun setFilter(category: ActivityCategory?) {
        savedStateHandle[FILTER_KEY] = category?.name
    }

    fun clear() {
        activityLog.clear()
    }

    /** Every entry, regardless of the filter, as plain text. */
    fun transcript(): String = activityLog.entries.value.toTranscript()

    private companion object {
        const val FILTER_KEY = "filter"
    }
}
