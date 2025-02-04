package com.olam.warehouse.vegax.processingsesame.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface SesameRminItemRemoveListener {
    fun itemRemoved(item: VegaCoffeeRminLots)
    fun weightUpdated(item: VegaCoffeeRminLots)
}

interface SesameRminAddWeightListener {
    fun addWeightForLot(pos: Int, item: VegaCoffeeRminLots)
}
