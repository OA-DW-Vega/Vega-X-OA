package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB

interface VegaNigeriaApprovalReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: Any
    )
    fun replaceFragment(receivingType: String,data: Any,list: VegaCocoaDispatchWB)

    fun replaceFragment(
        receivingType: String
    )
}
