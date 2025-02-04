package com.olam.warehouse.master.vegacoffee.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots


class VegaCoffeeFgrnGradesWithBagItems {
    @Embedded
    lateinit var fgrnGrades: VegaCoffeeFgrnItemsGrades

    @Relation(
        parentColumn = "fgrnIdMaterialCode",
        entityColumn = "fgrnIdMaterialCode",
        entity = VegaCoffeeFgrnGradesMatrialWeights::class
    )
    var bagItems: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList()
}

class VegaCoffeeRMINGradesWithBagItems {
    @Embedded
    lateinit var rminGrades: VegaCoffeeFgrnItemsGrades

    @Relation(
        parentColumn = "fgrnIdMaterialCode",
        entityColumn = "fgrnIdMaterialCode",
        entity = VegaCoffeeRminLots::class
    )
    var lotItems: List<VegaCoffeeRMINLotsWithBagItems>? = emptyList()
}


class VegaCoffeeRMINLotsWithBagItems {
    @Embedded
    lateinit var rminLots: VegaCoffeeRminLots

    @Relation(
        parentColumn = "batchNumber",
        entityColumn = "batchNumber",
        entity = VegaCoffeeFgrnGradesMatrialWeights::class
    )
    var lotBagItems: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList()
}

