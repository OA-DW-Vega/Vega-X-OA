package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery

/**
 * Created by Baskaran Kannan on 3/31/2020.
 */

class DeliveryWithBales {
    @Embedded
    lateinit var delivery: MtnDispatchDelivery

    @Relation(
        parentColumn = "deliveryNumber",
        entityColumn = "deliveryNumber",
        entity = MtnBale::class
    )
    var bales: List<MtnBale> = emptyList()
}
