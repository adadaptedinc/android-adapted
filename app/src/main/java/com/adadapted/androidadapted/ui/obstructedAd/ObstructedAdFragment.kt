package com.adadapted.androidadapted.ui.obstructedAd

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
//import com.adadapted.android.sdk.ui.view.AaZoneView
import com.adadapted.androidadapted.databinding.FragmentObstructedAdBinding

class ObstructedAdFragment : Fragment() {

    private lateinit var obstructedAdViewModel: ObstructedAdViewModel
    private var _binding: FragmentObstructedAdBinding? = null
    //private var obstructedAdZoneView: AaZoneView? = null
    private var scrollView: ScrollView? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        obstructedAdViewModel = ViewModelProvider(this).get(ObstructedAdViewModel::class.java)

        _binding = FragmentObstructedAdBinding.inflate(inflater, container, false)
        val root: View = binding.root

        scrollView = _binding?.scrollView

        scrollView?.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            val scrollBounds = Rect()
            scrollView?.getHitRect(scrollBounds)
//            if (obstructedAdZoneView?.getLocalVisibleRect(scrollBounds) == true) {
//                // Any portion of the imageView, even a single pixel, is within the visible window
//                obstructedAdZoneView?.setAdZoneVisibility(true)
//            } else {
//                obstructedAdZoneView?.setAdZoneVisibility(false)
//            }
        }

//        obstructedAdZoneView = _binding?.obstructedAdZoneView
//        obstructedAdZoneView?.init("101930")
//        obstructedAdZoneView?.setAdZoneVisibility(false)

        return root
    }

    override fun onStart() {
        super.onStart()
        //obstructedAdZoneView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        //obstructedAdZoneView?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}