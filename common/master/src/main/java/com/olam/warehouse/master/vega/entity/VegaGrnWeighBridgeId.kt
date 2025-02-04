package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Keerthi Santhanam on 6/29/2020.
 */
@Entity
@Parcelize
data class VegaGrnWeighBridgeId(
    @PrimaryKey
    var wbTempId: String = "",
    var weighBridgeId: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var batchNumber: String? = "",
    var discount: String? = "",
    var discountWeight: String? = "",
    var direction: String? = "",
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
    var deliveryItem: String? = "",
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
    var warehouseRecieptNum: String? = "",
    var billOfLading: String? = "",
    var pmat2Count: String? = "",
    var pmat2Type: String? = "",
    var unitPrice: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseType: String? = "",
    var grnNumber: String? = "",
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isTransStatus: Boolean? = false,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var grnModel: String? = "",
    var procurementType: String? = "",
    var grntNumber: String? = "",
// New Attributes added on 25th August 2021
//var bagMaterialCode: String? = "",
    var currency: String? = "",
    var finalApproval: String? = "",
    var pmat2Weight: String? = "",
    var pmat3Count: String? = "",
    var pmat3Type: String? = "",
    var pmat3Weight: String? = "",
    var postingDate: String? = "",
    var price: String? = "",
    var purchaseOrderNum: String? = "",
    var qcParamValue: String? = "",
//var key: String? = "",
    var userName: String? = "",
    var currentWbId: String? = "",
    @Ignore
    var isView: Boolean? = false,
    var status: Int? = 1,
    var message: String? = "",
    var challan: String? = "",
    var weighMethod: String? = ""
) : Parcelable
