package com.olam.warehouse.vegax.salescocoa.ui

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots

interface ItemRemoveListener {
    fun itemRemoved(item: VegaCocoaSalesLots)
    fun weightUpdated(item: VegaCocoaSalesLots)
}
