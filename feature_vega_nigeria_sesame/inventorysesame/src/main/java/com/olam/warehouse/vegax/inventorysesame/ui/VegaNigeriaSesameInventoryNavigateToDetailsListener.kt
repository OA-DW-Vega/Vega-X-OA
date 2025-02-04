package com.olam.warehouse.vegax.inventorysesame.ui

import com.olam.warehouse.vegax.inventorysesame.data.domain.model.StorageLoc

interface VegaNigeriaSesameInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
