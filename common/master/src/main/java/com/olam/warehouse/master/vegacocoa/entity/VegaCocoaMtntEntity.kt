package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = ["batchNumber", "materialCode"])
@Parcelize
data class VegaCocoaNoWeighmentLot(
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var status: Int? = 1,
    var isAdded: Boolean = false,
    var isSyncStatus: Boolean = false,
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
    var kor: String? = "",
    var region: String? = "",
    var processOrderNo: String? = "",
    var meins: String = "",
    var phase: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var isProgress: Boolean? = false,
    var isLowerWeight: Boolean = true,
    var weightToDispatchUOM: String? = "",
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var weighScaleWbId: String? = "",
    var isPgiFlag: Boolean = false,
    var delivery: String? = "",
    var vendor: String? = "",
    var vendorName: String? = "",
    var isEndLot: Boolean? = false,
    var storageLossFlag: Boolean? = false,
    var isBagCountMatched: Boolean = true,
    var isOfflineData: Boolean = false
) : Parcelable

@Entity(primaryKeys = ["wbTempId", "weighBridgeId"])
@Parcelize
data class VegaCocoaNoWeighmentModel(
    @ColumnInfo(index = true)
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var weighBridgeType: String = "",
    var batchPicking: String? = "",
    var wayBillNo: String = "",
    var item: String? = "",
    var erdat: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var materialCode: String? = "",
    var deliveryItem: String = "",
    var delivery: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var customerNum: String? = "",
    var materialName: String? = "",
    var isSyncStatus: Boolean = false,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var approximateWeight: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var vehicleNumber: String = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var driverPhoneNumber: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var turnAroundTime: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var transportVendor: String = "",
    var transportVendorID: String = "",
    var isStarted: Boolean = false,
    var isEnded: Boolean = false,
    @Ignore
    var isProgress: Boolean = false,
    var plantName: String = "",
    var deliveryStatus: Boolean = false,
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var storageLossFlag: Boolean = false,
    var isThirdPartyMaterial: Boolean = false,
    var imageString: String? = "",
    var warehouseId: String? = "",
    @Ignore
    var isEditData: Boolean? = false,
    var truckOutWeight: String? = ""
) : Parcelable


@Entity(primaryKeys = ["id", "weighBridgeId", "batchNumber"])
@Parcelize
data class VegaCocoaNoWeighmentBagMaterial(
    var id: Int = 0,
    var weighBridgeId: String = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "0",
    var bagType: String = "",
    var bagCount: String = "",
    var tareWeight: String? = "0",
    var bagMaterialCode: String = "",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "",
    var status: Int? = 1,
    var message: String? = "",
    var createdPosition: Int? = 0,
    var isPalletDetails: Boolean? = false,
    var isSyncStatus: Boolean? = false,
    var isOffLine: Boolean? = false
) : Parcelable


@Entity(primaryKeys = ["batchNumber", "materialCode", "vendorWithTransferType"])
@Parcelize
data class VegaCocoaOfflineStock(
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
    var kor: String? = "",
    var region: String? = "",
    var isAdded: Boolean = false,
    var isChecked: Boolean = false,
    var processOrderNo: String? = "",
    var meins: String = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    var isLowerWeight: Boolean = true,
    var weightToDispatchUOM: String? = "",
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var weighScaleWbId: String? = "",
    var isPgiFlag: Boolean = false,
    var delivery: String? = "",
    var vendor: String? = "",
    var vendorName: String? = "",
    var isEndLot: Boolean? = false,
    var vendorWithTransferType: String = "",
    var storageLossFlag: Boolean? = false,
    var isBagCountMatched: Boolean = true
) : Parcelable
