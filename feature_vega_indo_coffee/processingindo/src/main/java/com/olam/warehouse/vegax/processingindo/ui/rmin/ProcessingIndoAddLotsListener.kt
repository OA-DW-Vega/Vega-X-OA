package com.olam.warehouse.vegax.processingindo.ui.rmin

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots

interface ProcessingIndoAddLotsListener {
    fun addedLots(lots: ArrayList<VegaCocoaRminLots>)
}
