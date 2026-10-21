package com.adadapted.androidadapted.adadapted

import com.adadapted.android.sdk.core.keyword.KeywordInterceptMatcher
import com.adadapted.android.sdk.core.keyword.Suggestion
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog

/**
 * A keyword intercept term offered as the user types. [matchId] tells apart the same term returned
 * by different matches, since the SDK tracks each match's suggestions separately.
 */
data class KeywordSuggestion(val termId: String, val name: String, val matchId: Long)

/** Matches typed text against keyword intercept terms and reports what the user saw and picked. */
interface KeywordSuggestionSource {
    /** Shorter queries never match, so callers skip them. */
    val minimumQueryLength: Int

    /** [query] is trimmed and at least [minimumQueryLength] long. */
    fun match(query: String): List<KeywordSuggestion>

    /**
     * Call once per suggestion when it's shown. Repeat calls for the same match are ignored, as are
     * suggestions from an earlier match.
     */
    fun reportPresented(suggestion: KeywordSuggestion)

    fun reportSelected(suggestion: KeywordSuggestion)
}

/**
 * Backed by [KeywordInterceptMatcher]. Matching also reports matched and unmatched searches to
 * the SDK. Main thread only.
 */
class AdAdaptedKeywordSuggestionSource : KeywordSuggestionSource {

    // The SDK tracks presentation and selection on the Suggestion it returned from the last match.
    private var lastMatchId = 0L
    private var lastMatches: Map<String, Suggestion> = emptyMap()

    override val minimumQueryLength: Int = 3

    override fun match(query: String): List<KeywordSuggestion> {
        val matches = KeywordInterceptMatcher.match(query)
        val matchId = ++lastMatchId
        lastMatches = matches.associateBy { it.termId }
        return matches
            .map { KeywordSuggestion(termId = it.termId, name = it.name, matchId = matchId) }
            .sortedBy { it.name.lowercase() }
    }

    override fun reportPresented(suggestion: KeywordSuggestion) {
        sdkSuggestionFor(suggestion)?.presented()
    }

    override fun reportSelected(suggestion: KeywordSuggestion) {
        sdkSuggestionFor(suggestion)?.selected()
    }

    private fun sdkSuggestionFor(suggestion: KeywordSuggestion): Suggestion? =
        lastMatches[suggestion.termId]?.takeIf { suggestion.matchId == lastMatchId }
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
