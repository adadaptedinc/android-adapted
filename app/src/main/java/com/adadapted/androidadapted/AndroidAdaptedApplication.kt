package com.adadapted.androidadapted

import android.app.Application
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.di.AppContainer
import com.adadapted.androidadapted.di.DefaultAppContainer
import com.google.firebase.messaging.FirebaseMessaging

class AndroidAdaptedApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val container = DefaultAppContainer(this)
        this.container = container

        container.adAdaptedService.start(this)
        logPushToken()
    }

    /** Push messages can deliver add-to-list payloads; the token identifies this install for testing them. */
    private fun logPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                container.activityLog.log(ActivityCategory.SESSION, "Push token received", task.result)
            } else {
                container.activityLog.log(ActivityCategory.ERROR, "Push token unavailable", task.exception?.message)
            }
        }
    }
}
