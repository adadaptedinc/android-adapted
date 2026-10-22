package com.adadapted.androidadapted.ui.activity

import androidx.lifecycle.SavedStateHandle
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.testing.MainDispatcherRule
import com.adadapted.androidadapted.testing.collectInBackground
import com.adadapted.androidadapted.testing.testActivityLog
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activityLog = testActivityLog().apply {
        log(ActivityCategory.SESSION, "SDK started")
        log(ActivityCategory.ZONE, "Ad loaded", "Zone 102110")
        log(ActivityCategory.LIST, "Added \"Milk\"")
    }

    @Test
    fun `filters by category, and remembers the filter`() = runTest {
        val savedState = SavedStateHandle()
        val viewModel = ActivityViewModel(savedState, activityLog)
        collectInBackground(viewModel.uiState)

        viewModel.setFilter(ActivityCategory.ZONE)

        assertEquals(listOf("Ad loaded"), viewModel.uiState.value.entries.map { it.message })

        val restored = ActivityViewModel(savedState, activityLog)
        collectInBackground(restored.uiState)
        assertEquals(ActivityCategory.ZONE, restored.uiState.value.filter)
    }

    @Test
    fun `an empty filter still knows there's activity`() = runTest {
        val viewModel = ActivityViewModel(SavedStateHandle(), activityLog)
        collectInBackground(viewModel.uiState)

        viewModel.setFilter(ActivityCategory.ERROR)

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertTrue(viewModel.uiState.value.hasEntries)
    }

    @Test
    fun `transcript covers every entry and clear empties the log`() = runTest {
        val viewModel = ActivityViewModel(SavedStateHandle(), activityLog)
        collectInBackground(viewModel.uiState)
        viewModel.setFilter(ActivityCategory.LIST)

        assertEquals(3, viewModel.transcript().lines().size)

        viewModel.clear()
        assertFalse(viewModel.uiState.value.hasEntries)
    }
}
