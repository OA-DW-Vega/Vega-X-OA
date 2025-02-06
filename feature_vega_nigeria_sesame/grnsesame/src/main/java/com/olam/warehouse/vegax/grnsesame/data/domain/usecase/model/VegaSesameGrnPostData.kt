package com.olam.warehouse.vegax.grnsesame.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId

data class VegaSesameGrnPostData(
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
    var purchaseDocNum: String? = "",
    var qcParamValue: String? = "",
    var warehouseRecieptNum: String? = "",
    var postingDate: String? = ""
)

data class VegaSesameGrnPost(
    val key: String,
    val plant: Plant,
    val lotQualityDetails: List<VegaGRNSesameQuality>,
    val wbDetailsList: VegaGrnWeighBridgeId,
    var grnData: List<VegaSesameGrnPostData?> = emptyList()
)
