package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
@Entity(primaryKeys = ["batchNumber", "materialCode", "vendorWithTransferType"])
@Parcelize
data class VegaCocoaDispatchLots(
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
    var grossWeight: String? = "",
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
    var isBagCountMatched: Boolean = true,
    @Ignore
    var postingDate: String? = ""
) : Parcelable

@Entity(primaryKeys = ["batch", "materialNumber", "mtnNumber", "tempWBId"])
@Parcelize
data class VegaCoffeeReceiveLots(
    var batch: String = "",
    var materialName: String? = "",
    var materialNumber: String = "",
    var plantId: String? = "",
    var tempWBId: String = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
    var processOrderNo: String? = "",
    var deliveryItem: String? = "",
    var isLowerWeight: Boolean = true,
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var wbFlag: Boolean? = false,
    var qcFlag: Boolean? = false,
    var grnFlag: Boolean? = false,
    var delivery: String? = "",
    var mtnNumber: String = "",
    var posnr: String = "",
    var weightAdded: Double = 0.0,
    var uom: String = "",
    var purchaseOrder: String = "",
    var ebelp: String = "",
    var supplyingPlantId: String = "",
    var supplyingPlantName: String = "",
    var hasWeightAdded: Boolean = false,
    var bagCount: Int = 0,
    var editedUOM: String = ""
) : Parcelable


@Entity(primaryKeys = ["batch", "materialNumber", "mtnNumber"])
@Parcelize
data class VegaCoCoaReceiveLots(
    var batch: String = "",
    var materialName: String? = "",
    var materialNumber: String = "",
    var plantId: String? = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
    var processOrderNo: String? = "",
    var deliveryItem: String? = "",
    var isLowerWeight: Boolean = true,
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var delivery: String? = "",
    var mtnNumber: String = "",
    var posnr: String = "",
    var isEndLot: Boolean? = false,
    var weightAdded: Double = 0.0,
    var uom: String = "",
    var purchaseOrder: String = "",
    var ebelp: String = "",
    var supplyingPlantId: String = "",
    var supplyingPlantName: String = "",
    var hasWeightAdded: Boolean = false,
    var bagCount: Int = 0,
    var editedUOM: String = ""
) : Parcelable

@Entity(primaryKeys = ["batchNumber", "materialCode", "vendorWithTransferType", "weighBridgeId"])
@Parcelize
data class VegaGhanaCocoaDispatchLots(
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
