package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
/*@Parcelize
data class VegaQualityApproveCameroonWeighBridgeId(
    var weighBridgeId: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var batchNumber: String? = "",
    var discount: String? = "",
    var discountWeight: String? = "",
    var grn: String? = "",
    var grnQty: String? = "",
    var item: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var weighBridgeType: String = "",
    var qcStatus: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var netWeight: String = "0",
    var grossWeight: String? = "0",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var plantDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var transportVendorCode: String? = "",
    var storageLocationCode: String? = "",
    var bcApprover: String? = "",
    var unitsOfMeasure: String? = "",
    var totalPrice: String? = "",
    var unitPrice: String? = "",
    var qualityDetails: List<VegaQuality> = emptyList(),
    var finalApproval: String? = "Q",
    var year: String? = ""
    ) : Parcelable*/

//VegaQualityApproveCameroonWeighBridgeResponse
@Parcelize
data class VegaQualityApproveCameroonWeighBridgeId(
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
    var totalPrice: String? = "",
    var unitPrice: String? = "",
    var basePrice: String? = unitPrice,
    var qchar: String? = "",
    var meins: String? = "",
    var year: String? = "",
    var inventoryRes: String? = "",
    var finalApproval: String? = "",
    var qualityDetails: List<VegaQuality> = emptyList()
) : Parcelable

data class VegaCameroonWeighmentModel(
    var data :List<VegaCameroonWeighmentDetails>)

data class VegaCameroonWeighmentDetails(
    var appoximateWeight: String? = "",
    var delivery: String? = "",
    var deliveryNumber: String? = "",
    var erdat: String? = "",
    var inspectionLotNum: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var bagCount: String? = "",
    var bagTareWeight: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "",
    var batchNumber: String? = "",
    var challan: String? = "",
    var vehicleNumber: String? = "",
    var weighBridgeId: String = ""
)
