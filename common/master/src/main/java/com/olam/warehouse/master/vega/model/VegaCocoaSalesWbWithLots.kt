package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB

class VegaCocoaSalesWbWithLots {
    @Embedded
    lateinit var dispatch: VegaCocoaSalesWB

    @Relation(parentColumn = "weighBridgeId", entityColumn = "weighBridgeId", entity = VegaCocoaSalesLots::class)
    var lineItems: List<VegaCocoaSalesLots> = emptyList()
}
