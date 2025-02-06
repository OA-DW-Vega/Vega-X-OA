package com.olam.warehouse.vegax.dispatchnigeria.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB

interface VegaNigeriaCocoaReplaceFragmentCallback {
    fun replaceFragment(
        receivingType: String,
        data: VegaCocoaDispatchWB?,
        datanew: String
    )

    fun replaceFragment(
        receivingType: String,
        data: Any
    )

    fun replaceFragmentNew(
        receivingType: String,
        data: VegaCocoaDispatchWB?
    )

    fun replaceFragment(
        receivingType: String,
        materialCode: String,
        mergedBatchNumber: String, data: Any, datanew: Any
    )
}
