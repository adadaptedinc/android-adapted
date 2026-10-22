package com.adadapted.androidadapted.core.activity

import java.time.format.DateTimeFormatter

/** Formats entries as plain text, one per line, for sharing outside the app. */
fun List<ActivityEntry>.toTranscript(): String = joinToString(separator = "\n") { entry ->
    buildString {
        append(DateTimeFormatter.ISO_INSTANT.format(entry.timestamp))
        append(" [").append(entry.category.label).append("] ")
        append(entry.message)
        entry.detail?.let { append(" — ").append(it) }
    }
}
