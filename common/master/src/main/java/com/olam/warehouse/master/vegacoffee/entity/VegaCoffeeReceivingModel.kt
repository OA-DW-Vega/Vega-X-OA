package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 9/10/2020.
 */


@Entity(primaryKeys = ["weighBridgeId", "delivery"])
@Parcelize
data class VegaCoffeeReceiving(
    var weighBridgeId: String = "",
    var delivery: String = "",
    var mtnNumber: String? = "",
    var grnNumber: String? = "",
    var batchNumber: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var palletCount: String? = "",
    var palletType: String? = "",
    var palletWeight: String? = "0",
    var charg: String = "",
    var grossWeight: String? = "0",
    var item: String? = "",
    var deliveryItem: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocQty: String? = "",
    var customerNum: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String = "0",
    var supplierCode: String? = "",
    var posnr: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wsGate: String = "",
    var weighBridgeType: String = "",
    var location: String? = "",
    var tareWeight: String? = "0",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var txnId: String? = "",
    var truckDirection: String? = "",
    var imagePath: String? = "",
    var imageString: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var qcStatus: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var transportVendorCode: String? = "",
    var transportVendorName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var bagTareWeight: String? = "0",
    var dstorageLocationCode: String? = "",
    var dstorageLocationName: String? = "",
    var batchWeight: String? = "",
    var encodedImageContent: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var remarks: String? = "",
    var turnAroundTime: String? = "",
    var declaredBagCount: String? = "",
    var declaredWeight: String? = "",
    var vendorDeclaredWeight: String? = "",
    var origin: String? = "",
    var department: String? = "",
    var challan: String? = "",
    @Ignore
    var isProgress: Boolean = false,
    var commonPrimaryId: String? = "",
    var weighMethod: String? = "",
    var truckDriverName: String? = "",
    var isOnlineData: Boolean? = true,
    var tempWBId: String? = "",
    var isNotWBID: Boolean? = false,
    @Ignore
    var qualitgradedesc: String ="",
    var syncId: String? = "",
    @Ignore
    var mtntVendorName: String? = "",
    var tollingVendorCode :String?="",
    var tollingVendorName :String?=""
) : Parcelable

@Entity(primaryKeys = ["id", "batchNumber", "mtnNumber"])
@Parcelize
data class VegaCoffeeOffloadingBagMaterial(
    var id: Int = 0,
    var batchNumber: String = "",
    var mtnNumber: String = "",
    var tempWBId: String? = "",
    var grossWeight: String = "0",
    var netWeight: String = "",
    var bagType: String = "",
    var bagMaterialCode: String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "",
    var status: Int? = 1,
    var message: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var isSyncStatus: Boolean? = false,
    var createdPosition: Int? = 0,
    var baseMaterial: String? = "",
    var totalBagCount: String? = "",
    var truckOutWeight: String? = "",
    var bagType1: String? = "",
    var bagCount1: String? = "0",
    var tareWeight1: String? = "0",
    var bagMaterialCode1: String? = ""
) : Parcelable
