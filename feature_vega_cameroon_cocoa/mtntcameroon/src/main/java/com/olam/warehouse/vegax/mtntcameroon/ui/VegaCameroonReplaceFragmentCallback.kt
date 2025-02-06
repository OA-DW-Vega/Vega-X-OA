package com.olam.warehouse.vegax.mtntcameroon.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB

interface VegaCameroonReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )
    fun replaceFragment(receivingType: String,data: Any,list: VegaCocoaDispatchWB)
}
