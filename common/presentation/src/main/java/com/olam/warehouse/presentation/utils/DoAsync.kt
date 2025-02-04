package com.olam.warehouse.presentation.utils

import android.os.AsyncTask

/**
 * Created by SangiliPandian C on 16-11-2019.
 */
class DoAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}
