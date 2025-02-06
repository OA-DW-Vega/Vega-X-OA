package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vega.entity.VegaQuality
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
@Parcelize
data class VegaCoCoaLot(
    @PrimaryKey
    var item: String? = "",
    var delivery: String? = "",
    var customerNum: String = "",
    var purchaseDocNum: String = "",
    var purchaseDocDesc: String = "",
    var batchNumber: String = "",
    var receivedWeight: String = "",
    var sentWeight: String = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var supplierName: String? = "",
    var supplierCode: String? = "",
    var deliveryItem: String? = "",
    var bagType: String? = "",
    var bagCount: String? = "",
    var bagWeight: String? = "",
    var pmat2Count: String? = "",
    var pmat2Type: String? = "",
    var pmat2Weight: String? = "",
    var pmat3Count: String? = "",
    var pmat3Type: String? = "",
    var pmat3Weight: String? = "",
    var unitsOfMeasure: String? = "",
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var weighBridgeId: String? = "",
    var challan: String? = "",
    var plant: String? = "",
    var direction: String? = "",
    var weighBridgeType: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var qcStatus: String? = "",
    var bcApprover: String? = "",
    var vehicleNumber: String? = "",
    var storageLocationCode: String? = "",
    var storageLocation: String? = "",
    var transportVendorCode: String? = "",
    var tareWeight: String? = "",
    var batchPicking: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverNumber: String? = "",
    var truckType: String? = "",
    var truckDirection: String? = "",
    var bagTareWeight: String? = "",
    var driverName: String? = "",
    var batchWeight: String? = "",
    var weighMethod: String? = "",
    var grnNumber: String? = "",
    var dstorageLocationName: String? = "",
    var dstorageLocationCode: String? = "",
    var finalApproval: String? = "",
    var qualityFlag: Boolean? = true,
    var truckDriverName: String? = "",
    @Ignore
    var qualityDetails: List<VegaQuality?> = emptyList()
) : Parcelable
