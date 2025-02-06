package com.olam.warehouse.master.vegacocoa.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItemsGrades

/**
 * Created by Baskaran Kannan on 6/15/2020.
 */
class VegaCocoaFgrnGradesWithBagItems {
    @Embedded
    lateinit var fgrnGrades: VegaCocoaFgrnItemsGrades

    @Relation(
        parentColumn = "fgrnIdMaterialCode",
        entityColumn = "fgrnIdMaterialCode",
        entity = VegaCocoaFgrnGradesMatrialWeights::class
    )
    var bagItems: List<VegaCocoaFgrnGradesMatrialWeights>? = emptyList()
}
