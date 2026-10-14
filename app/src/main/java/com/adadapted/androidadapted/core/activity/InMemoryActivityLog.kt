package com.adadapted.androidadapted.core.activity

import android.util.Log
import java.time.Clock
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Keeps the most recent [capacity] entries in memory and mirrors each one to [echo],
 * which writes to Logcat by default.
 */
class InMemoryActivityLog(
    private val capacity: Int = DEFAULT_CAPACITY,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val echo: (ActivityEntry) -> Unit = ::writeToLogcat,
) : ActivityLog {

    private val nextId = AtomicLong()
    private val _entries = MutableStateFlow<List<ActivityEntry>>(emptyList())
    override val entries: StateFlow<List<ActivityEntry>> = _entries.asStateFlow()

    init {
        require(capacity > 0) { "capacity must be positive" }
    }

    override fun log(category: ActivityCategory, message: String, detail: String?) {
        val entry = ActivityEntry(
            id = nextId.incrementAndGet(),
            timestamp = clock.instant(),
            category = category,
            message = message,
            detail = detail,
        )
        _entries.update { current ->
            buildList(capacity) {
                add(entry)
                addAll(current.subList(0, minOf(current.size, capacity - 1)))
            }
        }
        echo(entry)
    }

    override fun clear() {
        _entries.value = emptyList()
    }

    companion object {
        const val DEFAULT_CAPACITY = 300
        private const val TAG = "AdAdapted"

        private fun writeToLogcat(entry: ActivityEntry) {
            val detail = entry.detail?.let { " — $it" }.orEmpty()
            Log.d(TAG, "[${entry.category.label}] ${entry.message}$detail")
        }
    }
}
