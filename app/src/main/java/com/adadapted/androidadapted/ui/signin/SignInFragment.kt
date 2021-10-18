package com.adadapted.androidadapted.ui.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentSigninBinding

class SignInFragment : Fragment() {

    private lateinit var signInViewModel: SignInViewModel
    private var _binding: FragmentSigninBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        signInViewModel =
            ViewModelProvider(this).get(SignInViewModel::class.java)

        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textSignIn
//        signInViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}