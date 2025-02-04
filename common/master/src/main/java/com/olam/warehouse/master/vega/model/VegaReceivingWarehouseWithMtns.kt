package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
class VegaReceivingWarehouseWithMtns {
    @Embedded
    lateinit var warehouse: VegaSupplyStorageLocation
    @Relation(
        parentColumn = "storageLocationCode",
        entityColumn = "storageLocationCode",
        entity = VegaReceivingMtn::class
    )
    var mtns: List<VegaReceivingMtnWithLots> = emptyList()
}

class VegaCoffeeReceivingWarehouseWithMtns {
    @Embedded
    lateinit var warehouse: VegaSupplyStorageLocation

    @Relation(
        parentColumn = "storageLocationCode",
        entityColumn = "storageLocationCode",
        entity = VegaReceivingMtn::class
    )
    var mtns: List<VegaCocoaDispatchLots> = emptyList()
}
