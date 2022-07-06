package com.adadapted.androidadapted

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.adadapted.library.AdAdapted
import com.adadapted.library.AdAdaptedEnv
import com.adadapted.library.atl.AddToListContent
import com.adadapted.library.interfaces.AddItContentListener
import com.adadapted.library.interfaces.EventBroadcastListener

class ApplicationStartup : Application() {
    override fun onCreate() {
        super.onCreate()
        val tag = "AADroidTestApp"

        //AdAdapted.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("NWY0NTM2YZDMMDQ0") // #YOUR API KEY GOES HERE# //NWY0NTM2YZDMMDQ0 test
            .inEnvironment(AdAdaptedEnv.DEV)
            .enableKeywordIntercept(true)
            .enableDebugLogging()
            .onHasAdsToServe {
                Log.i(tag, "Has Ads To Serve: $it")
            }
            .setSdkEventListener(object: EventBroadcastListener {
                override fun onAdEventTracked(zoneId: String, eventType: String) {
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