package com.adadapted.androidadapted.testing

import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.AdZoneHandlerFactory
import com.adadapted.androidadapted.adadapted.AddToListHandler
import com.adadapted.androidadapted.adadapted.KeywordSuggestion
import com.adadapted.androidadapted.adadapted.KeywordSuggestionSource
import com.adadapted.androidadapted.core.activity.InMemoryActivityLog
import com.adadapted.androidadapted.data.EditorBackedGroceryRepository
import com.adadapted.androidadapted.data.GroceryListEditor
import com.adadapted.androidadapted.data.ListEvent
import com.adadapted.androidadapted.data.ListEventTracker
import com.adadapted.androidadapted.data.ListZoneRule
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.ui.components.PreviewAdZoneHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun testActivityLog() = InMemoryActivityLog(echo = {})

fun testList(items: List<GroceryItem> = emptyList()) =
    GroceryList(id = "g", name = "Groceries", icon = ListIcon.CART, zoneId = "1", items = items)

class RecordingListEventTracker : ListEventTracker {
    val events = mutableListOf<ListEvent>()

    override fun track(event: ListEvent) {
        events += event
    }
}

/** Applies the real list rules in memory. */
internal class FakeGroceryRepository(initial: List<GroceryList> = emptyList()) : EditorBackedGroceryRepository(ListZoneRule()) {
    private val state = MutableStateFlow(initial)
    override val lists: StateFlow<List<GroceryList>> = state

    override suspend fun <T> edit(change: GroceryListEditor.() -> T): T? {
        val editor = editorFor(state.value)
        val outcome = editor.change()
        if (editor.isChanged) state.value = editor.result
        return outcome
    }
}

class FakeKeywordSuggestionSource(private val terms: List<String>) : KeywordSuggestionSource {
    val queries = mutableListOf<String>()
    val presented = mutableListOf<KeywordSuggestion>()
    val selected = mutableListOf<KeywordSuggestion>()

    override val minimumQueryLength = 3

    override fun match(query: String): List<KeywordSuggestion> {
        queries += query
        val matchId = queries.size.toLong()
        return terms
            .filter { it.startsWith(query, ignoreCase = true) }
            .map { KeywordSuggestion(termId = it.lowercase(), name = it, matchId = matchId) }
    }

    override fun reportPresented(suggestion: KeywordSuggestion) {
        presented += suggestion
    }

    override fun reportSelected(suggestion: KeywordSuggestion) {
        selected += suggestion
    }
}

/** Hands out a handler that keeps the add-to-list callback so tests can call it. */
class FakeAdZoneHandlerFactory : AdZoneHandlerFactory {
    var addToList: AddToListHandler? = null
        private set

    override fun create(addToList: AddToListHandler?): AdZoneHandler {
        this.addToList = addToList
        return PreviewAdZoneHandler
    }
}
