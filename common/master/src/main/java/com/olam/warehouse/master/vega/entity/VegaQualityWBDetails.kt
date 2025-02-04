package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Parcelize
@Entity(indices = [Index(value = ["wbTempId"], unique = true)])
data class VegaQualityWBDetails(
    @PrimaryKey
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var mtnNumber: String? = "",
    var weighBridgeType: String = "",
    var direction: String = "",
    var item: String? = "",
    var plant: String? = "",
    var gateEntry: String? = "",
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
    var mergedBatchNumber: String? = "",
    var receivedWeight: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var grntNumber: String? = "",
    var autoTransfer: String? = "",
    var recStorageLocation: String? = "",
    @Ignore
    var qualityDetails: List<VegaQuality> = emptyList(),
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
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var finalApproval: String? = "Q",
    var appName: String? = "",
    var LOBM_UDCODE: String? = "",
    var vehicleNumber: String? = "",
    var grnNumber: String = "",
    var unitPrice: String? = "",
    var storageLocationCode: String = "",
    var vendorDeclaredWeight: String? = "",
    var origin: String? = "",
    var department: String? = "",
    @Ignore
    var isProgress: Boolean = false,
    @Ignore
    var isCopy: Boolean = false,
    var paidWeight: String = "",
    var truckDriverName: String? = "",
    var driverName: String? = "",
    var contactNumber: String? = "",
    var transportVendorCode: String? = "",
    var storageLocation: String? = "",
    var kor: String? = "",
    var qualityFlag: Boolean? = false,
    var shipmentNumber: String? = "",
    var weighMethod: String? = "",
    var basePrice: String? = ""
) : Parcelable
