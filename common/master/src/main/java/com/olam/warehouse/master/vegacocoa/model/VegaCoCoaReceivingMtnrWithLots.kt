package com.olam.warehouse.master.vegacocoa.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiving

/**
 * Created by Baskaran Kannan on 9/10/2020.
 */
class VegaCoCoaReceivingMtnrWithLots {
    @Embedded
    lateinit var receiving: VegaCoCoaReceiving

    @Relation(
        parentColumn = "delivery",
        entityColumn = "delivery",
        entity = VegaCoCoaReceiveLots::class
    )
    var lineItems: List<ReceivingLotsWithBagItem> = emptyList()
}

class ReceivingLotsWithBagItem {
    @Embedded
    lateinit var lots: VegaCoCoaReceiveLots

    @Relation(
        parentColumn = "batch",
        entityColumn = "batchNumber",
        entity = VegaCoCoaOffloadingBagMaterial::class
    )
    var bagItem: List<VegaCoCoaOffloadingBagMaterial> = emptyList()
}
