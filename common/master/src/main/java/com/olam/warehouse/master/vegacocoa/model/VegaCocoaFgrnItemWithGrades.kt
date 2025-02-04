package com.olam.warehouse.master.vegacocoa.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades

/**
 * Created by Baskaran Kannan on 6/18/2020.
 */

class VegaCocoaFgrnItemWithGrades {
    @Embedded
    lateinit var fgrnItems: VegaCocoaFgrnItems
    @Relation(parentColumn = "fgrnId",
        entityColumn = "fgrnId",
        entity = VegaCocoaFgrnItemsGrades::class)
    var gradeItems: List<VegaCocoaFgrnGradesWithBagItems>? = emptyList()
}
