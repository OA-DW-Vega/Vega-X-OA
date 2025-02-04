package com.olam.warehouse.vegax.inventoryghana.ui

import com.olam.warehouse.vegax.inventoryghana.data.domain.model.StorageLoc

interface VegaGhanaInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
