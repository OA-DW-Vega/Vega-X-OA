package com.olam.warehouse.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.olam.warehouse.presentation.utils.Event

abstract class BaseViewModel : ViewModel() {

    // FOR ERROR HANDLER
    private val _loading = MutableLiveData<Event<Boolean>>()
    val loading: LiveData<Event<Boolean>> get() = _loading

    fun showLoading(flag: Boolean) {
        _loading.value = Event(flag)
    }
}
