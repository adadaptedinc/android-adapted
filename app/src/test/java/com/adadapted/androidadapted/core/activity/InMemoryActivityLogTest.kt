package com.adadapted.androidadapted.core.activity

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryActivityLogTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-13T15:04:05Z"), ZoneOffset.UTC)

    @Test
    fun `keeps the newest entries first, up to capacity`() {
        val log = InMemoryActivityLog(capacity = 2, clock = clock, echo = {})

        log.log(ActivityCategory.SESSION, "first")
        log.log(ActivityCategory.ZONE, "second")
        log.log(ActivityCategory.LIST, "third")

        assertEquals(listOf("third", "second"), log.entries.value.map { it.message })
    }

    @Test
    fun `echoes each entry and clears`() {
        val echoed = mutableListOf<ActivityEntry>()
        val log = InMemoryActivityLog(clock = clock, echo = { echoed += it })

        log.log(ActivityCategory.ERROR, "Ad failed to load", "Zone 1")
        log.clear()

        assertEquals("Ad failed to load", echoed.single().message)
        assertTrue(log.entries.value.isEmpty())
    }

    @Test
    fun `formats a transcript line per entry`() {
        val log = InMemoryActivityLog(clock = clock, echo = {})
        log.log(ActivityCategory.SESSION, "SDK started")
        log.log(ActivityCategory.LIST, "Added \"Milk\"", "Groceries")

        assertEquals(
            "2026-10-13T15:04:05Z [List] Added \"Milk\" — Groceries\n" +
                "2026-10-13T15:04:05Z [Session] SDK started",
            log.entries.value.toTranscript(),
        )
    }
}
