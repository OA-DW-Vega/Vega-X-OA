package com.olam.warehouse.master.vegacoffee.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder

/**
 * Created by Baskaran Kannan on 8/20/2020.
 */
class VegaCoffeeSalesOrderWithLots {

    @Embedded
    lateinit var salesOrder: VegaCoffeeSalesOrder

    @Relation(parentColumn = "saleOrderId", entityColumn = "saleOrderId", entity = VegaCoffeeSalesLots::class)
    var lineItems: List<VegaCoffeeSalesLotWithbags> = emptyList()
}

class VegaCoffeeSalesLotWithbags {

    @Embedded
    lateinit var salesLot: VegaCoffeeSalesLots

    @Relation(parentColumn = "batchNumber", entityColumn = "batchNumber", entity = VegaCoffeeSalesBagMaterial::class)
    var lineItems: List<VegaCoffeeSalesBagMaterial> = emptyList()
}

class VegaCoffeePendingSalesOrderWithLots {

    @Embedded
    lateinit var salesOrder: VegaCoffeeSalesOrder

    @Relation(parentColumn = "salesTempId", entityColumn = "salesTempId", entity = VegaCoffeeSalesLots::class)
    var lineItems: List<VegaCoffeeSalesLots> = emptyList()
}
