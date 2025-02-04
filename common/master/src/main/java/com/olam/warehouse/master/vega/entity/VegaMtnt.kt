package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
@Parcelize
data class VegaMtnt(
    @ColumnInfo(index = true)
    @PrimaryKey
    var tmpWbId: String = "",
    var weighBridgeId: String = "",
    var batchNumber: String? = "",
    var batchPicking: String = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "0",
    var palletCount: String? = "",
    var palletType: String? = "",
    var palletWeight: String? = "0",
    var grossWeight: String? = "0",
    var item: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
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
    var approximateWeight: String? = "0",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = "",
    var doWeightThreshold: Int? = 0,
    var truckDirection: String? = "",
    var imagePath: String? = "",
    var imageString: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var transportVendorCode: String? = "",
    var transportVendorName: String? = "",
    var storageLocationCode: String? = "",
    var recStorageLocationCode: String? = "",
    var recPlantId: String? = "",
    var bagTareWeight: String? = "0",
    @Ignore
    var isProgress: Boolean = false,
    @Ignore
    var supplier: String? = ""
) : Parcelable

