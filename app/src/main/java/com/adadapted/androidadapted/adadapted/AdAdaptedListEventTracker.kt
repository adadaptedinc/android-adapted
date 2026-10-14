package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.AdAdaptedListManager
import com.adadapted.androidadapted.data.ListEvent
import com.adadapted.androidadapted.data.ListEventTracker

/** Reports list events to [AdAdaptedListManager]. */
class AdAdaptedListEventTracker : ListEventTracker {
    override fun track(event: ListEvent) {
        when (event) {
            is ListEvent.ItemAdded -> AdAdaptedListManager.itemAddedToList(list = event.listName, item = event.itemName)
            is ListEvent.ItemCrossedOff -> AdAdaptedListManager.itemCrossedOffList(list = event.listName, item = event.itemName)
            is ListEvent.ItemDeleted -> AdAdaptedListManager.itemDeletedFromList(list = event.listName, item = event.itemName)
        }
    }
}
