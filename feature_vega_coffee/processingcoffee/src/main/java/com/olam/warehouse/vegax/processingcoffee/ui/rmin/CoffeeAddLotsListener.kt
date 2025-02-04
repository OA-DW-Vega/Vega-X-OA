package com.olam.warehouse.vegax.processingcoffee.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface CoffeeAddLotsListener {
    fun addedLots(lots: ArrayList<VegaCoffeeRminLots>)
}
