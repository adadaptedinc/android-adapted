package com.adadapted.androidadapted

import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        (application as AndroidAdaptedApplication).container.activityLog
            .log(ActivityCategory.SESSION, "Push token refreshed", token)
    }
}
