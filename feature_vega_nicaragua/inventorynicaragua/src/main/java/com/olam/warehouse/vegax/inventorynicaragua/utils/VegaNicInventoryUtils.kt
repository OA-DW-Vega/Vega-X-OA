package com.olam.warehouse.vegax.inventorynicaragua.utils

import android.os.Build
import com.olam.warehouse.vegax.App

/**
 * Created by Baskaran Kannan on 12/12/2020.
 */

const val SYNC_STATUS_DATA = "sync_status_list"

@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}
