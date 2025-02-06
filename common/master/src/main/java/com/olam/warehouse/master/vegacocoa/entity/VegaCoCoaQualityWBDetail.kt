package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Parcelize
@Entity(primaryKeys = ["weighBridgeId"])
data class VegaCoCoaQualityWBDetail(
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var weighBridgeType: String? = "",
    var direction: String? = "",
    var item: String? = "",
    var plant: String? = "",
    var purchaseDocNum: String? = "",
    var batchNumber: String? = "",
    var purchaseDocDesc: String? = "",
    var salesDocNum: String? = "",
    var materialCode: String? = "",
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var materialName: String? = "",
    var customerNum: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var challan: String? = "", // DO txn id
    var qcStatus: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var pmat2Type : String?="",
    var pmat2Count : String? = "",
    var pmat2Weight : String? = "",
    var pmat3Type : String? = "",
    var pmat3Count : String? = "",
    var pmat3Weight : String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var finalApproval: String? = "Q",
    var vehicleNumber: String? = "",
    var grnNumber: String? = "",
    var driverName: String? = "",
    var storageLocationCode: String? = "",
    @Ignore
    var isProgress: Boolean = false,
    @Ignore
    var isCopy: Boolean = false,
    var paidWeight: String? = "",
    var truckDriverName: String? = "",
    var contactNumber: String? = "",
    var transportVendorCode: String? = ""
) : Parcelable

