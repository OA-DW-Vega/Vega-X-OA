package com.olam.warehouse.vegax.approve.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
@Parcelize
data class VegaApproveWeighBridgeId(
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
    var unitPrice: String? = ""
) : Parcelable
