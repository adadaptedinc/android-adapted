package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon
import kotlinx.coroutines.flow.Flow

/** The user's grocery lists. Every change is persisted and reported to a [ListEventTracker]. */
interface GroceryRepository {
    val lists: Flow<List<GroceryList>>

    /** Returns whether anything was added, which is false when the list doesn't exist. */
    suspend fun addItems(listId: String, names: List<String>, origin: ItemOrigin): Boolean

    /** Adds to the first list. Returns false when there are no lists. */
    suspend fun addItemsToDefaultList(names: List<String>, origin: ItemOrigin): Boolean

    suspend fun setCrossedOff(listId: String, itemId: String, isCrossedOff: Boolean)

    /** Applies the fields that differ between [original] and [edited]. */
    suspend fun updateItem(listId: String, original: GroceryItem, edited: GroceryItem)

    suspend fun deleteItems(listId: String, itemIds: Set<String>)

    suspend fun clearCrossedOff(listId: String)

    suspend fun addList(name: String, icon: ListIcon, zoneId: String)

    suspend fun deleteList(listId: String)

    suspend fun moveList(listId: String, toIndex: Int)
}
