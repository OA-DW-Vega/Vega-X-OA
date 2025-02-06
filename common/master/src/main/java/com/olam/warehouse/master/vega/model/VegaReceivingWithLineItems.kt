package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
class VegaReceivingWithLineItems {
    @Embedded
    lateinit var receiving: VegaReceiving

    @Relation(parentColumn = "tmpWbId", entityColumn = "tmpWbId", entity = VegaReceivingLineItem::class)
    var lineItems: List<VegaReceivingLineItem> = emptyList()
}

class VegaCoffeeTruckOutReceivingWithBags {
    @Embedded
    lateinit var receiving: VegaReceiving

    @Relation(
        parentColumn = "weighBridgeId",
        entityColumn = "baseMaterial",
        entity = VegaCoffeeOffloadingBagMaterial::class
    )
    var bags: List<VegaCoffeeOffloadingBagMaterial> = emptyList()
}
