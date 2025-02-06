package com.olam.warehouse.vegax.processingghana.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface GhanaRminItemRemoveListener {
    fun itemRemoved(item: VegaCoffeeRminLots)
    fun weightUpdated(item: VegaCoffeeRminLots)
}

interface GhanaRminAddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots)
    fun calculateWtp()
    fun updateAddWeightCal(weight: String)
}
