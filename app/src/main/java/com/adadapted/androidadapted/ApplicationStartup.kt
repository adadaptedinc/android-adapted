package com.adadapted.androidadapted

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.adadapted.android.sdk.AdAdapted
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.atl.AddToListItem
import com.adadapted.android.sdk.core.interfaces.AaSdkAdditContentListener
import com.adadapted.android.sdk.core.interfaces.AaSdkEventListener
import com.adadapted.android.sdk.core.interfaces.AaSdkSessionListener
import java.util.Locale

class ApplicationStartup: Application() {

    override fun onCreate() {
        super.onCreate()
        val tag = "AADroid"
        //AdAdapted.INSTANCE.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("7D58810X6333241C") // #YOUR API KEY GOES HERE# 846ACA0X62F13A62 NWY0NTM2YZDMMDQ0
            .inEnv(AdAdapted.Env.DEV)
            .enableKeywordIntercept(true)
            .setSdkSessionListener(object : AaSdkSessionListener {
                override fun onHasAdsToServe(hasAds: Boolean, availableZoneIds: List<String>) {
                    Log.i(tag, "Has Ads To Serve: $hasAds")
                    Log.i(tag, "The following zones have ads to serve: $availableZoneIds")
                }
            })
            .setSdkEventListener(object : AaSdkEventListener {
                override fun onNextAdEvent(zoneId: String, eventType: String) {
                    Log.i(tag, "Ad $eventType for Zone $zoneId")
                }
            })
            .setSdkAdditContentListener(object : AaSdkAdditContentListener {
                override fun onContentAvailable(content: AddToListContent) {
                    val listItems: List<AddToListItem> = content.getItems()
                    Toast.makeText(
                                applicationContext,
                                String.format(
                                    Locale.ENGLISH,
                                    "%d item(s) received from payload or circular.",
                                    listItems.size
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                }
            })
            .start(this)
    }
}