package com.adadapted.androidadapted

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.ui.AndroidAdaptedApp
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme

class MainActivity : ComponentActivity() {

    private val container
        get() = (application as AndroidAdaptedApplication).container

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        container.activityLog.log(ActivityCategory.SESSION, if (isGranted) "Notifications allowed" else "Notifications denied")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The bottom bar is black in both themes, so the gesture handle stays light.
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            requestNotificationPermissionIfNeeded()
        }

        setContent {
            AndroidAdaptedTheme {
                AndroidAdaptedApp(container)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(permission)
        }
    }
}
