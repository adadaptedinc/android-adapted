package com.adadapted.androidadapted.ui.obstructedAd

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ObstructedAdViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is obstructed ad Fragment"
    }
    val text: LiveData<String> = _text
}