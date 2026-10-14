package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.testing.testList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroceryListEditorTest {

    private val milk = GroceryItem(id = "milk", name = "Milk")
    private val eggs = GroceryItem(id = "eggs", name = "Eggs", isCrossedOff = true)
    private val groceries = testList(items = listOf(milk, eggs))
    private val household = GroceryList(id = "h", name = "Household", icon = ListIcon.HOUSEHOLD, zoneId = "1")

    private fun editor() = GroceryListEditor(listOf(groceries, household))

    private fun GroceryListEditor.items(listId: String = "g") = result.first { it.id == listId }.items

    @Test
    fun `adding an open item's name bumps its quantity, ignoring case`() {
        val editor = editor()

        assertTrue(editor.addItems("g", listOf(" milk "), ItemOrigin.MANUAL))

        assertEquals(2, editor.items().first { it.id == "milk" }.quantity)
        assertEquals(2, editor.items().size)
        assertEquals(listOf(ListEvent.ItemAdded("milk", "Groceries")), editor.events)
    }

    @Test
    fun `adding a crossed-off item's name adds a new open item`() {
        val editor = editor()

        editor.addItems("g", listOf("Eggs"), ItemOrigin.AD)

        val added = editor.items().last()
        assertEquals("Eggs", added.name)
        assertFalse(added.isCrossedOff)
        assertEquals(ItemOrigin.AD, added.origin)
    }

    @Test
    fun `adding to a missing list or only blank names changes nothing`() {
        val editor = editor()

        assertFalse(editor.addItems("missing", listOf("Bread"), ItemOrigin.MANUAL))
        assertFalse(editor.addItems("g", listOf(" ", ""), ItemOrigin.MANUAL))

        assertFalse(editor.isChanged)
        assertTrue(editor.events.isEmpty())
    }

    @Test
    fun `quantity never passes the maximum`() {
        val full = groceries.copy(items = listOf(milk.copy(quantity = GroceryItem.QuantityRange.last)))
        val editor = GroceryListEditor(listOf(full))

        editor.addItems("g", listOf("Milk"), ItemOrigin.MANUAL)

        assertEquals(GroceryItem.QuantityRange.last, editor.items().single().quantity)
    }

    @Test
    fun `crossing off reports a cross-off, and uncrossing reports nothing`() {
        val editor = editor()

        editor.setCrossedOff("g", "milk", true)
        editor.setCrossedOff("g", "eggs", false)

        assertEquals(listOf(ListEvent.ItemCrossedOff("Milk", "Groceries")), editor.events)
    }

    @Test
    fun `uncrossing into a name that's already open merges the two`() {
        val list = groceries.copy(items = listOf(milk.copy(quantity = 2), GroceryItem(id = "old", name = "MILK", isCrossedOff = true, quantity = 3)))
        val editor = GroceryListEditor(listOf(list))

        editor.setCrossedOff("g", "old", false)

        val merged = editor.items().single()
        assertEquals("milk", merged.id)
        assertEquals(5, merged.quantity)
    }

    @Test
    fun `a rename is reported as a delete and an add`() {
        val editor = editor()

        editor.applyEdits("g", milk, milk.copy(name = "Oat Milk"))

        assertEquals("Oat Milk", editor.items().first { it.id == "milk" }.name)
        assertEquals(
            listOf(ListEvent.ItemDeleted("Milk", "Groceries"), ListEvent.ItemAdded("Oat Milk", "Groceries")),
            editor.events,
        )
    }

    @Test
    fun `edits keep changes made since the editor opened`() {
        val bumped = groceries.copy(items = listOf(milk.copy(quantity = 3), eggs))
        val editor = GroceryListEditor(listOf(bumped))

        // The editor opened when quantity was 1, and only the name changed.
        editor.applyEdits("g", milk, milk.copy(name = "Whole Milk"))

        val item = editor.items().first { it.id == "milk" }
        assertEquals("Whole Milk", item.name)
        assertEquals(3, item.quantity)
    }

    @Test
    fun `an edit that changes nothing isn't saved or reported`() {
        val editor = editor()

        editor.applyEdits("g", milk, milk.copy())
        editor.applyEdits("g", milk, milk.copy(name = "  "))

        assertFalse(editor.isChanged)
    }

    @Test
    fun `clearing crossed-off items deletes only those`() {
        val editor = editor()

        editor.clearCrossedOff("g")

        assertEquals(listOf("milk"), editor.items().map { it.id })
        assertEquals(listOf(ListEvent.ItemDeleted("Eggs", "Groceries")), editor.events)
    }

    @Test
    fun `deleting a list reports each of its items as deleted`() {
        val editor = editor()

        editor.deleteList("g")

        assertEquals(listOf("h"), editor.result.map { it.id })
        assertEquals(
            listOf(ListEvent.ItemDeleted("Milk", "Groceries"), ListEvent.ItemDeleted("Eggs", "Groceries")),
            editor.events,
        )
    }

    @Test
    fun `adding a list trims its name and zone and rejects a blank name`() {
        val editor = editor()

        assertNull(editor.addList("  ", ListIcon.CART, "1"))
        val id = editor.addList(" Party ", ListIcon.PARTY, " 42 ")

        val added = editor.result.last()
        assertEquals(id, added.id)
        assertEquals("Party", added.name)
        assertEquals("42", added.zoneId)
    }

    @Test
    fun `adding a list rejects a blank or reserved zone`() {
        val editor = GroceryListEditor(emptyList(), ListZoneRule(reserved = setOf("7")))

        assertNull(editor.addList("Party", ListIcon.PARTY, " "))
        assertNull(editor.addList("Party", ListIcon.PARTY, " 7 "))

        assertFalse(editor.isChanged)
    }

    @Test
    fun `moving a list reorders it and ignores out-of-range targets`() {
        val editor = editor()

        editor.moveList("h", 0)
        editor.moveList("h", 5)

        assertEquals(listOf("h", "g"), editor.result.map { it.id })
    }
}
