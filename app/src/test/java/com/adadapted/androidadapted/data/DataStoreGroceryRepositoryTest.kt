package com.adadapted.androidadapted.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.testing.RecordingListEventTracker
import com.adadapted.androidadapted.testing.testList
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreGroceryRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val tracker = RecordingListEventTracker()
    private val starter = listOf(testList())

    private fun TestScope.repository() =
        DataStoreGroceryRepository(
            dataStore = DataStoreFactory.create(
                serializer = GroceryListsSerializer(StoredGroceryLists(starter)),
                scope = backgroundScope,
                produceFile = { File(folder.root, "lists.json") },
            ),
            tracker = tracker,
            externalScope = backgroundScope,
        )

    @Test
    fun `starts with the starter lists`() = runTest {
        assertEquals(starter, repository().lists.first())
    }

    @Test
    fun `reports events once the change is saved`() = runTest {
        val repository = repository()

        assertEquals(AddItemsResult.Added(listOf("Milk")), repository.addItems("g", listOf("Milk"), ItemOrigin.MANUAL))

        assertEquals(listOf("Milk"), repository.lists.first().single().items.map { it.name })
        assertEquals(listOf(ListEvent.ItemAdded("Milk", "Groceries")), tracker.events)
    }

    @Test
    fun `adds to the first list by default`() = runTest {
        val repository = repository()
        repository.addList("Household", ListIcon.HOUSEHOLD, "1")

        assertEquals(AddItemsResult.Added(listOf("Soap")), repository.addItemsToDefaultList(listOf("Soap"), ItemOrigin.PAYLOAD))

        assertEquals(listOf("Soap"), repository.lists.first().first().items.map { it.name })
    }

    @Test
    fun `fails when there's no list to add to`() = runTest {
        val repository = repository()
        repository.deleteList("g")

        assertEquals(AddItemsResult.NoList, repository.addItemsToDefaultList(listOf("Soap"), ItemOrigin.PAYLOAD))
        assertEquals(AddItemsResult.NoList, repository.addItems("g", listOf("Soap"), ItemOrigin.MANUAL))
        assertTrue(tracker.events.isEmpty())
    }

    @Test
    fun `a change that can't be saved adds nothing and reports nothing`() = runTest {
        val unwritable = object : DataStore<StoredGroceryLists> {
            override val data: Flow<StoredGroceryLists> = flow { emit(StoredGroceryLists(starter)) }

            override suspend fun updateData(transform: suspend (StoredGroceryLists) -> StoredGroceryLists) =
                throw IOException("Disk full")
        }
        val writeErrors = mutableListOf<IOException>()
        val repository = DataStoreGroceryRepository(
            dataStore = unwritable,
            tracker = tracker,
            externalScope = backgroundScope,
            onWriteFailed = writeErrors::add,
        )

        assertEquals(AddItemsResult.NotSaved, repository.addItems("g", listOf("Milk"), ItemOrigin.MANUAL))
        assertEquals("Disk full", writeErrors.single().message)
        assertTrue(tracker.events.isEmpty())
    }

    @Test
    fun `a file that can't be read shows no lists instead of crashing`() = runTest {
        val unreadable = object : DataStore<StoredGroceryLists> {
            override val data: Flow<StoredGroceryLists> = flow { throw IOException("Disk error") }

            override suspend fun updateData(transform: suspend (StoredGroceryLists) -> StoredGroceryLists) =
                throw IOException("Disk error")
        }
        val readErrors = mutableListOf<IOException>()
        val repository = DataStoreGroceryRepository(
            dataStore = unreadable,
            tracker = tracker,
            externalScope = backgroundScope,
            onReadFailed = readErrors::add,
        )

        assertTrue(repository.lists.first().isEmpty())
        assertEquals("Disk error", readErrors.single().message)
    }
}
