package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.data.AddItemsResult

/** Adds item names to a list and says which of them were added. */
fun interface AddToListHandler {
    suspend fun addToList(itemNames: List<String>): AddItemsResult
}

/**
 * Items an ad or payload offered to add to a list. Wraps the SDK content so it can be settled
 * without depending on the SDK, and so the SDK hears exactly one outcome.
 */
class AddToListOffer internal constructor(private val content: AddToListContent) {

    private val items = content.getItems().filter { it.title.isNotBlank() }

    val itemNames: List<String> = items.map { it.title.trim() }

    /** Where the content came from, such as "in_app" or "out_of_app". */
    val source: String
        get() = content.getSource()

    sealed interface Outcome {
        /** [count] items were added, and [skipped] more were already at the maximum quantity. */
        data class Added(val count: Int, val skipped: Int) : Outcome
        data object NoItems : Outcome
        data object NoList : Outcome
        data object AllAtMaximum : Outcome
        data object NotSaved : Outcome
    }

    /** Hands the items to [handler], then acknowledges each item it added and fails the rest. */
    suspend fun deliverTo(handler: AddToListHandler): Outcome {
        if (itemNames.isEmpty()) {
            content.failed(NO_ITEMS_MESSAGE)
            return Outcome.NoItems
        }
        return when (val result = handler.addToList(itemNames)) {
            is AddItemsResult.Added -> settle(result.names)
            AddItemsResult.NothingAdded -> fail(AT_MAXIMUM_MESSAGE, Outcome.AllAtMaximum)
            AddItemsResult.NoList -> fail(NO_LIST_MESSAGE, Outcome.NoList)
            AddItemsResult.NotSaved -> fail(NOT_SAVED_MESSAGE, Outcome.NotSaved)
        }
    }

    /**
     * Matches each item to one of the [added] names, so a repeated item is only acknowledged as
     * often as it was added. Acknowledges before failing, since the SDK settles the whole content
     * on the first item call.
     */
    private fun settle(added: List<String>): Outcome {
        val unclaimed = added.toMutableList()
        val (accepted, rejected) = items.partition { unclaimed.remove(it.title.trim()) }
        accepted.forEach(content::itemAcknowledge)
        rejected.forEach { content.itemFailed(it, AT_MAXIMUM_MESSAGE) }
        return Outcome.Added(count = accepted.size, skipped = rejected.size)
    }

    private fun fail(message: String, outcome: Outcome): Outcome {
        content.failed(message)
        return outcome
    }

    private companion object {
        const val NO_ITEMS_MESSAGE = "Content has no items with a title"
        const val NO_LIST_MESSAGE = "No list to add the items to"
        const val AT_MAXIMUM_MESSAGE = "Item is already at the maximum quantity"
        const val NOT_SAVED_MESSAGE = "The list couldn't be saved"
    }
}

/**
 * Records how delivering an offer went, as "[subject] added N item(s)" or an error saying why
 * nothing was added. [where] says which zone or payload it came from.
 */
internal fun ActivityLog.logDelivery(outcome: AddToListOffer.Outcome, subject: String, where: String) {
    when (outcome) {
        is AddToListOffer.Outcome.Added -> log(
            ActivityCategory.CONTENT,
            "$subject added ${outcome.count} item(s)",
            if (outcome.skipped > 0) "$where · ${outcome.skipped} already at the maximum" else where,
        )
        AddToListOffer.Outcome.NoItems -> log(ActivityCategory.ERROR, "$subject items not added", "$where · no items with a title")
        AddToListOffer.Outcome.NoList -> log(ActivityCategory.ERROR, "$subject items not added", "$where · no list to add them to")
        AddToListOffer.Outcome.AllAtMaximum -> log(ActivityCategory.ERROR, "$subject items not added", "$where · all already at the maximum")
        AddToListOffer.Outcome.NotSaved -> log(ActivityCategory.ERROR, "$subject items not added", "$where · the list couldn't be saved")
    }
}
