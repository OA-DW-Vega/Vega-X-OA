package com.olam.warehouse.presentation.ui.wifiprinter

import com.example.mywifiprinter.observers.Observable
import com.example.mywifiprinter.observers.ObservableImpl

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
class ObservableSingleton {
    companion object {
        var mObservable: Observable? = null
        fun initInstance() {
            if (mObservable == null) {
                mObservable = ObservableImpl()
            }
        }

        fun getInstance(): Observable? {
            return mObservable
        }
    }
}
