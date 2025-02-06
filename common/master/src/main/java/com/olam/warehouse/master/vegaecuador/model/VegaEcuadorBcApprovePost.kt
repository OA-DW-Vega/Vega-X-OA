package com.olam.warehouse.master.vegaecuador.model

import com.olam.warehouse.master.user.model.Plant
import java.util.*

data class VegaEcuadorBcApprovePost (
    val key: String,
    val plant: Plant,
    var approvalDetailsList: List<VegaEcuadorBcApproveDetails> = emptyList()

)
data class VegaEcuadorBcApproveDetails(
    var autoTransfer: String? = "",
    var batchNumber: String? = "",
    var discount: String? = "",
    var finalApproval: String? = "",
    var grnQty: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
    var paidWeight: String? = "",
    var plant: String? ="",
    var priceCharacter: String? = "",
    var qchar: String? = "",
    var qualityDetails: ArrayList<VegaEcuadorBcApproveQualityDetails> = ArrayList(),
    var receivingStorageLoc: String? = "",
    var sendingStorageLoc: String? = "",
    var status: Boolean = false,
    var supplierCode: String? = "",
    var uom: String? = "",
    var waers: String? = "",
    var weighBridgeId: String? = "",
    var poNumber: String? = ""
    )


data class VegaEcuadorBcApproveQualityDetails(
    var descrChar: String? = "",
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
)
data class VegaEcuadorBcApproveResponse (
    var key:String?="",
    var plant:String?="",
    var weighDetails:List<String>?= emptyList(),
    var approvalDetails:String?= "",
    var approvalDetailsList: ArrayList<VegaEcuadorBcApproveDetails> = ArrayList()
    )

