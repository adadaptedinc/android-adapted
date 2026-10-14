package com.adadapted.androidadapted.ui.lists

import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewListDraftTest {

    @Test
    fun `a named list on the list zone can be submitted`() {
        val draft = NewListDraft(name = "Party", zoneId = AdZoneCatalog.LIST)

        assertNull(draft.zoneIdProblem)
        assertTrue(draft.canSubmit)
    }

    @Test
    fun `a blank name can't be submitted`() {
        assertFalse(NewListDraft(name = "  ").canSubmit)
    }

    @Test
    fun `Zone Lab's zones are rejected`() {
        AdZoneCatalog.zoneLabZones.forEach { zoneId ->
            val draft = NewListDraft(name = "Party", zoneId = " $zoneId ")

            assertEquals(NewListDraft.ZoneIdProblem.USED_BY_ZONE_LAB, draft.zoneIdProblem)
            assertFalse(draft.canSubmit)
        }
    }

    @Test
    fun `a blank zone is rejected`() {
        assertEquals(NewListDraft.ZoneIdProblem.BLANK, NewListDraft(name = "Party", zoneId = " ").zoneIdProblem)
    }
}
