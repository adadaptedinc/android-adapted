package com.adadapted.androidadapted

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.adadapted.library.AdAdapted
import com.adadapted.library.AdAdaptedEnv
import java.util.Locale

class ApplicationStartup: Application() {

    override fun onCreate() {
        super.onCreate()
        val tag = "AADroidTestApp"


        //AdAdapted.disableAdTracking(this); //Disable ad tracking completely
        AdAdapted
            .withAppId("NWY0NTM2YZDMMDQ0") // #YOUR API KEY GOES HERE#   NTKXMZFJZTA2NMZJ
            .inEnvironment(AdAdaptedEnv.DEV)
            .onHasAdsToServe {
                Log.i(tag, "Has Ads To Serve: $it")
            }
//            .setSdkSessionListener(object : AaSdkSessionListener {
//                override fun onHasAdsToServe(hasAds: Boolean) {
//                    Log.i(tag, "Has Ads To Serve: $hasAds")
//                }
//            })
//            .setSdkEventListener(object : AaSdkEventListener {
//                override fun onNextAdEvent(zoneId: String, eventType: String) {
//                    Log.i(tag, "Ad $eventType for Zone $zoneId")
//                }
//            })
//            .setSdkAdditContentListener(object : AaSdkAdditContentListener {
//                override fun onContentAvailable(content: AddToListContent) {
//                    val listItems: List<AddToListItem> = content.getItems()
//                    Toast.makeText(
//                                applicationContext,
//                                String.format(
//                                    Locale.ENGLISH,
//                                    "%d item(s) received from payload or circular.",
//                                    listItems.size
//                                ),
//                                Toast.LENGTH_LONG
//                            ).show()
//                }
//            })
            .start(this)
    }
}