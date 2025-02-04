package com.olam.warehouse.vegax.exportsalesindo.utils

import android.os.Build
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesOrderModel
import kotlin.random.Random

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */


const val MOVE_ADD_LOT = "move_add_lot"
const val MOVE_SUMMARY = "move_summary"
const val BUNDLE_DATA = "bundle_data"
const val CONTAINER_DATA = "container_data"
const val MATERIAL_LIST = "material_list"
const val SELECTED_MATERIAL_LIST = "selected_material_list"
const val INVENTORY_FRAG = "inventory_frag"
const val SALES_ID = "sales_order_id"
const val SALES_ORDER = "sales_order"
const val TMP_ID = "tmp_id"
const val UNIT_MT = "MT"
const val UNIT_KG = "KG"


@Suppress("DEPRECATION")
fun getColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        App.getAppContext().resources.getColor(id, null)
    } else {
        App.getAppContext().resources.getColor(id)
    }
}

fun convertMtToKg(weight: String): String {
    val converted = weight.toDouble().times(1000)
    return converted.formatThreeDigits()
}

fun convertKgToMT(weight: String): String {
    return weight.toDouble().div(1000).formatThreeDigits()
}

fun getTmpId() = "TMP_".plus(Random.nextLong().toString())

fun preparePurchaseOrder(item: List<IndoExporSalesMaterialList>): ArrayList<VegaIndoCoffeeExportSalesOrderModel> {
    val salesOrder = arrayListOf<VegaIndoCoffeeExportSalesOrderModel>()
    item.forEach {
        val model = VegaIndoCoffeeExportSalesOrderModel()
        model.salesOrderId = it.salesOrderId
        model.salesOrderList = listOf(it)
        salesOrder.add(model)
    }
    return salesOrder
}

fun prepareLotsList(it1: MutableList<VegaEcuadorDispatchStocks>): MutableList<VegaCoffeeExportSalesLots> {
    val lotList = mutableListOf<VegaCoffeeExportSalesLots>()
    it1.forEach { item ->
        val lot = VegaCoffeeExportSalesLots()
        lot.batchNumber = item.batchNumber
        lot.materialCode = item.materialCode.toString()
        lot.materialName = item.materialName
        lot.plantId = item.plantId
        lot.plantName = item.plantName
        lot.storageLocationCode = item.storageLocationCode
        lot.unitOfMeasure = item.unitOfMeasure
        lot.weight = item.weight
        lot.vendor = item.vendor.toString()
        lotList.add(lot)
    }
    return lotList
}

fun prepareSalesOrder(item: List<IndoExporSalesMaterialList>): ArrayList<VegaIndoCoffeeExportSalesOrder> {
    val salesOrder = arrayListOf<VegaIndoCoffeeExportSalesOrder>()
    item.forEach {
        val model = VegaIndoCoffeeExportSalesOrder()
        model.saleOrderId = it.salesOrderId
        model.salesItem = it.salesItemNum
        model.materialCode = it.materialNumber
        model.materialName = it.materialDesc
        model.plantId = it.plantId
        model.openQuantity = it.openQuantity
        model.unitOfMeasure = it.meins
        salesOrder.add(model)
    }
    return salesOrder
}

fun prepareSalesOrderToMaterialList(it: VegaIndoCoffeeExportSalesOrder): ArrayList<IndoExporSalesMaterialList> {
    val materialList = arrayListOf<IndoExporSalesMaterialList>()
    val model = IndoExporSalesMaterialList()
    model.salesOrderId = it.saleOrderId.toString()
    model.salesItemNum = it.salesItem
    model.materialNumber = it.materialCode
    model.materialDesc = it.materialName
    model.plantId = it.plantId
    model.openQuantity = it.openQuantity
    model.meins = it.unitOfMeasure
    model.isView = it.isView
    materialList.add(model)
    return materialList
}
