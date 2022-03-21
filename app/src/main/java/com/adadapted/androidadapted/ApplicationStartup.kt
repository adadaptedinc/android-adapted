package com.adadapted.androidadapted

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.adadapted.library.AdAdapted
import com.adadapted.library.AdAdaptedEnv
import com.adadapted.library.atl.AddToListItem

class ApplicationStartup : Application() {

    override fun onCreate() {
        super.onCreate()
        val tag = "AADroidTestApp"


        //AdAdapted.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("NWY0NTM2YZDMMDQ0") // #YOUR API KEY GOES HERE#   NTKXMZFJZTA2NMZJ
            .inEnvironment(AdAdaptedEnv.DEV)
            .enableKeywordIntercept(true)
            .onHasAdsToServe {
                Log.i(tag, "Has Ads To Serve: $it")
            }
//            .setSdkEventListener(object : AaSdkEventListener {
//                override fun onNextAdEvent(zoneId: String, eventType: String) {
//                    Log.i(tag, "Ad $eventType for Zone $zoneId")
//                }
//            })
            .setSdkAddItContentListener {
                val listItems: List<AddToListItem> = it.getItems()

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this.applicationContext, "Received item: " + listItems.first().title, Toast.LENGTH_SHORT).show()
                }
            }
            .start(this)
    }
}