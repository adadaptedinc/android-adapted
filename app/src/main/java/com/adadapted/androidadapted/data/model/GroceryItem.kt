package com.adadapted.androidadapted.data.model

import java.util.UUID
import kotlinx.serialization.Serializable

/** How an item got onto a list. */
@Serializable
enum class ItemOrigin {
    MANUAL,
    SUGGESTION,
    AD,
    PAYLOAD,
    RECIPE,
}

@Serializable
data class GroceryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int = 1,
    val isCrossedOff: Boolean = false,
    val origin: ItemOrigin = ItemOrigin.MANUAL,
    val addedAtMillis: Long = System.currentTimeMillis(),
) {
    companion object {
        val QuantityRange = 1..99
    }
}
