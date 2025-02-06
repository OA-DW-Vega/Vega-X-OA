package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
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
    var encodedImageContent: String? = "",
    val grnFlag: Boolean? = false,
    val wbFlag: Boolean? = false,
    val qcFlag: Boolean? = false,
    val bagFlag: Boolean? = false,
    val grnData: List<VegaGhanaCocoaGrnData>? = emptyList()
)

data class VegaGhanaCocoaOffloadingPost(
    val batchNumber: String? = "",
    val cascara: String? = "",
    val certificate: String? = "",
    val encodedImageContent: String? = "",
    val errorMessage: String? = "",
    val exchangeRate: String? = "",
    val grade: String? = "",
    val grnData: List<VegaGhanaCocoaGrnData>? = emptyList(),
    var grnFlag: Boolean? = false,
    var wbFlag: Boolean? = false,
    var qcFlag: Boolean? = false,
    var bagFlag: Boolean? = false,
    var grnNumber: String? = "",
    val grnType: String? = "",
    val humedad: String? = "",
    val imageUploadMsg: String? = "",
    val key: String? = "",
    val plant: Plant? = null,
    val weighDetails: List<VegaReceiving>? = emptyList(),
    val lotDetails: List<GhanaCocoaOffloadingQualityDetails>? = emptyList(),
    val whReceiptNum: String? = "",
    val purchaseOrg: String? = "",
    var sourceLotId:String? = "",
    var farmerLessTransactionId:String? = "",
    var eudrComplaint:Boolean? = false,
    var ttFarmerList: List<TrackTraceFarmerModel> = emptyList()
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
    var nameChar: String? = "",
    var qualityParameterValue: String? = ""
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
    var batchNumber: String? = "",
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
    var weighBridgeId: String? = "",
    val weighBridgeType: String? = "",
    val whReceiptNum: String? = ""
)

