package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB

class VegaCoffeeMtntWithLots {

    @Embedded
    lateinit var dispatch: VegaCocoaDispatchWB

    @Relation(parentColumn = "weighBridgeId", entityColumn = "weighBridgeId", entity = VegaCocoaDispatchLots::class)
    var lineItems: List<VegaCocoaDispatchLots> = emptyList()
}
