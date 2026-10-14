package com.adadapted.androidadapted.ui.zonelab

import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.data.ListEvent
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.testing.FakeAdZoneHandlerFactory
import com.adadapted.androidadapted.testing.FakeGroceryRepository
import com.adadapted.androidadapted.testing.MainDispatcherRule
import com.adadapted.androidadapted.testing.RecordingListEventTracker
import com.adadapted.androidadapted.testing.testActivityLog
import com.adadapted.androidadapted.testing.testList
import com.adadapted.androidadapted.ui.zonelab.events.ListEventKind
import com.adadapted.androidadapted.ui.zonelab.events.ListManagerEventsViewModel
import com.adadapted.androidadapted.ui.zonelab.recipe.Recipe
import com.adadapted.androidadapted.ui.zonelab.recipe.RecipeMessage
import com.adadapted.androidadapted.ui.zonelab.recipe.RecipeViewModel
import com.adadapted.androidadapted.ui.zonelab.scroller.ScrollerViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ZoneLabViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activityLog = testActivityLog()

    @Test
    fun `list manager fires one event, or the full sequence with and without the list`() {
        val tracker = RecordingListEventTracker()
        val viewModel = ListManagerEventsViewModel(tracker, activityLog)

        viewModel.fire(ListEventKind.CROSSED_OFF)
        viewModel.fireFullSequence()

        assertEquals(ListEvent.ItemCrossedOff("Test Milk", "Test List"), tracker.events.first())
        assertEquals(7, tracker.events.size)
        assertEquals(3, tracker.events.drop(1).count { it.listName.isEmpty() })
        assertEquals("Manual sequence: 6 events — Test Milk · Test List", viewModel.lastFired)
    }

    @Test
    fun `list manager won't fire without an item name`() {
        val tracker = RecordingListEventTracker()
        val viewModel = ListManagerEventsViewModel(tracker, activityLog)

        viewModel.onItemNameChange("  ")
        viewModel.fire(ListEventKind.ADDED)
        viewModel.fireFullSequence()

        assertTrue(tracker.events.isEmpty())
        assertNull(viewModel.lastFired)
    }

    @Test
    fun `recipe adds its ingredients to the first list and says so`() = runTest {
        val repository = FakeGroceryRepository(listOf(testList()))
        val viewModel = RecipeViewModel(repository, activityLog, FakeAdZoneHandlerFactory())

        viewModel.addIngredientsToList()

        val items = repository.lists.value.single().items
        assertEquals(Recipe.TomatoSauce.ingredients, items.map { it.name })
        assertTrue(items.all { it.origin == ItemOrigin.RECIPE })
        assertEquals(RecipeMessage.IngredientsAdded(items.size), viewModel.uiState.value.message)

        viewModel.onMessageShown()
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `recipe says when there's no list`() = runTest {
        val viewModel = RecipeViewModel(FakeGroceryRepository(), activityLog, FakeAdZoneHandlerFactory())

        viewModel.addIngredientsToList()

        assertEquals(RecipeMessage.NoList, viewModel.uiState.value.message)
    }

    @Test
    fun `recipe logs context changes once`() {
        val viewModel = RecipeViewModel(FakeGroceryRepository(), activityLog, FakeAdZoneHandlerFactory())

        viewModel.setContextId(AdZoneCatalog.RECIPE_CONTEXT_ID)
        viewModel.setContextId(AdZoneCatalog.RECIPE_CONTEXT_ID)

        assertEquals(AdZoneCatalog.RECIPE_CONTEXT_ID, viewModel.uiState.value.contextId)
        assertEquals(1, activityLog.entries.value.size)
    }

    @Test
    fun `scroller grows a page at a time and never attaches a list`() {
        val zoneHandlers = FakeAdZoneHandlerFactory()
        val viewModel = ScrollerViewModel(zoneHandlers)

        viewModel.loadMore()

        assertEquals(ScrollerViewModel.PAGE_SIZE * 2, viewModel.rowCount.value)
        assertNull(zoneHandlers.addToList)
        assertTrue(ScrollerViewModel.isAdRow(0) && ScrollerViewModel.isAdRow(ScrollerViewModel.AD_INTERVAL))
    }
}
