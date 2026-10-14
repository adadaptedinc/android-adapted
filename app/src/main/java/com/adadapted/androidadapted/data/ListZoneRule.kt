package com.adadapted.androidadapted.data

/**
 * Which ad zone a list may use: any zone that isn't blank or [reserved] for another feature. A list
 * receives its zone's add-to-list content, so sharing a zone would deliver an ad's items twice.
 */
class ListZoneRule(private val reserved: Set<String> = emptySet()) {

    enum class Problem { BLANK, RESERVED }

    fun problemWith(zoneId: String): Problem? {
        val trimmed = zoneId.trim()
        return when {
            trimmed.isEmpty() -> Problem.BLANK
            trimmed in reserved -> Problem.RESERVED
            else -> null
        }
    }
}
