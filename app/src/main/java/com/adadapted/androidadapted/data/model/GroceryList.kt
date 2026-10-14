package com.adadapted.androidadapted.data.model

import java.util.UUID
import kotlinx.serialization.Serializable

/** The icon a list is shown with. Kept free of UI types so the model can be persisted. */
@Serializable
enum class ListIcon {
    CART,
    PRODUCE,
    HOUSEHOLD,
    ELECTRONICS,
    PARTY,
    TAKEOUT,
}

@Serializable
data class GroceryList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ListIcon,
    /** The AdAdapted zone rendered at the bottom of this list. */
    val zoneId: String,
    val items: List<GroceryItem> = emptyList(),
) {
    val remainingItems: List<GroceryItem>
        get() = items.filterNot { it.isCrossedOff }

    val crossedOffItems: List<GroceryItem>
        get() = items.filter { it.isCrossedOff }
}
