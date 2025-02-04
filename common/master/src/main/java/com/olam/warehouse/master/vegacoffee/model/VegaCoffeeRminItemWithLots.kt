package com.olam.warehouse.master.vegacoffee.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing

/**
 * Created by Baskaran Kannan on 6/18/2020.
 */

class VegaCoffeeRminItemWithLots {
    @Embedded
    lateinit var rminItem: VegaCoffeeRminProcessing

    @Relation(
        parentColumn = "rminId",
        entityColumn = "rminId",
        entity = VegaCoffeeRminLots::class
    )
    var lots: List<VegaCoffeeRminLots>? = emptyList()
}
