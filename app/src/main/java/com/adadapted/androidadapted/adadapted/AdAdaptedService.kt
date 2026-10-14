package com.adadapted.androidadapted.adadapted

import android.content.Context
import com.adadapted.android.sdk.AdAdapted
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.interfaces.AaSdkAdditContentListener
import com.adadapted.android.sdk.core.interfaces.AaSdkEventListener
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Starts the SDK and handles its app-wide callbacks: ad events for every zone, and add-to-list
 * content delivered outside a zone, such as payloads and deep links, which goes to [payloadHandler].
 */
class AdAdaptedService(
    private val config: AdAdaptedConfig,
    private val activityLog: ActivityLog,
    private val scope: CoroutineScope,
    private val payloadHandler: AddToListHandler,
) : AaSdkEventListener, AaSdkAdditContentListener {

    /** Call once, from [android.app.Application.onCreate]. */
    fun start(context: Context) {
        AdAdapted
            .withAppId(config.appId)
            .inEnv(if (config.isProduction) AdAdapted.Env.PROD else AdAdapted.Env.DEV)
            .enableKeywordIntercept(config.isKeywordInterceptEnabled)
            .enablePayloads(config.arePayloadsEnabled)
            .setCustomIdentifier(config.customIdentifier)
            .setSdkEventListener(this)
            .setSdkAdditContentListener(this)
            .apply { if (config.isDebugLoggingEnabled) enableDebugLogging() }
            .start(context)

        activityLog.log(ActivityCategory.SESSION, "SDK started", "${config.environmentName} · app ${config.appId}")
    }

    override fun onNextAdEvent(zoneId: String, eventType: String) {
        activityLog.log(ActivityCategory.ZONE, "Ad $eventType", "Zone $zoneId")
    }

    override fun onContentAvailable(content: AddToListContent) {
        val offer = AddToListOffer(content)
        activityLog.log(
            ActivityCategory.CONTENT,
            "Payload delivered ${offer.itemNames.size} item(s)",
            offer.itemNames.joinToString().ifEmpty { offer.source },
        )
        scope.launch {
            activityLog.logDelivery(offer.deliverTo(payloadHandler), subject = "Payload", where = offer.source)
        }
    }
}
