package com.olam.warehouse.master.vegacoffee.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesOrder
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 9/4/2020.
 */

class VegaCoffeeExportOTWithContainer {
    @Embedded
    lateinit var salesOrder: VegaCoffeeExportSalesOrder

    @Relation(
        parentColumn = "saleOrderId",
        entityColumn = "saleOrderId",
        entity = VegaCoffeeExportSalesContainer::class
    )
    var lineItems: List<ContainerWithLots> = emptyList()
}
@Parcelize
class ContainerWithLots : Parcelable {
    @Embedded
    lateinit var container: VegaCoffeeExportSalesContainer

    @Relation(
        parentColumn = "containerNumber",
        entityColumn = "containerNumber",
        entity = VegaCoffeeExportSalesLots::class
    )
    var lots: List<VegaCoffeeExportSalesLots> = emptyList()
}

