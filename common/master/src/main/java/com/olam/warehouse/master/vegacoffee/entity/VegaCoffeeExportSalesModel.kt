package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 9/4/2020.
 */

@Entity
@Parcelize
data class VegaCoffeeExportSalesOrder(
    @PrimaryKey
    var saleOrderId: String = "",
    var salesType: String = "",
    var salesOrderDesc: String = "",
    var customerId: String? = "",
    var customerName: String? = "",
    var soWeight: String? = "",
    var salesItem: String? = "",
    var createdDate: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var turnAroundTime: String = "",
    var remarks: String? = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    @Ignore
    var isProgress: Boolean = false,
    var plantName: String = "",
    @Ignore
    var containerList: ArrayList<VegaCoffeeExportSalesContainer> = ArrayList()
) : Parcelable


@Entity(primaryKeys = ["containerNumber", "saleOrderId"])
@Parcelize
data class VegaCoffeeExportSalesContainer(
    var containerNumber: String = "",
    var saleOrderId: String = "",
    var tmpId: String? = "",
    @Ignore
    var openQuantity: String? = "",
    @Ignore
    var unitOfMeasure: String? = "",
    @Ignore
    var materialCode: String? = "",
    @Ignore
    var materialName: String? = "",
    @Ignore
    var lotList: ArrayList<VegaCoffeeExportSalesLots> = ArrayList()
) : Parcelable


@Entity(primaryKeys = ["batchNumber", "saleOrderId", "containerNumber", "materialCode"])
@Parcelize
data class VegaCoffeeExportSalesLots(
    var saleOrderId: String = "",
    var containerNumber: String = "",
    var batchNumber: String = "",
    var materialCode: String = "",
    var tmpId: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var vendor: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "0.0",
    var isAdded: Boolean? = false,
    var isChecked: Boolean? = false,
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
    var isLowerWeight: Boolean? = true,
    var salesType: String? = "",
    var weightToDispatchUOM: String? = "",
    var deliveryFlag: Boolean? = false,
    var pickingFlag: Boolean? = false,
    var pgiFlag: Boolean? = false,
    var containerFlag: Boolean? = false,
    var delivery: String? = "",
    var weighBridgeId: String? = "",
    var mergedLotId: String? = "",
    var receivingStorageLocation: String? = "",
    @Ignore
    var synStatusMsg: String? = "",
    var isBagCountMatched: Boolean? = false
) : Parcelable
