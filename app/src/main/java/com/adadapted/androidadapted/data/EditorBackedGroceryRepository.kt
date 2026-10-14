package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon

/** Runs every change through a [GroceryListEditor], leaving subclasses to decide how lists are stored. */
internal abstract class EditorBackedGroceryRepository(private val zoneRule: ListZoneRule) : GroceryRepository {

    override suspend fun addItems(listId: String, names: List<String>, origin: ItemOrigin): Boolean =
        edit { addItems(listId, names, origin) }

    override suspend fun addItemsToDefaultList(names: List<String>, origin: ItemOrigin): Boolean =
        edit { result.firstOrNull()?.let { addItems(it.id, names, origin) } }

    override suspend fun setCrossedOff(listId: String, itemId: String, isCrossedOff: Boolean) {
        edit { setCrossedOff(listId, itemId, isCrossedOff) }
    }

    override suspend fun updateItem(listId: String, original: GroceryItem, edited: GroceryItem) {
        edit { applyEdits(listId, original, edited) }
    }

    override suspend fun deleteItems(listId: String, itemIds: Set<String>) {
        edit { deleteItems(listId, itemIds) }
    }

    override suspend fun clearCrossedOff(listId: String) {
        edit { clearCrossedOff(listId) }
    }

    override suspend fun addList(name: String, icon: ListIcon, zoneId: String) {
        edit { addList(name, icon, zoneId) }
    }

    override suspend fun deleteList(listId: String) {
        edit { deleteList(listId) }
    }

    override suspend fun moveList(listId: String, toIndex: Int) {
        edit { moveList(listId, toIndex) }
    }

    /** Applies [change] to the current lists. Returns whether it altered them and they were saved. */
    protected abstract suspend fun edit(change: GroceryListEditor.() -> Unit): Boolean

    protected fun editorFor(lists: List<GroceryList>): GroceryListEditor = GroceryListEditor(lists, zoneRule)
}
