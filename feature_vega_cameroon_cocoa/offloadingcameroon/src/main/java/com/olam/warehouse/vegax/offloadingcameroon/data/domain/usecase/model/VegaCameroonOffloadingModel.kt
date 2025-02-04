package com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaReceivingLineItem

data class VegaCameroonReceivingPostLineItem(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceivingLineItem>
)

data class VegaCameroonWeighScalePallet(
    var weighbridgeID: String? = "",
    var serialNumber: String? = "",
    var plant: String? = "",
    var material: String? = "",
    var batch: String? = "",
    var gossWeight: Boolean = false,
    var packingWeight: String? = "",
    var netWeight: String? = "",
    var packingMaterial1: String? = "",
    var noofPackingMat1: String? = "",
    var packingMatweight1: String? = "",
    var packingMaterial2: String? = "",
    var noofPackingMat2: String? = "",
    var packingMatweight2: String? = "",
    var unit: String? = "",
    var storageLocation: String? = "",
    var noOfPallet: String? = "",
    var totalPalletWeight: String? = ""
)

data class VegaCameroonOffloadingPost(
    val batchNumber: String? = "",
    val cascara: String? = "",
    val certificate: String? = "",
    val encodedImageContent: String? = "",
    val errorMessage: String? = "",
    val exchangeRate: String? = "",
    val grade: String? = "",
    val grnData: List<VegaCameroonGrnData>?,
    val grnFlag: Boolean? = true,
    val grnNumber: String? = "",
    val grnType: String? = "",
    val humedad: String? = "",
    val imageUploadMsg: String? = "",
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceiving>,
    val lotDetails: List<CameroonOffloadingQualityDetails>
)

data class CameroonOffloadingQualityDetails(
    val materialCode: String? = "",
    val batchNumber: String? = "",
    val qualityDetails: List<OffloadingQualityDetails>
)
data class OffloadingQualityDetails(
    val materialCode: String? = "",
    val batchNumber: String? = "",
    val descrChar: String? = "",
    val nameChar: String? = "",
    val qualityParameterValue: String? = ""
)

data class VegaCameroonGrnData(
    val batchNumber: String? = "",
    val createdDate: String? = "",
    val currency: String? = "",
    val docDate: String? = "",
    val item: String? = "",
    val materialCode: String? = "",
    val netWeight: String? = "",
    val plant: String? = "",
    val price: String? = "",
    val purchaseDocNum: String? = "",
    val purchaseOrderNum: String? = "",
    val storageLocationCode: String? = "",
    val supplierCode: String? = "",
    val unitsOfMeasure: String? = "",
    val weighBridgeId: String? = "",
    val weighBridgeType: String? = ""
)



