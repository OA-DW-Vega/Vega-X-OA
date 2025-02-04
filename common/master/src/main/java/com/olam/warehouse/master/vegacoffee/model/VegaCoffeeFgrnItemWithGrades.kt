package com.olam.warehouse.master.vegacoffee.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing

/**
 * Created by Baskaran Kannan on 6/18/2020.
 */

class VegaCoffeeFgrnItemWithGrades {
    @Embedded
    lateinit var fgrnItems: VegaCoffeeFgrnItems

    @Relation(
        parentColumn = "fgrnId",
        entityColumn = "fgrnId",
        entity = VegaCoffeeFgrnItemsGrades::class
    )
    var gradeItems: List<VegaCoffeeFgrnGradesWithBagItems>? = emptyList()
}

class VegaCoffeeRminItemWithGrades {
    @Embedded
    lateinit var processing: VegaCoffeeRminProcessing

    @Relation(
        parentColumn = "rminId",
        entityColumn = "fgrnId",
        entity = VegaCoffeeFgrnItemsGrades::class
    )
    var gradeWithLotsAndBags: List<VegaCoffeeRMINGradesWithBagItems>? = emptyList()
}
