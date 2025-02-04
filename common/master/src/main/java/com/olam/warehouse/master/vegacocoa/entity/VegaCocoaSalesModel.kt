package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

@Entity(primaryKeys = ["wbTempId", "weighBridgeId", "saleOrderId"])
@Parcelize
data class VegaCocoaSalesWB(
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
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var purchaseDocNum: String? = "",
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
    var grossWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var vehicleNumber: String = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var storageLocationCode: String? = "",
    var recStorageLocationCode: String? = "",
    var recPlantId: String? = "",
    var frbnr1: String? = "",
    var bltxt: String? = "",
    var docum: String? = "",
    var deliveryUOM: String? = "",            //Vrkme
    var stockQty: String? = "",               //Lgmng
    var deliveryQty: String? = "",            //Lfimg
    var denominator: String? = "",            //Umvkz
    var numerator: String? = "",              //Umvkn
    var stockUOM: String? = "",                //Meins
    @Ignore
    var isProgress: Boolean = false,
    var isRoundOff: Boolean? = false,
    var plantName: String = "",
    var salesType: String = "",
    var saleOrderId: String = "",
    var salesOrderDesc: String = "",
    var customerId: String? = "",
    var customerName: String? = "",
    var soWeight: String? = "",
    var salesItem: String? = "",
    var createdDate: String? = "",
    var operatorName: String? = "",
    var thirdPartyMaterialCode: String = "",
    @Ignore
    var lotList: ArrayList<VegaCocoaSalesLots> = ArrayList()
) : Parcelable


@Entity(primaryKeys = ["batchNumber", "salesOrderId"])
@Parcelize
data class VegaCocoaSalesLots(
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "0.0",
    var kor: String? = "",
    var region: String? = "",
    var isAdded: Boolean? = false,
    var isChecked: Boolean? = false,
    var processOrderNo: String? = "",
    var meins: String = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var deliveryItem: String? = "",
    var delivery: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    @Ignore
    var isLowerWeight: Boolean = true,
    var salesOrderId: String = "",
    var salesType: String? = "",
    var weightToDispatchUOM: String? = "",
    var vendor: String? = "",
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var inventoryFlag: Boolean? = false
) : Parcelable

@Entity
@Parcelize
data class VegaCocoaSalesBagMaterial(
    @PrimaryKey
    var id: Int = 0,
    var batchNumber: String = "",
    var grossWeight: String = "",
    var netWeight: String = "",
    var bagType: String = "",
    var bagCount: String = "",
    var tareWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "",
    var status: Int? = 1,
    var message: String? = "",
    var isSyncStatus: Boolean? = false
) : Parcelable
