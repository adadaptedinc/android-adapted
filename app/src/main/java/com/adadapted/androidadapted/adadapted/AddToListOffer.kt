package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog

/** Adds item names to a list. Returns whether they were added. */
fun interface AddToListHandler {
    suspend fun addToList(itemNames: List<String>): Boolean
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
        data class Added(val count: Int) : Outcome
        data object NoItems : Outcome
        data object NoList : Outcome
    }

    /** Hands the items to [handler], then acknowledges or fails the content to match. */
    suspend fun deliverTo(handler: AddToListHandler): Outcome {
        if (itemNames.isEmpty()) {
            content.failed(NO_ITEMS_MESSAGE)
            return Outcome.NoItems
        }
        if (!handler.addToList(itemNames)) {
            content.failed(NO_LIST_MESSAGE)
            return Outcome.NoList
        }
        items.forEach(content::itemAcknowledge)
        content.acknowledge()
        return Outcome.Added(items.size)
    }

    private companion object {
        const val NO_ITEMS_MESSAGE = "Content has no items with a title"
        const val NO_LIST_MESSAGE = "No list to add the items to"
    }
}

/**
 * Records how delivering an offer went, as "[subject] added N item(s)" or an error saying why
 * nothing was added. [where] says which zone or payload it came from.
 */
internal fun ActivityLog.logDelivery(outcome: AddToListOffer.Outcome, subject: String, where: String) {
    when (outcome) {
        is AddToListOffer.Outcome.Added -> log(ActivityCategory.CONTENT, "$subject added ${outcome.count} item(s)", where)
        AddToListOffer.Outcome.NoItems -> log(ActivityCategory.ERROR, "$subject items not added", "$where · no items with a title")
        AddToListOffer.Outcome.NoList -> log(ActivityCategory.ERROR, "$subject items not added", "$where · no list to add to")
    }
}
