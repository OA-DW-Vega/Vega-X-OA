package com.olam.warehouse.vegax.inventorycoffee.utils

import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.CoffeeStorageLoc

interface CoffeeNavigateToDetailsListener {
    fun navigateToDetails(storage: CoffeeStorageLoc, colorCode: Int)
}
