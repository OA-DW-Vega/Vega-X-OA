package com.olam.warehouse.vegax.processingcameroon.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface CameroonRminItemRemoveListener {
    fun itemRemoved(item: VegaCoffeeRminLots)
    fun weightUpdated(item: VegaCoffeeRminLots)
}

interface CameroonRminAddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots)
}
