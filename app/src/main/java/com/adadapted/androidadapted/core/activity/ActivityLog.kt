package com.adadapted.androidadapted.core.activity

import java.time.Instant
import kotlinx.coroutines.flow.StateFlow

/** What an [ActivityEntry] is about, used to filter the log. */
enum class ActivityCategory(val label: String) {
    SESSION("Session"),
    ZONE("Zone"),
    CONTENT("Content"),
    LIST("List"),
    KEYWORD("Keyword"),
    ERROR("Error"),
}

data class ActivityEntry(
    val id: Long,
    val timestamp: Instant,
    val category: ActivityCategory,
    val message: String,
    val detail: String? = null,
)

/**
 * A live record of SDK callbacks and the events the app reported, so the app can be
 * exercised without a debugger attached. Safe to call from any thread.
 */
interface ActivityLog {
    /** Newest first. */
    val entries: StateFlow<List<ActivityEntry>>

    fun log(category: ActivityCategory, message: String, detail: String? = null)

    fun clear()
}
