package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks

/**
 * Created by Baskaran Kannan on 5/13/2020.
 */
class VegaDispatchWithLots {

    @Embedded
    lateinit var dispatch: VegaDispatchTrucks
    @Relation(parentColumn = "weighBridgeId", entityColumn = "weighBridgeId", entity = VegaDispatchLots::class)
    var lineItems: List<VegaDispatchLots> = emptyList()
}
