package com.adadapted.androidadapted

import android.app.Application
import android.os.Handler
import android.os.Looper
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
            .withAppId("NWY0NTM2YZDMMDQ0") // #YOUR API KEY GOES HERE#   NTKXMZFJZTA2NMZJ //OG Prod NTDMZJK2NTM2YWZH //NWY0NTM2YZDMMDQ0 test
            .inEnvironment(AdAdaptedEnv.DEV)
            .enableKeywordIntercept(true)
            .onHasAdsToServe {
                Log.i(tag, "Has Ads To Serve: $it")
            }
            .setSdkEventListener { zoneId, eventType ->
                Log.i(tag, "Ad $eventType for Zone $zoneId")
            }
            .setSdkAddItContentListener {
                val listItems: List<AddToListItem> = it.getItems()
                it.itemAcknowledge(listItems.first())
                it.acknowledge()

                Handler(Looper.getMainLooper()).post {
                    AddToListItemCache.items.value = listItems
                    Toast.makeText(this.applicationContext, "Received item: " + listItems.first().title, Toast.LENGTH_SHORT).show()
                }
            }
            .start(this)
    }
}