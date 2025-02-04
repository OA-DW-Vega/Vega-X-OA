package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 11/18/2020.
 */

@Entity
@Parcelize
data class VegaNicaraguaMtnt(
    @PrimaryKey
    var tempId: String = "",
    var weighBridgeId: String? = "",
    var batchPicking: String? = "",
    var weighBridgeType: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var sendingPlant: String? = "",
    var batchNumber: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var item: String? = "",
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var isSyncStatus: Boolean? = false,
    var isErrorStatus: Boolean? = true,
    var isOfflineData: Boolean? = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var erdat: String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var recStorageLocationCode: String? = "",
    var recPlantId: String? = "",
    var remarks: String? = "",
    var turnAroundTime: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var transportVendor: String? = "",
    var transportVendorID: String? = "",
    var deliveryStatus: Boolean? = false,
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var storageLossFlag: Boolean? = false,
    var qualityGrade: String? = "",
    var qulityGradeDesc: String? = "",
    var certification: String? = "",
    var vendorName: String? = "",
    var vendorCode: String? = "",
    var soWeight: String? = "",
    var soUOM: String? = "",
    @Ignore
    var bagType: String? = "",
    @Ignore
    var isProgress: Boolean? = false,
    @Ignore
    var isView: Boolean? = false,
    var mergedNetWeight : String? = ""
) : Parcelable


@Entity(primaryKeys = ["batchNumber", "tempId"])
@Parcelize
data class VegaNicDispatchLots(
    var batchNumber: String = "",
    var tempId: String = "",
    var tempIdWithBatch: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "0",
    var isChecked: Boolean? = false,
    var processOrderNo: String? = "",
    var meins: String? = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var weighScaleWbId: String? = "",
    var documentNum: String? = "",
    var xchpf: String? = "",
    var isLowerWeight: Boolean? = true,
    var weightToDispatchUOM: String? = "",
    var truckNo: String? = "",
    var isEndLot: Boolean? = false,
    var storageLossFlag: Boolean? = false,
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var isMergedLot:Boolean?=false,
    @Ignore
    var bagType: String? = "",
    @Ignore
    var materialQuality: MaterialQuality = MaterialQuality(),
    @Ignore
    var vendor: String? = "",
    var qualityGrade: String? = "",
    var certification: String? = ""

) : Parcelable

@Parcelize
data class MaterialQuality(
    var charg: String = "",
    var materialNumber: String = "",
//    var qualityParams: Quality = Quality(),
    var qualityParameters: List<QualityParams> = emptyList()
) : Parcelable

@Parcelize
data class QualityParams(
    var sapQCName: String = "",
    var satNam: String = ""
) : Parcelable


