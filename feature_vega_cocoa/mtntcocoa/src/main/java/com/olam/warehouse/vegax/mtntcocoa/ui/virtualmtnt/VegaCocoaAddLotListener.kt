package com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot

interface VegaCocoaAddLotListener {
    fun addedLots(lots: ArrayList<VegaCocoaNoWeighmentLot>)
}

interface UpdateSelectedLotWeightListener {
    fun updateLotWeight(count: String, weight: String)
}

