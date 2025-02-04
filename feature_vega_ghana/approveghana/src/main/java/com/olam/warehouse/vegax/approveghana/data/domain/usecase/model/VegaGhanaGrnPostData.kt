package com.olam.warehouse.vegax.approveghana.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnPostData

data class VegaGhanaGrnPostData(
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
    var qcParamValue: String? ="",
    var qualityDetails: List<GhanaGRNQualityDetails> = emptyList()
)

data class VegaGhanaGrnPost(
    val key: String,
    val plant: Plant,
    var grnData: List<VegaGhanaGrnPostData?> = emptyList()
)

data class GhanaGRNQualityDetails(
    var qualityParameterValue: String? = "",
    var nameChar: String? = ""
)
