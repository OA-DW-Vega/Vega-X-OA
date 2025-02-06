package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel

interface VegaGhanaCocoaAddLotListener {
    fun addedLots(lots: ArrayList<VegaGRNDWLotManualModel>)
}
