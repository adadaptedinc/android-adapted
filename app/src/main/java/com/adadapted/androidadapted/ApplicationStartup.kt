package com.adadapted.androidadapted

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.adadapted.android.sdk.AdAdapted
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.atl.AddToListItem
import com.adadapted.android.sdk.ui.messaging.AaSdkAdditContentListener
import com.adadapted.android.sdk.ui.messaging.AaSdkEventListener
import com.adadapted.android.sdk.ui.messaging.AaSdkSessionListener
import java.util.Locale

class ApplicationStartup: Application() {

    override fun onCreate() {
        super.onCreate()
        val tag = "AADroid"

        //AdAdapted.INSTANCE.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("846ACA0X62F13A62") // #YOUR API KEY GOES HERE# 846ACA0X62F13A62 NWY0NTM2YZDMMDQ0
            .inEnv(AdAdapted.Env.DEV)
            .setSdkSessionListener(object : AaSdkSessionListener {
                override fun onHasAdsToServe(hasAds: Boolean) {
                    Log.i(tag, "Has Ads To Serve: $hasAds")
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