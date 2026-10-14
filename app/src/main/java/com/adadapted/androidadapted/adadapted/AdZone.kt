package com.adadapted.androidadapted.adadapted

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.adadapted.android.sdk.core.ad.AdContentListener
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.view.AaZoneView

/**
 * An AdAdapted zone, sized to the creative.
 *
 * Each call hosts its own [AaZoneView] and its own SDK listener. The SDK drops a content listener
 * when the zone that registered it stops, so zones can't share one. The zone starts when it enters
 * the composition and stops when it leaves; to request a fresh ad, wrap it in [key] and change
 * the key.
 *
 * @param isVisible whether the zone is on screen. Drives impression tracking and the refresh timer.
 * @param contextId the zone context to request ads for, or empty for none.
 */
@Composable
fun AdZone(
    zoneId: String,
    handler: AdZoneHandler,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    contextId: String = "",
) {
    if (LocalInspectionMode.current) {
        AdZonePreviewPlaceholder(zoneId, modifier)
        return
    }

    key(zoneId) {
        val binding = remember { ZoneBinding(zoneId) }
        SideEffect { binding.handler = handler }

        AndroidView(
            factory = { context ->
                AaZoneView(context).also { view ->
                    view.init(zoneId)
                    view.contentDescription = AD_DESCRIPTION
                    binding.attach(view)
                    // Context first, so the first request already carries it.
                    binding.sync(isVisible, contextId)
                    view.onStart(binding, binding)
                }
            },
            update = { binding.sync(isVisible, contextId) },
            // Not reusable: each zone view belongs to the composition that started it.
            onReset = null,
            onRelease = { view ->
                view.onStop(binding)
                binding.detach()
            },
            modifier = modifier
                .fillMaxWidth()
                .height(AdZoneCatalog.Height),
        )
    }
}

/**
 * Bridges one zone view's SDK callbacks to an [AdZoneHandler], and applies visibility and context
 * changes to the view only when they change, since each context change is reported to the SDK.
 */
private class ZoneBinding(private val zoneId: String) : AaZoneView.Listener, AdContentListener {

    var handler: AdZoneHandler? = null
    private var view: AaZoneView? = null

    // A new zone view starts visible and without a context.
    private var appliedVisibility = true
    private var appliedContextId = ""

    fun attach(view: AaZoneView) {
        this.view = view
        setAccessible(false)
    }

    /**
     * The SDK keeps content listeners it has seen for the life of the process, so drop everything
     * this one points at once its zone is gone.
     */
    fun detach() {
        view = null
        handler = null
    }

    fun sync(isVisible: Boolean, contextId: String) {
        val view = view ?: return
        if (contextId != appliedContextId) {
            if (contextId.isEmpty()) view.removeAdZoneContext() else view.setAdZoneContextId(contextId)
            appliedContextId = contextId
        }
        if (isVisible != appliedVisibility) {
            view.setAdZoneVisibility(isVisible)
            appliedVisibility = isVisible
        }
    }

    // Hidden from accessibility until there's an ad to describe.
    private fun setAccessible(isAccessible: Boolean) {
        view?.importantForAccessibility = if (isAccessible) {
            View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        } else {
            View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
    }

    // region AaZoneView.Listener

    override fun onZoneHasAds(hasAds: Boolean) {
        handler?.onEvent(zoneId, AdZoneEvent.ZoneHasAds(hasAds))
    }

    override fun onAdLoaded() {
        setAccessible(true)
        handler?.onEvent(zoneId, AdZoneEvent.AdLoaded)
    }

    override fun onAdLoadFailed() {
        setAccessible(false)
        handler?.onEvent(zoneId, AdZoneEvent.AdLoadFailed)
    }

    // endregion

    // region AdContentListener. The SDK broadcasts to every registered listener.

    override fun onContentAvailable(zoneId: String, content: AddToListContent) {
        if (zoneId != this.zoneId) return
        handler?.onAddToListOffer(zoneId, AddToListOffer(content))
    }

    override fun onNonContentAction(zoneId: String, adId: String) {
        if (zoneId != this.zoneId) return
        handler?.onEvent(zoneId, AdZoneEvent.NonContentAction(adId))
    }

    // endregion
}

@Composable
private fun AdZonePreviewPlaceholder(zoneId: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AdZoneCatalog.Height),
        contentAlignment = Alignment.Center,
    ) {
        Text("Ad zone $zoneId", style = MaterialTheme.typography.labelMedium)
    }
}

private const val AD_DESCRIPTION = "Advertisement"
