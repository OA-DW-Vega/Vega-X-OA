package com.olam.warehouse.master.vegaindocoffee.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 4/27/2021.
 */


class VegaIndoCoffeeExportOTWithContainer {
    @Embedded
    lateinit var salesOrder: VegaIndoCoffeeExportSalesOrder

    @Relation(
        parentColumn = "tmpId",
        entityColumn = "tmpId",
        entity = VegaCoffeeExportSalesContainer::class
    )
    var lineItems: List<IndoContainerWithLots> = emptyList()
}

@Parcelize
class IndoContainerWithLots : Parcelable {
    @Embedded
    lateinit var container: VegaCoffeeExportSalesContainer

    @Relation(
        parentColumn = "tmpId",
        entityColumn = "tmpId",
        entity = VegaCoffeeExportSalesLots::class
    )
    var lots: List<VegaCoffeeExportSalesLots> = emptyList()
}
