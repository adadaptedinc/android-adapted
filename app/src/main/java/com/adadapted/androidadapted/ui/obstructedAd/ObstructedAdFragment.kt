package com.adadapted.androidadapted.ui.obstructedAd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentObstructedAdBinding

class ObstructedAdFragment : Fragment() {

    private lateinit var obstructedAdViewModel: ObstructedAdViewModel
    private var _binding: FragmentObstructedAdBinding? = null

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

        val textView: TextView = binding.textObstructedAd
        obstructedAdViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}