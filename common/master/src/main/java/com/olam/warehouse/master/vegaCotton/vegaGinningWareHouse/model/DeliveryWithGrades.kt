package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Grade
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery

/**
 * Created by Baskaran Kannan on 3/23/2020.
 */
class DeliveryWithGrades {
    @Embedded
    lateinit var deliveryVegaCotton: VegaCottonGinningDispatchDelivery

    @Relation(
        parentColumn = "deliveryNumber",
        entityColumn = "deliveryNumber",
        entity = Grade::class
    )
    var grades: List<Grade> = emptyList()
}
