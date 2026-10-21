package com.adadapted.androidadapted.data

/** What came of adding item names to a list. */
sealed interface AddItemsResult {
    /**
     * [names] holds one entry per name that was added or had its quantity bumped, in the order
     * given. Any other names were blank or already at the maximum quantity.
     */
    data class Added(val names: List<String>) : AddItemsResult

    /** Every name was blank or already at the maximum quantity. */
    data object NothingAdded : AddItemsResult

    data object NoList : AddItemsResult

    /** The change couldn't be saved, so nothing was added. */
    data object NotSaved : AddItemsResult
}
