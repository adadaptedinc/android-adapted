package com.adadapted.androidadapted.data

import androidx.datastore.core.DataStore
import com.adadapted.androidadapted.data.model.GroceryList
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Persists the lists with DataStore and reports each change to [tracker] once it has been saved.
 *
 * Writes run in [externalScope], so a change that has started is saved and reported even if the
 * screen that asked for it goes away. [onReadFailed] hears about a file that couldn't be read, after
 * which no lists are shown, and [onWriteFailed] about saves that couldn't be written.
 */
internal class DataStoreGroceryRepository(
    private val dataStore: DataStore<StoredGroceryLists>,
    private val tracker: ListEventTracker,
    private val externalScope: CoroutineScope,
    zoneRule: ListZoneRule = ListZoneRule(),
    private val onReadFailed: (IOException) -> Unit = {},
    private val onWriteFailed: (IOException) -> Unit = {},
) : EditorBackedGroceryRepository(zoneRule) {

    override val lists: Flow<List<GroceryList>> =
        dataStore.data
            .catch { error ->
                if (error !is IOException) throw error
                onReadFailed(error)
                emit(StoredGroceryLists(emptyList()))
            }
            .map { it.lists }
            .distinctUntilChanged()

    override suspend fun <T> edit(change: GroceryListEditor.() -> T): T? =
        externalScope.async {
            var events = emptyList<ListEvent>()
            var outcome: T? = null
            try {
                // DataStore runs the transform in the caller's context, so keep the editing off main.
                withContext(Dispatchers.Default) {
                    dataStore.updateData { stored ->
                        val editor = editorFor(stored.lists)
                        outcome = editor.change()
                        events = editor.events
                        if (editor.isChanged) stored.copy(lists = editor.result) else stored
                    }
                }
            } catch (e: IOException) {
                onWriteFailed(e)
                return@async null
            }
            events.forEach(tracker::track)
            outcome
        }.await()
}
