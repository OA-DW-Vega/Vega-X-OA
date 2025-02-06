package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.olam.warehouse.presentation.enums.Status
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
@Entity
@Parcelize
data class VegaReceivingLineItem(
    @PrimaryKey(autoGenerate = true)
    var bagId: Int = 0,
    var tmpWbId: String = "",
    var bagCount: String? = "",
    var bagType: String? = "",
    var bagWeight: String? = "",
    var bagTareWeight: String? = "",
    var palletCount: String? = "",
    var palletType: String? = "",
    var palletWeight: String? = "",
    var charg: String = "",
    var grossWeight: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String? = "",
    var supplierCode: String? = "",
    var posnr: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = "",
    var wsGate: String = "",
    var weighBridgeType: String = "",
    var location: String? = "",
    var tareWeight: String? = "",
    var supplierName: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var syncStatusMsg: String? = "",
    var mtnCode: String? = ""
) : Parcelable
