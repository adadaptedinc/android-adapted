package com.adadapted.androidadapted.adadapted

import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.data.ListZoneRule

/**
 * The zones served for [AdAdaptedConfig.Default]'s app key. A zone that belongs to another app key
 * returns "requested ad zone not found" and renders blank.
 */
object AdZoneCatalog {
    /** Shared by every list, since only one is open at a time. */
    const val LIST = "102110"

    /** The scroller and the off-screen page are never on screen together, so they can share. */
    const val SCROLLER = "110002"
    const val OFF_SCREEN_TOP = SCROLLER
    const val OFF_SCREEN_BOTTOM = "110003"

    /** 102110 and 110002 serve a different ad for this zone context. */
    const val RECIPE_CONTEXT_ID = "original"

    /**
     * Off limits to lists. Every live zone with an ID receives that ID's add-to-list content, so a
     * list sharing a Zone Lab zone would have an ad's items added by both.
     */
    val zoneLabZones: Set<String> = setOf(SCROLLER, OFF_SCREEN_TOP, OFF_SCREEN_BOTTOM)

    /** Keeps lists off [zoneLabZones]. */
    val listZoneRule = ListZoneRule(reserved = zoneLabZones)

    /** Every zone on this app key is authored full width × 100, which the SDK doesn't expose. */
    val Height = 100.dp
}
