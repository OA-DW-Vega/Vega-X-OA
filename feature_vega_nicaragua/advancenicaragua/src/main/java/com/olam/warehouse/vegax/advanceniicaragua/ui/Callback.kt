package com.olam.warehouse.vegax.advanceniicaragua.ui

import android.os.Bundle

interface Callback {

    fun replaceFragment(moveFrag: String)
    fun replaceFragment(
        moveFrag: String,
        data: Bundle
    )
}
