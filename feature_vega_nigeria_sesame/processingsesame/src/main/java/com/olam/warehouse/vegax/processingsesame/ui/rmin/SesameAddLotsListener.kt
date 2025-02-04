package com.olam.warehouse.vegax.processingsesame.ui.rmin

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots

interface SesameAddLotsListener {
    fun addedLots(lots: ArrayList<VegaCoffeeRminLots>)
}
