package com.adadapted.androidadapted.data

import androidx.datastore.core.CorruptionException
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ItemOrigin
import com.adadapted.androidadapted.data.model.ListIcon
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GroceryListsSerializerTest {

    private val serializer = GroceryListsSerializer(StoredGroceryLists(emptyList()))

    @Test
    fun `round-trips the lists`() = runTest {
        val stored = StoredGroceryLists(
            listOf(
                GroceryList(
                    name = "Groceries",
                    icon = ListIcon.PRODUCE,
                    zoneId = "102110",
                    items = listOf(GroceryItem(name = "Milk", quantity = 2, isCrossedOff = true, origin = ItemOrigin.AD)),
                ),
            ),
        )
        val output = ByteArrayOutputStream()

        serializer.writeTo(stored, output)

        assertEquals(stored, serializer.readFrom(ByteArrayInputStream(output.toByteArray())))
    }

    @Test
    fun `ignores fields it doesn't know`() = runTest {
        val json = """{"lists":[],"addedLater":true}"""

        assertEquals(StoredGroceryLists(emptyList()), serializer.readFrom(ByteArrayInputStream(json.toByteArray())))
    }

    @Test(expected = CorruptionException::class)
    fun `unreadable content is reported as corruption`() = runTest {
        serializer.readFrom(ByteArrayInputStream("{ not json".toByteArray()))
    }
}
