package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Keerthi Santhanam on 2/17/2020.
 */
@Entity(primaryKeys = ["wbTempId", "weighBridgeId"])
@Parcelize
data class VegaCocoaDispatchWB(
    @ColumnInfo(index = true)
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var weighBridgeType: String = "",
    var batchPicking: String? = "",
    var wayBillNo: String = "",
    var item: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var direction: String? = "",
    var batchNumber: String? = "",
    var materialCode: String? = "",
    var deliveryItem: String = "",
    var delivery: String? = "",
    var purchaseDocNum: String? = "",
    var encodedImageContent: String? = "",
    var purchaseDocDesc: String? = "",
    var customerNum: String? = "",
    var materialName: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var approximateWeight: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var qcStatus: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagMaterialCode: String? = "",
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var vehicleNumber: String = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var driverPhoneNumber: String? = "",
    var driverLicenseNumber: String? = "",
    var sidingDepotwayBillNumber: String? = "",
    var evacuationCertificateNumber: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var recStorageLocationCode: String? = "",
    var recPlantId: String? = "",
    var recPlantName: String? = "",
    var frbnr1: String? = "",
    var bltxt: String? = "",
    var docum: String? = "",
    var deliveryUOM: String? = "",            //Vrkme
    var stockQty: String? = "",               //Lgmng
    var deliveryQty: String? = "",            //Lfimg
    var denominator: String? = "",            //Umvkz
    var numerator: String? = "",              //Umvkn
    var stockUOM: String? = "",                //Meins
    var remarks: String = "",
    var turnAroundTime: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var truckID: String? = "",
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
    var tareWeight: String? = "",
    var purchaseQuantity: String? = "",
    var routeLocCode: String? = "",
    var textId: String? = "",
    var textValue: String? = "",
    var shipmentNumber: String? = "",
    var fromVendorCode: String? = "",
    var departurePoint: String? = "",
    var route: String? = "",
    var destinationWH: String? = "",
    var destinationWHName: String? = "",
    @Ignore
    var weighMethod: String? = "",
    var operatorName: String? = "",
    @Ignore
    var imagePath: String? = "",
    @Ignore
    var imagesList: List<String> = emptyList()
) : Parcelable
