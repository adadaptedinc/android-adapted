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
    /** [ListZoneRule.Problem.RESERVED] means a Zone Lab zone, see [AdZoneCatalog.listZoneRule]. */
    val zoneIdProblem: ListZoneRule.Problem?
        get() = AdZoneCatalog.listZoneRule.problemWith(zoneId)

    val canSubmit: Boolean
        get() = name.isNotBlank() && zoneIdProblem == null
}
