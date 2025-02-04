package com.olam.warehouse.master.vega.entity

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class VegaPostProcessQualityWBDetails(
    @PrimaryKey
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var weighBridgeType: String = "",
    var direction: String = "",
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
    var vehicleNumber: String? = ""

)

