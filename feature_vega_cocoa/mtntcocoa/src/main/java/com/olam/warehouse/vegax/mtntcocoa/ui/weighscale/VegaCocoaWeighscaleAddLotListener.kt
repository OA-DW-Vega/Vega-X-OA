package com.olam.warehouse.vegax.mtntcocoa.ui.weighscale

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots

interface VegaCocoaWeighscaleAddLotListener {
    fun addedLotsWeighscale(lots: ArrayList<VegaCocoaDispatchLots>)
}
