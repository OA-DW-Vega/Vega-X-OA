package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.os.Bundle

interface Callback {

    fun replaceFragment(moveFrag: String)
    fun replaceFragment(
        moveFrag: String,
        data: Bundle
    )
}
