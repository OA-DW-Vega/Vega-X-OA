package com.olam.warehouse.vegax.inventoryecuador.ui

import com.olam.warehouse.vegax.inventoryecuador.data.domain.model.StorageLoc

interface VegaEcuadorInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
