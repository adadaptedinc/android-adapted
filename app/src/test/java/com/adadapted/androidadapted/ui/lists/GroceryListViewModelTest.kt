package com.adadapted.androidadapted.ui.lists

import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.KeywordSuggestion
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.testing.FakeAdZoneHandlerFactory
import com.adadapted.androidadapted.testing.FakeGroceryRepository
import com.adadapted.androidadapted.testing.FakeKeywordSuggestionSource
import com.adadapted.androidadapted.testing.MainDispatcherRule
import com.adadapted.androidadapted.testing.collectInBackground
import com.adadapted.androidadapted.testing.testActivityLog
import com.adadapted.androidadapted.testing.testList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroceryListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val milk = GroceryItem(id = "milk", name = "Milk")
    private val repository = FakeGroceryRepository(listOf(testList(items = listOf(milk))))
    private val suggestions = FakeKeywordSuggestionSource(listOf("Mighty Milk", "Milk Duds", "Bread"))
    private val activityLog = testActivityLog()
    private val zoneHandlers = FakeAdZoneHandlerFactory()

    private fun TestScope.viewModel(listId: String = "g") =
        GroceryListViewModel(listId, repository, suggestions, activityLog, zoneHandlers).also { viewModel ->
            collectInBackground(viewModel.uiState)
        }

    private val GroceryListViewModel.items: List<GroceryItem>
        get() = uiState.value.list?.items.orEmpty()

    @Test
    fun `shows the list, or says it's missing`() = runTest {
        assertEquals("Groceries", viewModel().uiState.value.list?.name)
        assertEquals(ListContent.Missing, viewModel("gone").uiState.value.content)
    }

    @Test
    fun `matches keywords once typing pauses`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(GroceryListAction.DraftChanged("Mi"))
        viewModel.onAction(GroceryListAction.DraftChanged("Mil"))
        advanceTimeBy(GroceryListViewModel.SUGGESTION_DEBOUNCE_MILLIS / 2)
        viewModel.onAction(GroceryListAction.DraftChanged("Milk "))
        advanceTimeBy(GroceryListViewModel.SUGGESTION_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf("Milk"), suggestions.queries)
        assertEquals(listOf("Milk Duds"), viewModel.uiState.value.suggestions.map { it.name })
    }

    @Test
    fun `clears suggestions as soon as the query is too short`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(GroceryListAction.DraftChanged("Mig"))
        advanceTimeBy(GroceryListViewModel.SUGGESTION_DEBOUNCE_MILLIS + 1)

        viewModel.onAction(GroceryListAction.DraftChanged("Mi"))

        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
    }

    @Test
    fun `selecting a suggestion reports it, adds it, and resets the field`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(GroceryListAction.DraftChanged("Migh"))
        advanceTimeBy(GroceryListViewModel.SUGGESTION_DEBOUNCE_MILLIS + 1)
        val suggestion = viewModel.uiState.value.suggestions.single()

        viewModel.onAction(GroceryListAction.SuggestionShown(suggestion))
        viewModel.onAction(GroceryListAction.SuggestionSelected(suggestion))

        assertEquals(listOf(suggestion), suggestions.presented)
        assertEquals(listOf(suggestion), suggestions.selected)
        assertEquals(ItemOrigin.SUGGESTION, viewModel.items.last { it.name == "Mighty Milk" }.origin)
        assertEquals("", viewModel.draft)
        assertTrue(viewModel.uiState.value.suggestions.isEmpty())
    }

    @Test
    fun `adding the draft ignores blank text`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(GroceryListAction.DraftChanged("   "))
        viewModel.onAction(GroceryListAction.AddDraft)
        viewModel.onAction(GroceryListAction.DraftChanged("Bread"))
        viewModel.onAction(GroceryListAction.AddDraft)

        assertEquals(listOf("Milk", "Bread"), viewModel.items.map { it.name })
        assertEquals(ItemOrigin.MANUAL, viewModel.items.last().origin)
    }

    @Test
    fun `saving edits compares against the item as it was when the editor opened`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(GroceryListAction.EditItem(milk))
        repository.addItems("g", listOf("Milk"), ItemOrigin.AD) // An ad bumps the quantity meanwhile.

        viewModel.onAction(GroceryListAction.SaveEdits(milk.copy(name = "Oat Milk")))

        val item = viewModel.items.single()
        assertEquals("Oat Milk", item.name)
        assertEquals(2, item.quantity)
        assertEquals(null, viewModel.uiState.value.editingItem)
    }

    @Test
    fun `the editor closes when its item is removed some other way`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(GroceryListAction.EditItem(milk))

        repository.deleteItems("g", setOf(milk.id))

        assertEquals(null, viewModel.uiState.value.editingItem)
    }

    @Test
    fun `ads in the list's zone add to this list`() = runTest {
        viewModel()

        val isAdded = zoneHandlers.addToList!!.addToList(listOf("Granola"))

        assertTrue(isAdded)
        assertEquals(ItemOrigin.AD, repository.lists.value.single().items.last().origin)
    }

    @Test
    fun `recipe context switches the zone context and reloads bump the zone key`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(GroceryListAction.SetRecipeContext(true))
        viewModel.onAction(GroceryListAction.ReloadZone)

        assertEquals(AdZoneCatalog.RECIPE_CONTEXT_ID, viewModel.uiState.value.zoneContextId)
        assertEquals(1, viewModel.uiState.value.zoneReloadCount)
    }

    @Test
    fun `suggestions are matched against what was typed, not padded`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(GroceryListAction.DraftChanged("  bre  "))
        advanceTimeBy(GroceryListViewModel.SUGGESTION_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf(KeywordSuggestion("bread", "Bread")), viewModel.uiState.value.suggestions)
    }
}
