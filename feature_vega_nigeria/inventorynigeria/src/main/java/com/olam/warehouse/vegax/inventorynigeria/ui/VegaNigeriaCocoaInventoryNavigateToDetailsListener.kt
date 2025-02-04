package com.olam.warehouse.vegax.inventorynigeria.ui

import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.StorageLoc


interface VegaNigeriaCocoaInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
