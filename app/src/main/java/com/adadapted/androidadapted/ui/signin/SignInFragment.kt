package com.adadapted.androidadapted.ui.signin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.ads.identifier.AdvertisingIdClient
import androidx.ads.identifier.AdvertisingIdInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentSigninBinding
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures.addCallback
import java.io.IOException
import java.util.concurrent.Executors
import android.widget.Toast
import kotlinx.coroutines.*
import java.net.URL
import java.util.*

class SignInFragment : Fragment() {

    private lateinit var signInViewModel: SignInViewModel
    private var _binding: FragmentSigninBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        signInViewModel =
            ViewModelProvider(this).get(SignInViewModel::class.java)

        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val signInAndSubmitButton = binding.signInAndSubmitButton
        signInAndSubmitButton.setOnClickListener {
            val deviceAdId = getDeviceAdId()

            CoroutineScope(Dispatchers.IO).launch {
                val deviceIp = getIp()
                toastOnMainThread(deviceAdId, deviceIp)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDeviceAdId(): String {
        var adId = "12345"
        if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(requireContext())) {
            val advertisingIdInfoListenableFuture =
                requireContext().let { AdvertisingIdClient.getAdvertisingIdInfo(it) }
            addCallback(
                advertisingIdInfoListenableFuture,
                object : FutureCallback<AdvertisingIdInfo> {
                    override fun onSuccess(adInfo: AdvertisingIdInfo?) {
                        if (adInfo != null) {
                            adId = adInfo.id
                        }
                    }

                    override fun onFailure(t: Throwable) {
                        Log.e("FAIL:", "Failed to connect to Advertising ID provider.")
                    }
                }, Executors.newSingleThreadExecutor()
            )
        }
        return adId
    }

    private suspend fun toastOnMainThread(deviceAdId: String, deviceIp: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Ready to send adId: $deviceAdId and IP address: $deviceIp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getIp(): String {
        var publicIP = ""
        try {
            val s = Scanner(
                URL(
                    "https://api.ipify.org"
                )
                    .openStream(), "UTF-8"
            )
                .useDelimiter("\\A")
            publicIP = s.next()
            return publicIP

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return publicIP
    }
}