package com.adadapted.androidadapted

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.adadapted.android.sdk.AdAdapted
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.atl.AddToListItem
import com.adadapted.android.sdk.core.interfaces.AaSdkAdditContentListener
import com.adadapted.android.sdk.core.interfaces.AaSdkEventListener
import java.util.Locale

class ApplicationStartup: Application() {

class ApplicationStartup : Application() {
    override fun onCreate() {
        super.onCreate()
        val tag = "AADroid"
        //AdAdapted.INSTANCE.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("7D58810X6333241C") // #YOUR API KEY GOES HERE# 846ACA0X62F13A62 NWY0NTM2YZDMMDQ0 NJIXNMRHZDC5ODCY - flipp
            .inEnv(AdAdapted.Env.DEV)
            .enableKeywordIntercept(true)
            .enableDebugLogging()
//            .setSdkSessionListener(object: AaSdkSessionListener {
//                override fun onHasAdsToServe(
//                    hasAds: Boolean,
//                    availableZoneIds: List<String>
//                ) {
//                    var check = 0
//                }
//
//            })
            .setSdkEventListener(object : AaSdkEventListener {
                override fun onNextAdEvent(zoneId: String, eventType: String) {
                    Log.i(tag, "Ad $eventType for Zone $zoneId")
                }
            })
            .setSdkAddItContentListener(object: AddItContentListener{
                override fun onContentAvailable(content: AddToListContent) {
                    val listItems = content.getItems()
                    content.itemAcknowledge(listItems.first())
                    content.acknowledge()

                    Handler(Looper.getMainLooper()).post {
                        for (item in listItems) {
                            AddToListItemCache.holdingItems.add(item)
                        }
                        AddToListItemCache.takeHoldingItems()
                        Toast.makeText(applicationContext, "Received item: " + listItems.first().title, Toast.LENGTH_SHORT).show()
                    }
                }
            })
            .start(this)
    }
}