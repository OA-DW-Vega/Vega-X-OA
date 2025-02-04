package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
@Entity(primaryKeys = ["tmpWbId", "weighBridgeId"])
@Parcelize
data class VegaGateEntry(
    @ColumnInfo(index = true)
    var tmpWbId: String = "",
    var weighBridgeId: String = "",
    var batchNumber: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var bagMaterialCode: String? = "",
    var palletCount: String? = "",
    var palletType: String? = "",
    var palletWeight: String? = "0",
    var charg: String = "",
    var grntNumber: String? = "",
    var grossWeight: String? = "0",
    var item: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var customerNum: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var grnModel: String? = "",
    var procurementType: String? = "",
    var netWeight: String = "0",
    var supplierCode: String? = "",
    var posnr: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var plantName: String? = "",
    var wsGate: String = "",
    var wsType: String? = "",
    var receivingPlant: String? = "",
    var weighBridgeType: String = "",
    var location: String? = "",
    var tareWeight: String? = "0",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var txnId: String? = "",
    var doWeightThreshold: Int? = 0,
    var truckDirection: String? = "",
    var imagePath: String? = "",
    var imageString: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverNumber: String? = "",
    var driverName: String? = "",
    var truckId: String? = "",
    var qcStatus: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var transportVendorCode: String? = "",
    var transportVendorName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var bagTareWeight: String? = "0",
    var vendorDeclaredWeight: String? = "0",
    var supplierZone: String? = "",
    var approximateWeight: String? = "",
    var challan: String? = "",
    var commonPrimaryId: String? = "",
    var weighMethod: String? = "",
    var tempBagCount: String? = "",
    var truckDriverName: String? = "",
    var remarks: String? = "",
    @Embedded
    @Ignore
    var imagesList: List<String> = emptyList()
) : Parcelable

