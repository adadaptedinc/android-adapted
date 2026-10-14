package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.core.keyword.KeywordInterceptMatcher
import com.adadapted.android.sdk.core.keyword.Suggestion
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog

/** A keyword intercept term offered as the user types. */
data class KeywordSuggestion(val termId: String, val name: String)

/** Matches typed text against keyword intercept terms and reports what the user saw and picked. */
interface KeywordSuggestionSource {
    /** Shorter queries never match. */
    val minimumQueryLength: Int

    fun match(query: String): List<KeywordSuggestion>

    /** Call once per suggestion when it's shown. Repeat calls for the same match are ignored. */
    fun reportPresented(suggestion: KeywordSuggestion)

    fun reportSelected(suggestion: KeywordSuggestion)
}

/**
 * Backed by [KeywordInterceptMatcher]. Matching also reports matched and unmatched searches to
 * the SDK. Main thread only.
 */
class AdAdaptedKeywordSuggestionSource : KeywordSuggestionSource {

    // The SDK tracks presentation and selection on the Suggestion it returned from the last match.
    private var lastMatches: Map<String, Suggestion> = emptyMap()

    override val minimumQueryLength: Int = 3

    override fun match(query: String): List<KeywordSuggestion> {
        val trimmed = query.trim()
        if (trimmed.length < minimumQueryLength) {
            lastMatches = emptyMap()
            return emptyList()
        }
        val matches = KeywordInterceptMatcher.match(trimmed)
        lastMatches = matches.associateBy { it.termId }
        return matches
            .map { KeywordSuggestion(termId = it.termId, name = it.name) }
            .sortedBy { it.name.lowercase() }
    }

    override fun reportPresented(suggestion: KeywordSuggestion) {
        lastMatches[suggestion.termId]?.presented()
    }

    override fun reportSelected(suggestion: KeywordSuggestion) {
        lastMatches[suggestion.termId]?.selected()
    }
}

/** Passes calls on to [delegate] and records each match and selection in the activity log. */
class ActivityLoggingKeywordSuggestionSource(
    private val delegate: KeywordSuggestionSource,
    private val activityLog: ActivityLog,
) : KeywordSuggestionSource by delegate {

    override fun match(query: String): List<KeywordSuggestion> {
        val matches = delegate.match(query)
        activityLog.log(
            ActivityCategory.KEYWORD,
            if (matches.isEmpty()) "No suggestions" else "${matches.size} suggestion(s)",
            "\"$query\"",
        )
        return matches
    }

    override fun reportSelected(suggestion: KeywordSuggestion) {
        delegate.reportSelected(suggestion)
        activityLog.log(ActivityCategory.KEYWORD, "Suggestion selected", suggestion.name)
    }
}
