package com.olam.warehouse.vegax.mtntghanacocoa.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB

interface VegaGhanaCocoaReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )
    fun replaceFragment(receivingType: String,data: Any,list: VegaCocoaDispatchWB)

    fun replaceFragment(
        receivingType: String
    )
}
