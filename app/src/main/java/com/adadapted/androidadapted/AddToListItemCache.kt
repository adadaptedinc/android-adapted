package com.adadapted.androidadapted

import androidx.lifecycle.MutableLiveData
import com.adadapted.library.atl.AddToListItem

object AddToListItemCache {
    val items: MutableLiveData<List<AddToListItem>> by lazy {
        MutableLiveData<List<AddToListItem>>()
    }
}