package com.olam.warehouse.vegax.offloadingghana.ui


interface VegaGhanaOffloadReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )

    fun replaceFragment(moveFrag: String)
}
