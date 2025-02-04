package com.olam.warehouse.vegax.inventoryindiacoffee.ui

import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.StorageLoc

interface VegaIndiaCoffeeInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
