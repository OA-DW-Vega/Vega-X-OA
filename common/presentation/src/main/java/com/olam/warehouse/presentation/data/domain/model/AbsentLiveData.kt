package com.olam.warehouse.presentation.data.domain.model

import androidx.lifecycle.LiveData

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */

class AbsentLiveData<T : Any?> private constructor() : LiveData<T>() {
    init {
        // use post instead of set since this can be created on any thread
        postValue(null)
    }

    companion object {
        fun <T> create(): LiveData<T> {
            return AbsentLiveData()
        }
    }
}
