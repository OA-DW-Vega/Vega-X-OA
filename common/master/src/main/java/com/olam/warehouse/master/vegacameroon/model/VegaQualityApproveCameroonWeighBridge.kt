package com.olam.warehouse.master.vegacameroon.model

import android.os.Parcelable
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

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
    var totalPrice: String? = "",
    var unitPrice: String? = "",
    var basePrice: String? = "",
    var qchar: String? = "",
    var meins: String? = "",
    var year: String? = "",
    var inventoryRes: String? = "",
    var finalApproval: String? = "",
    var qualityDetails: List<VegaQuality> = emptyList()
) : Parcelable
