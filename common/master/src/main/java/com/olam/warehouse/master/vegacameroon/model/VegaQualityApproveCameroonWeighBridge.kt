package com.olam.warehouse.master.vegacameroon.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaQualityApproveCameroonWeighBridge(
    var charg: String? = "",
    var batchNumber: String? = "",
    var discount: String? = "",
    var discountWeight: String? = "",
    var grn: String? = "",
    var grnQty: String? = "",
    var grnNumber: String? = "",
    var grnType: String? = "",
    var item: String? = "",
    var materialName: String? = "",
    var materialNumber: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wbid: String? = "",
    var werks: String? = "",
    var werksName: String? = "0",
    var pchar: String? = "",
    var kpein: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var plantDesc: String? = "",
    var waers: String? = "",
    var bprme: String? = "",
    var matkl: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var grossWeight: String? = "0",
    var materialCode: String? = "",
    var netWeight: String = "0",
    var storageLocationCode: String? = "",
    var vehicleNumber: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String = "",
    var deliveryItem: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var grntNumber: String? = "",
    var unitsOfMeasure: String? = "",
    var purchaseDocNum: String? = "",
    var isOfflineData: Boolean = false,
    var wbTempId: String = "",
    var isNotWBID: Boolean = false,
    var autoTransfer: String? = "",
    var recStorageLocation: String? = "",
    var recPlant: String? = "",

    var totalPrice: String? = "",
    var unitPrice: String? = "",
    var basePrice: String? = "",
    var qchar: String? = "",
    var meins: String? = "",
    var year: String? = "",
    var inventoryRes: String? = "",
    var finalApproval: String? = "",
    var qualityDetails: List<VegaQuality> = emptyList(),
    var sourceLotId:String?=""
) : Parcelable
