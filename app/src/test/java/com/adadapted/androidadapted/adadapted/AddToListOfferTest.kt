package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.atl.AddToListItem
import com.adadapted.androidadapted.data.AddItemsResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddToListOfferTest {

    private val milk = AddToListItem(title = "Milk")
    private val bread = AddToListItem(title = " Bread ")

    /** Records the SDK calls in order. */
    private class RecordingContent(private val items: List<AddToListItem>) : AddToListContent {
        val calls = mutableListOf<String>()

        override fun acknowledge() {
            calls += "acknowledge"
        }

        override fun itemAcknowledge(item: AddToListItem) {
            calls += "acknowledge ${item.title.trim()}"
        }

        override fun failed(message: String) {
            calls += "failed"
        }

        override fun itemFailed(item: AddToListItem, message: String) {
            calls += "failed ${item.title.trim()}"
        }

        override fun getSource() = AddToListContent.Sources.IN_APP
        override fun getItems() = items
        override fun hasNoItems() = items.isEmpty()
    }

    @Test
    fun `acknowledges only the items that were added, before failing the rest`() = runTest {
        val content = RecordingContent(listOf(milk, bread))

        val outcome = AddToListOffer(content).deliverTo { AddItemsResult.Added(listOf("Bread")) }

        assertEquals(AddToListOffer.Outcome.Added(count = 1, skipped = 1), outcome)
        assertEquals(listOf("acknowledge Bread", "failed Milk"), content.calls)
    }

    @Test
    fun `a repeated item is acknowledged only as often as it was added`() = runTest {
        val content = RecordingContent(listOf(milk, milk))

        val outcome = AddToListOffer(content).deliverTo { AddItemsResult.Added(listOf("Milk")) }

        assertEquals(AddToListOffer.Outcome.Added(count = 1, skipped = 1), outcome)
        assertEquals(listOf("acknowledge Milk", "failed Milk"), content.calls)
    }

    @Test
    fun `fails the content when nothing was added, saying why`() = runTest {
        val results = mapOf(
            AddItemsResult.NothingAdded to AddToListOffer.Outcome.AllAtMaximum,
            AddItemsResult.NoList to AddToListOffer.Outcome.NoList,
            AddItemsResult.NotSaved to AddToListOffer.Outcome.NotSaved,
        )
        for ((result, expected) in results) {
            val content = RecordingContent(listOf(milk))

            assertEquals(expected, AddToListOffer(content).deliverTo { result })
            assertEquals(listOf("failed"), content.calls)
        }
    }
}
