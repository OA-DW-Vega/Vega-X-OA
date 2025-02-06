package com.olam.warehouse.vegax.coffeepile.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots

interface VegaCoffeePileManagementAddLotListener {
    fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>)
}
