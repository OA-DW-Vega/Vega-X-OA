package com.olam.warehouse.vegax.mtntghana.ui

interface VegaGhanaReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )

    fun replaceFragment(
        receivingType: String
    )
}
