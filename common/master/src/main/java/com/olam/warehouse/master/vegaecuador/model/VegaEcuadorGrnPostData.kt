package com.olam.warehouse.master.vegaecuador.model

import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
data class VegaEcuadorGrnPostData(
    var batchNumber: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var storageLocationCode: String? = "",
    var price: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",
    var materialCode: String? = "",
    var item: String? = "",
    var currency: String? = "",
    var purchaseDocNum: String? = ""
)

data class VegaEcuadorGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaEcuadorGrnPostData?> = emptyList()
)
