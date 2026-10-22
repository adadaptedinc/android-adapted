package com.adadapted.androidadapted.ui.components

import com.adadapted.androidadapted.adadapted.AdZoneEvent
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.AddToListOffer

/** Ignores everything. For previews, where zones render as placeholders. */
internal object PreviewAdZoneHandler : AdZoneHandler {
    override fun onEvent(zoneId: String, event: AdZoneEvent) = Unit

    override fun onAddToListOffer(zoneId: String, offer: AddToListOffer) = Unit
}
