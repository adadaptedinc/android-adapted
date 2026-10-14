package com.adadapted.androidadapted.data

import androidx.datastore.core.DataStore
import com.adadapted.androidadapted.data.model.GroceryList
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Persists the lists with DataStore and reports each change to [tracker] once it has been saved.
 *
 * Writes run in [externalScope], so a change that has started is saved and reported even if the
 * screen that asked for it goes away. [onWriteFailed] hears about saves that couldn't be written.
 */
internal class DataStoreGroceryRepository(
    private val dataStore: DataStore<StoredGroceryLists>,
    private val tracker: ListEventTracker,
    private val externalScope: CoroutineScope,
    zoneRule: ListZoneRule = ListZoneRule(),
    private val onWriteFailed: (IOException) -> Unit = {},
) : EditorBackedGroceryRepository(zoneRule) {

    override val lists: Flow<List<GroceryList>> =
        dataStore.data.map { it.lists }.distinctUntilChanged()

    override suspend fun edit(change: GroceryListEditor.() -> Unit): Boolean =
        externalScope.async {
            var events = emptyList<ListEvent>()
            var isChanged = false
            try {
                // DataStore runs the transform in the caller's context, so keep the editing off main.
                withContext(Dispatchers.Default) {
                    dataStore.updateData { stored ->
                        val editor = editorFor(stored.lists).apply { change() }
                        events = editor.events
                        isChanged = editor.isChanged
                        if (isChanged) stored.copy(lists = editor.result) else stored
                    }
                }
            } catch (e: IOException) {
                onWriteFailed(e)
                return@async false
            }
            events.forEach(tracker::track)
            isChanged
        }.await()
}
