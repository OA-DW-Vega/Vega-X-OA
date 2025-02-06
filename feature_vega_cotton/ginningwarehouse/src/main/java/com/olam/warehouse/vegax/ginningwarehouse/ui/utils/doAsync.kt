package com.olam.warehouse.vegax.ginningwarehouse.ui.utils

import android.os.AsyncTask

/**
 * Created by SangiliPandian C on 28-02-2019.
 */
class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}
