package com.adadapted.androidadapted.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.adadapted.androidadapted.data.model.GroceryList
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

/** The persisted form of the lists. Wrapped so fields can be added without breaking saved files. */
@Serializable
data class StoredGroceryLists(val lists: List<GroceryList>)

/** Stores the lists as JSON. An unreadable file surfaces as a [CorruptionException]. */
@OptIn(ExperimentalSerializationApi::class)
class GroceryListsSerializer(override val defaultValue: StoredGroceryLists) : Serializer<StoredGroceryLists> {

    override suspend fun readFrom(input: InputStream): StoredGroceryLists =
        try {
            json.decodeFromStream(StoredGroceryLists.serializer(), input)
        } catch (e: SerializationException) {
            throw CorruptionException("Saved grocery lists are unreadable", e)
        }

    override suspend fun writeTo(t: StoredGroceryLists, output: OutputStream) {
        json.encodeToStream(StoredGroceryLists.serializer(), t, output)
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            // Defaults like a creation time are expressions, so a value that happens to match one
            // must still be written, or it would come back as something else.
            encodeDefaults = true
        }
    }
}
