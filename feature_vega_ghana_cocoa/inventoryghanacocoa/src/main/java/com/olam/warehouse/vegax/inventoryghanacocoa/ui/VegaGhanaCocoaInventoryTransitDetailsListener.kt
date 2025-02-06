package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots

interface VegaGhanaCocoaInventoryTransitDetailsListener {
    fun navigateTransitToDetails(vegaReceivingMtnLots: VegaReceivingMtnLots, colorCode: Int)
}
