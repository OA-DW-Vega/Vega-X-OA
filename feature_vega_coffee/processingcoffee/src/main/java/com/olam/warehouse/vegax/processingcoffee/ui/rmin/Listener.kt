package com.olam.warehouse.vegax.processingcoffee.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface CoffeeRminItemRemoveListener {
    fun itemRemoved(item: VegaCoffeeRminLots)
    fun weightUpdated(item: VegaCoffeeRminLots)
}

interface CoffeeRminAddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots)
}
