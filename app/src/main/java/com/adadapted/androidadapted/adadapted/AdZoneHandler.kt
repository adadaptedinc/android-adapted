package com.adadapted.androidadapted.adadapted

import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** What a zone reports about itself. */
sealed interface AdZoneEvent {
    data class ZoneHasAds(val hasAds: Boolean) : AdZoneEvent
    data object AdLoaded : AdZoneEvent
    data object AdLoadFailed : AdZoneEvent

    /** A link or popup ad was tapped. */
    data class NonContentAction(val adId: String) : AdZoneEvent
}

/** Receives one zone's callbacks. Called on the main thread. */
interface AdZoneHandler {
    fun onEvent(zoneId: String, event: AdZoneEvent)

    fun onAddToListOffer(zoneId: String, offer: AddToListOffer)
}

/** Builds handlers for screens, so they don't depend on how zone callbacks are handled. */
fun interface AdZoneHandlerFactory {
    /**
     * Without [addToList], add-to-list content is logged but left unhandled, so the SDK doesn't
     * count an interaction for items nothing received.
     */
    fun create(addToList: AddToListHandler?): AdZoneHandler
}

/**
 * Logs every zone callback to the activity log and delivers add-to-list content to [addToList].
 * Deliveries run in [scope], which should outlive the screen, so the SDK still hears the outcome
 * when the user leaves mid-delivery.
 */
class ActivityLoggingAdZoneHandler(
    private val activityLog: ActivityLog,
    private val scope: CoroutineScope,
    private val addToList: AddToListHandler?,
) : AdZoneHandler {

    override fun onEvent(zoneId: String, event: AdZoneEvent) {
        val zone = "Zone $zoneId"
        when (event) {
            is AdZoneEvent.ZoneHasAds ->
                activityLog.log(ActivityCategory.ZONE, if (event.hasAds) "Zone has ads" else "Zone has no ads", zone)
            AdZoneEvent.AdLoaded -> activityLog.log(ActivityCategory.ZONE, "Ad loaded", zone)
            AdZoneEvent.AdLoadFailed -> activityLog.log(ActivityCategory.ERROR, "Ad failed to load", zone)
            is AdZoneEvent.NonContentAction ->
                activityLog.log(ActivityCategory.CONTENT, "Link ad tapped", "$zone · ad ${event.adId}")
        }
    }

    override fun onAddToListOffer(zoneId: String, offer: AddToListOffer) {
        val zone = "Zone $zoneId"
        val handler = addToList
        if (handler == null) {
            activityLog.log(ActivityCategory.CONTENT, "Ad offered ${offer.itemNames.size} item(s)", "$zone has no list attached")
            return
        }
        scope.launch {
            activityLog.logDelivery(offer.deliverTo(handler), subject = "Ad", where = zone)
        }
    }
}
