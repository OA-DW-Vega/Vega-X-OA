package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry
import com.olam.warehouse.master.vega.entity.VegaReceiving

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaReceivingResponse(
    var wbId: String? = "",
    val txnId: String? = "",
    val batchNumber: String? = "",
    val grnNumber: String? = "",
    var encodedImageContent: String? = ""
)

data class VegaGhanaCocoaOffloadingPost(
    val batchNumber: String? = "",
    val cascara: String? = "",
    val certificate: String? = "",
    val encodedImageContent: String? = "",
    val errorMessage: String? = "",
    val exchangeRate: String? = "",
    val grade: String? = "",
    val grnData: List<VegaGhanaCocoaGrnData>?,
    val grnFlag: Boolean? = true,
    val grnNumber: String? = "",
    val grnType: String? = "",
    val humedad: String? = "",
    val imageUploadMsg: String? = "",
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaReceiving>,
    val lotDetails: List<GhanaCocoaOffloadingQualityDetails>,
    val whReceiptNum: String? = ""
)

data class VegaNigeriaCocoaOffloadingPost(
    val batchNumber: String? = "",
    val whReceiptNum: String? = "",
    val cascara: String? = "",
    val certificate: String? = "",
    val encodedImageContent: String? = "",
    val errorMessage: String? = "",
    val exchangeRate: String? = "",
    val grade: String? = "",
    val grnData: List<VegaNigeriaCocoaGrnData>?,
    val grnFlag: Boolean? = true,
    val grnNumber: String? = "",
    val grnType: String? = "",
    val humedad: String? = "",
    val imageUploadMsg: String? = "",
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaGateEntry>,
    val lotDetails: List<GhanaCocoaOffloadingQualityDetails>
)

data class GhanaCocoaOffloadingQualityDetails(
    val materialCode: String? = "",
    val batchNumber: String? = "",
    val qualityDetails: List<OffloadingGhanaCocoaQualityDetails>
)

data class OffloadingGhanaCocoaQualityDetails(
    val materialCode: String? = "",
    val batchNumber: String? = "",
    val descrChar: String? = "",
    val nameChar: String? = "",
    val qualityParameterValue: String? = ""
)

data class VegaNigeriaCocoaGrnData(
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
    val weighBridgeType: String? = "",
    val whReceiptNum: String? = "",
    val bwart: String? = ""
)

data class VegaGhanaCocoaGrnData(
    val batchNumber: String? = "",
    val createdDate: String? = "",
    val currency: String? = "",
    val docDate: String? = "",
    val item: String? = "",
    val materialCode: String? = "",
    val netWeight: String? = "",
    val plant: String? = "",
    val price: String? = "",
    val bagCount: String? = "",
    val purchaseDocNum: String? = "",
    val purchaseOrderNum: String? = "",
    val storageLocationCode: String? = "",
    val supplierCode: String? = "",
    val unitsOfMeasure: String? = "",
    val weighBridgeId: String? = "",
    val weighBridgeType: String? = "",
    val whReceiptNum: String? = ""
)

