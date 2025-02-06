package com.olam.warehouse.master.vegacocoa.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminProcessing

/**
 * Created by Baskaran Kannan on 6/18/2020.
 */

class VegaCocoaRminItemWithLots {
    @Embedded
    lateinit var rminItem: VegaCocoaRminProcessing
    @Relation(
        parentColumn = "rminId",
        entityColumn = "rminId",
        entity = VegaCocoaRminLots::class
    )
    var lots: List<VegaCocoaRminLots>? = emptyList()
}
