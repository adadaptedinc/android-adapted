package com.adadapted.androidadapted.ui.zonelab.scroller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.adadapted.AdZoneHandlerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScrollerViewModel(adZoneHandlerFactory: AdZoneHandlerFactory) : ViewModel() {

    /** No list is attached: every ad row shares a zone ID, so each would add an ad's items. */
    val zoneHandler: AdZoneHandler = adZoneHandlerFactory.create(viewModelScope, addToList = null)

    private val _rowCount = MutableStateFlow(PAGE_SIZE)
    val rowCount: StateFlow<Int> = _rowCount.asStateFlow()

    fun loadMore() {
        _rowCount.update { it + PAGE_SIZE }
    }

    companion object {
        const val PAGE_SIZE = 32
        const val AD_INTERVAL = 8

        fun isAdRow(index: Int): Boolean = index % AD_INTERVAL == 0
    }
}
