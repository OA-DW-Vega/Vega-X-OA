package com.olam.warehouse.vegax.inventorycameroon.ui

import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.StorageLoc

interface VegaCameroonInventoryNavigateToDetailsListener {
    fun navigateToDetails(storage: StorageLoc, colorCode: Int)
}
