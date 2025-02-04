package com.olam.warehouse.vegax.salescocoa.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots

interface AddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCocoaSalesLots)
}
