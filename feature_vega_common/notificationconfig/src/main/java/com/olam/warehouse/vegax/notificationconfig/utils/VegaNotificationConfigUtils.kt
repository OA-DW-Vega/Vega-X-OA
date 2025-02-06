package com.olam.warehouse.vegax.notificationconfig.utils

import android.os.Build
import com.olam.warehouse.vegax.App


const val BUNDLE_DATA = "bundle_data"
const val MODULE_LIST_FRAGMENT = "module list fragment"


@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}



