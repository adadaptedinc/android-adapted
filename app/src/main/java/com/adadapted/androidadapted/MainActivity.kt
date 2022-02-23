package com.adadapted.androidadapted

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adadapted.androidadapted.databinding.ActivityMainBinding
import com.adadapted.library.DeviceInfo
import com.adadapted.library.Greeting
import com.adadapted.library.constants.Config
import com.adadapted.library.network.HttpSessionAdapter
import com.adadapted.library.session.Session
import com.adadapted.library.session.SessionAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_list, R.id.nav_obstructed_ad, R.id.nav_sign_in
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // kmm test lib implementation
        var check = Greeting().greeting()
        var doubleCheck = check
        testKmmSessionConnection()
    }

    fun testKmmSessionConnection() {
        val httpSessionAdapter = HttpSessionAdapter(Config.getInitSessionUrl(), Config.getRefreshAdsUrl())
        val deviceInfo = DeviceInfo(
            appId = "NWY0NTM2YZDMMDQ0",
            udid = "1234567890",
            device = "Android Emulator",
            deviceUdid = "12345",
            os = "Android",
            isAllowRetargetingEnabled = true,
            sdkVersion = "0.0.8"
        )
        val listener = (object : SessionAdapter.SessionInitListener {
            override fun onSessionInitialized(session: Session) {
                var returnedSession = session
            }

            override fun onSessionInitializeFailed() {
               var uhOh = "notgood"
            }
        })
        lifecycleScope.launch {
            httpSessionAdapter.sendInit(deviceInfo, listener)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}