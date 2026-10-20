package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon

/**
 * Applies changes to a snapshot of the lists and collects the [ListEvent]s they produce.
 *
 * Holds every list rule in one place, free of I/O and threading, so the rules can be tested
 * directly. Changes that don't apply, like an unknown list or item ID, are ignored.
 */
internal class GroceryListEditor(lists: List<GroceryList>, private val zoneRule: ListZoneRule = ListZoneRule()) {

    private val lists = lists.toMutableList()
    private val pendingEvents = mutableListOf<ListEvent>()

    var isChanged = false
        private set

    val result: List<GroceryList>
        get() = lists.toList()

    val events: List<ListEvent>
        get() = pendingEvents.toList()

    // region Items

    /**
     * Bumps the quantity of a matching item that isn't crossed off instead of adding a duplicate, and
     * skips a match already at the maximum. Returns whether anything was added, which is false when
     * the list doesn't exist, every name is blank, or every match is at the maximum.
     */
    fun addItems(listId: String, names: List<String>, origin: ItemOrigin): Boolean {
        val listIndex = indexOfList(listId) ?: return false
        val trimmedNames = names.map(String::trim).filter(String::isNotEmpty)
        if (trimmedNames.isEmpty()) return false

        val original = lists[listIndex]
        var list = original
        for (name in trimmedNames) {
            val existing = list.items.indexOfOpenItem(name)
            val items = when {
                existing == null -> list.items + GroceryItem(name = name, origin = origin)
                list.items[existing].quantity < GroceryItem.QuantityRange.last ->
                    list.items.replaceAt(existing) { it.copy(quantity = it.quantity + 1) }
                else -> continue
            }
            list = list.copy(items = items)
            pendingEvents += ListEvent.ItemAdded(name, list.name)
        }
        if (list == original) return false
        commit(listIndex, list)
        return true
    }

    fun setCrossedOff(listId: String, itemId: String, isCrossedOff: Boolean) {
        updateItem(listId, itemId) { it.copy(isCrossedOff = isCrossedOff) }
    }

    /**
     * Applies only the fields that differ between [original] and [edited], so changes made since
     * [original] was copied, like an ad bumping the quantity, aren't overwritten.
     */
    fun applyEdits(listId: String, original: GroceryItem, edited: GroceryItem) {
        updateItem(listId, original.id) { current ->
            current.copy(
                name = if (edited.name != original.name) edited.name.trim() else current.name,
                quantity = if (edited.quantity != original.quantity) edited.quantity else current.quantity,
                isCrossedOff = if (edited.isCrossedOff != original.isCrossedOff) edited.isCrossedOff else current.isCrossedOff,
            )
        }
    }

    fun deleteItems(listId: String, itemIds: Set<String>) {
        val listIndex = indexOfList(listId) ?: return
        val list = lists[listIndex]
        val (removed, kept) = list.items.partition { it.id in itemIds }
        if (removed.isEmpty()) return

        removed.forEach { pendingEvents += ListEvent.ItemDeleted(it.name, list.name) }
        commit(listIndex, list.copy(items = kept))
    }

    fun clearCrossedOff(listId: String) {
        val list = indexOfList(listId)?.let(lists::get) ?: return
        deleteItems(listId, list.crossedOffItems.mapTo(mutableSetOf()) { it.id })
    }

    // endregion

    // region Lists

    /** Returns the new list's ID, or null when [name] is blank or [zoneRule] rejects [zoneId]. */
    fun addList(name: String, icon: ListIcon, zoneId: String): String? {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || zoneRule.problemWith(zoneId) != null) return null

        val list = GroceryList(name = trimmedName, icon = icon, zoneId = zoneId.trim())
        lists += list
        isChanged = true
        return list.id
    }

    fun deleteList(listId: String) {
        val listIndex = indexOfList(listId) ?: return
        val removed = lists.removeAt(listIndex)
        removed.items.forEach { pendingEvents += ListEvent.ItemDeleted(it.name, removed.name) }
        isChanged = true
    }

    fun moveList(listId: String, toIndex: Int) {
        val fromIndex = indexOfList(listId) ?: return
        if (toIndex !in lists.indices || toIndex == fromIndex) return

        lists.add(toIndex, lists.removeAt(fromIndex))
        isChanged = true
    }

    // endregion

    /**
     * Reports a rename as a delete and an add, since the SDK tracks items by name. An item that
     * becomes open under a name another open item already has is folded into that item.
     */
    private fun updateItem(listId: String, itemId: String, change: (GroceryItem) -> GroceryItem) {
        val listIndex = indexOfList(listId) ?: return
        val list = lists[listIndex]
        val itemIndex = list.items.indexOfFirst { it.id == itemId }.takeIf { it >= 0 } ?: return

        val old = list.items[itemIndex]
        val item = change(old)
        if (item == old || item.name.isBlank()) return

        val isRenamed = item.name != old.name
        if (isRenamed) {
            pendingEvents += ListEvent.ItemDeleted(old.name, list.name)
            pendingEvents += ListEvent.ItemAdded(item.name, list.name)
        }
        if (item.isCrossedOff && (!old.isCrossedOff || isRenamed)) {
            pendingEvents += ListEvent.ItemCrossedOff(item.name, list.name)
        }

        val isReopened = !item.isCrossedOff && (old.isCrossedOff || isRenamed)
        val duplicate = if (isReopened) list.items.indexOfOpenItem(item.name, excludingId = itemId) else null
        val items = if (duplicate != null) {
            list.items
                .replaceAt(duplicate) { it.copy(quantity = it.quantity.plusClamped(item.quantity)) }
                .filterNot { it.id == itemId }
        } else {
            list.items.replaceAt(itemIndex) { item }
        }
        commit(listIndex, list.copy(items = items))
    }

    private fun commit(listIndex: Int, list: GroceryList) {
        lists[listIndex] = list
        isChanged = true
    }

    private fun indexOfList(listId: String): Int? =
        lists.indexOfFirst { it.id == listId }.takeIf { it >= 0 }

    private fun List<GroceryItem>.indexOfOpenItem(name: String, excludingId: String? = null): Int? =
        indexOfFirst { it.id != excludingId && !it.isCrossedOff && it.name.equals(name, ignoreCase = true) }
            .takeIf { it >= 0 }

    private inline fun <T> List<T>.replaceAt(index: Int, transform: (T) -> T): List<T> =
        toMutableList().also { it[index] = transform(it[index]) }

    private fun Int.plusClamped(amount: Int): Int =
        (this + amount).coerceIn(GroceryItem.QuantityRange)
}
