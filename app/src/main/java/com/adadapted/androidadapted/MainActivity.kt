package com.adadapted.androidadapted

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adadapted.androidadapted.ui.screens.GroceryListScreen
import com.adadapted.androidadapted.ui.screens.LandingScreen
import com.adadapted.androidadapted.ui.screens.ScrollerScreen
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            Log.d("FCM", task.result.toString())
        })

        askNotificationPermission()

        setContent {
            AndroidAdaptedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "landing"
                    ) {
                        composable("landing") {
                            LandingScreen(
                                onNavigateToGroceryList = { navController.navigate("grocery_list") },
                                onNavigateToScroller = { navController.navigate("scroller") }
                            )
                        }
                        composable("grocery_list") {
                            GroceryListScreen(onBack = { navController.popBackStack() })
                        }
                        composable("scroller") {
                            ScrollerScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}
