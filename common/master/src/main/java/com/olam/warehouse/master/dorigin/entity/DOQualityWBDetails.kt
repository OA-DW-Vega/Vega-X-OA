package com.olam.warehouse.master.dorigin.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.olam.warehouse.master.common.utils.getCurrentKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Parcelize
@Entity(indices = [Index(value = ["wbTempId"], unique = true)])
data class DOQualityWBDetails(
    @PrimaryKey
    var wbTempId: String = "",
    var weighBridgeId: String = "",
    var item: String? = "",
    var plant: String? = "",
    var purchaseDocNum: String? = "",
    var batchNumber: String? = "",
    var purchaseDocDesc: String? = "",
    var salesDocNum: String? = "",
    var materialCode: String? = "",
    var deliveryItem: String? = "",
    var materialName: String? = "",
    var supplierCode: String? = "",
    @Ignore
    var qualityDetails: List<DOQuality> = emptyList(),
    @Ignore
    var missedQrCodes: MutableSet<String> = mutableSetOf(),
    var isSyncStatus: Boolean = false,
    var isErrorStatus: Boolean = true,
    var isOfflineData: Boolean = false,
    var isNotWBID: Boolean = false,
    var status: Int? = 1,
    var message: String? = "",
    var netWeight: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var challan: String? = "", // DO txn id
    var qcStatus: String? = "",
    var bagWeight: String? = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var storageLocationCode: String? = "",
    var grossWeight: String? = "",
    var tareWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var weighMethod: String? = "",
    var currentKey: String? = getCurrentKey()
) : Parcelable
