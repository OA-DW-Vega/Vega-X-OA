package com.olam.warehouse.vegax.processingnigeria.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface NigeriaRminItemRemoveListener {
    fun itemRemoved(item: VegaCoffeeRminLots)
    fun weightUpdated(item: VegaCoffeeRminLots)
}

interface NigeriaRminAddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots)
}
