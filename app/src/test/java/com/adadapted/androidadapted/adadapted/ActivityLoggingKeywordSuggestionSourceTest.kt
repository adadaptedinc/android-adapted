package com.adadapted.androidadapted.adadapted

import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.testing.FakeKeywordSuggestionSource
import com.adadapted.androidadapted.testing.testActivityLog
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityLoggingKeywordSuggestionSourceTest {

    private val delegate = FakeKeywordSuggestionSource(listOf("Milk Duds", "Bread"))
    private val activityLog = testActivityLog()
    private val source = ActivityLoggingKeywordSuggestionSource(delegate, activityLog)

    @Test
    fun `logs each match and selection and passes them on`() {
        val suggestion = source.match("Milk").single()
        source.match("Zzz")
        source.reportPresented(suggestion)
        source.reportSelected(suggestion)

        assertEquals(listOf("Milk", "Zzz"), delegate.queries)
        assertEquals(listOf(suggestion), delegate.presented)
        assertEquals(listOf(suggestion), delegate.selected)
        assertEquals(
            listOf("Suggestion selected", "No suggestions", "1 suggestion(s)"),
            activityLog.entries.value.map { it.message },
        )
        assertEquals(ActivityCategory.KEYWORD, activityLog.entries.value.map { it.category }.distinct().single())
    }
}
