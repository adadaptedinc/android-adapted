package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.data.model.GroceryList
import com.adadapted.androidadapted.data.model.ListIcon

/** What a fresh install starts with. IDs are fixed so they hold steady until the lists are first saved. */
internal object StarterLists {
    fun create(zoneId: String): List<GroceryList> = listOf(
        GroceryList(
            id = "starter-groceries",
            name = "Groceries",
            icon = ListIcon.PRODUCE,
            zoneId = zoneId,
            items = listOf(
                GroceryItem(id = "starter-eggs", name = "Eggs"),
                GroceryItem(id = "starter-bread", name = "Bread"),
                GroceryItem(id = "starter-oranges", name = "Oranges", quantity = 4),
            ),
        ),
        GroceryList(id = "starter-household", name = "Household", icon = ListIcon.HOUSEHOLD, zoneId = zoneId),
        GroceryList(id = "starter-electronics", name = "Electronics", icon = ListIcon.ELECTRONICS, zoneId = zoneId),
    )
}
