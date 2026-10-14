package com.adadapted.androidadapted.ui.zonelab.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.data.ListEvent
import com.adadapted.androidadapted.data.ListEventTracker
import com.adadapted.androidadapted.data.description

enum class ListEventKind {
    ADDED,
    CROSSED_OFF,
    DELETED,
    ;

    fun event(itemName: String, listName: String): ListEvent = when (this) {
        ADDED -> ListEvent.ItemAdded(itemName, listName)
        CROSSED_OFF -> ListEvent.ItemCrossedOff(itemName, listName)
        DELETED -> ListEvent.ItemDeleted(itemName, listName)
    }
}

/** Fires list events by hand, straight to the SDK. */
class ListManagerEventsViewModel(
    private val tracker: ListEventTracker,
    private val activityLog: ActivityLog,
) : ViewModel() {

    var itemName by mutableStateOf("Test Milk")
        private set

    var listName by mutableStateOf("Test List")
        private set

    /** A summary of what was fired last, or null before anything has been. */
    var lastFired by mutableStateOf<String?>(null)
        private set

    val canFire: Boolean
        get() = itemName.isNotBlank()

    fun onItemNameChange(name: String) {
        itemName = name
    }

    fun onListNameChange(name: String) {
        listName = name
    }

    fun fire(kind: ListEventKind) {
        if (!canFire) return
        val event = kind.event(itemName.trim(), listName.trim())
        tracker.track(event)
        record("Manual event: ${event.description}", event.listName)
    }

    /** Sends an add, a cross-off, and a delete, each with and without the list name. */
    fun fireFullSequence() {
        if (!canFire) return
        val item = itemName.trim()
        val list = listName.trim()
        val events = ListEventKind.entries.flatMap { kind -> listOf(kind.event(item, list), kind.event(item, "")) }
        events.forEach(tracker::track)
        record("Manual sequence: ${events.size} events", if (list.isEmpty()) item else "$item · $list")
    }

    private fun record(message: String, detail: String) {
        activityLog.log(ActivityCategory.LIST, message, detail.ifEmpty { null })
        lastFired = if (detail.isEmpty()) message else "$message — $detail"
    }
}
