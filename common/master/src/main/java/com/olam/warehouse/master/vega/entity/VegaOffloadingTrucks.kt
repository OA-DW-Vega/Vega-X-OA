package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(indices = [Index(value = ["wbTempId"], unique = true)])
@Parcelize
class VegaOffloadingTrucks(
    @PrimaryKey
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var weighBridgeType: String = "",
    var direction: String = "",
    var item: String? = "",
    var plant: String? = "",
    var batchNumber: String? = "",
    var materialCode: String? = "",
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var customerNum: String? = "",
    var materialName: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var grntNumber: String? = "",
    var weighMethod: String? = "",
    @Ignore
    var qualityDetails: List<VegaOffloadingParameter> = emptyList(),
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var qcStatus: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var vehicleNumber: String? = "",
    var finalApproval: String? = "",
    var qualityFlag: Boolean? = false,
    var appName: String? = "",
    var storageLocationCode: String? = "",
    var driverNumber: String? = "",
    var challan: String? = "",
    var appoximateWeight: String? = "",
    @Ignore
    var tareWeight: String? = ""

  
) : Parcelable
