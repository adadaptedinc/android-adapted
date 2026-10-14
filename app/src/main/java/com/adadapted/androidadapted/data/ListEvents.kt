package com.adadapted.androidadapted.data

/** A change to a list that an ad SDK reports on. Items are identified by name. */
sealed interface ListEvent {
    val itemName: String

    /** Empty when the event isn't tied to a named list. */
    val listName: String

    data class ItemAdded(override val itemName: String, override val listName: String) : ListEvent

    data class ItemCrossedOff(override val itemName: String, override val listName: String) : ListEvent

    data class ItemDeleted(override val itemName: String, override val listName: String) : ListEvent
}

/** A short, human-readable account of the event, like `Added "Milk"`. */
val ListEvent.description: String
    get() = when (this) {
        is ListEvent.ItemAdded -> "Added \"$itemName\""
        is ListEvent.ItemCrossedOff -> "Crossed off \"$itemName\""
        is ListEvent.ItemDeleted -> "Deleted \"$itemName\""
    }

/** Receives list changes, so the data layer stays independent of the SDK that reports them. */
fun interface ListEventTracker {
    fun track(event: ListEvent)
}
