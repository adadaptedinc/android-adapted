package com.adadapted.androidadapted.ui.lists

import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.data.ListZoneRule
import com.adadapted.androidadapted.data.model.ListIcon

/** What the New List form holds, and whether it can be submitted. */
data class NewListDraft(
    val name: String = "",
    val icon: ListIcon = ListIcon.CART,
    val zoneId: String = AdZoneCatalog.LIST,
) {
    enum class ZoneIdProblem {
        BLANK,

        /** Zone Lab's zones would deliver an ad's items to both Zone Lab and the list. */
        USED_BY_ZONE_LAB,
    }

    val zoneIdProblem: ZoneIdProblem?
        get() = when (AdZoneCatalog.listZoneRule.problemWith(zoneId)) {
            ListZoneRule.Problem.BLANK -> ZoneIdProblem.BLANK
            ListZoneRule.Problem.RESERVED -> ZoneIdProblem.USED_BY_ZONE_LAB
            null -> null
        }

    val canSubmit: Boolean
        get() = name.isNotBlank() && zoneIdProblem == null
}
