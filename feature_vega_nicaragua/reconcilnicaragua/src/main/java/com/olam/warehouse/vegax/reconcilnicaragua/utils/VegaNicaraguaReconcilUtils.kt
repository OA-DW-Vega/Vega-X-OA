package com.olam.warehouse.vegax.reconcilnicaragua.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 10/12/2020.
 */

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
