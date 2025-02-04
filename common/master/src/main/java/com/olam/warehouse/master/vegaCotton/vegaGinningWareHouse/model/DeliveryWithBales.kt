package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery

/**
 * Created by Baskaran Kannan on 3/23/2020.
 */
class DeliveryWithBales {
    @Embedded
    lateinit var deliveryVegaCotton: VegaCottonGinningDispatchDelivery

    @Relation(
        parentColumn = "deliveryNumber",
        entityColumn = "deliveryNumber",
        entity = Bale::class
    )
    var bales: List<Bale> = emptyList()
}
