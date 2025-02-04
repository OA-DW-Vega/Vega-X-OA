package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.StorageLoc

interface VegaGhanaCocoaInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
