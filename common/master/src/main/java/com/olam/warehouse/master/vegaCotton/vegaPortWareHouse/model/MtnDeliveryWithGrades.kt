package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnGrade

/**
 * Created by Baskaran Kannan on 3/31/2020.
 */

class MtnDeliveryWithGrades {
    @Embedded
    lateinit var delivery: MtnDispatchDelivery

    @Relation(
        parentColumn = "deliveryNumber",
        entityColumn = "deliveryNumber",
        entity = MtnGrade::class
    )
    var grades: List<MtnGrade> = emptyList()
}
