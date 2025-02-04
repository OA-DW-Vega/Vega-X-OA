package com.olam.warehouse.vegax.inventory.ui

import com.olam.warehouse.vegax.inventory.data.domain.model.StorageLoc

interface NavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
