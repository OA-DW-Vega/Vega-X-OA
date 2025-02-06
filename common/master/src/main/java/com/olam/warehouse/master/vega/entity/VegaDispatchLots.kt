package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */

@Entity
@Parcelize
data class VegaDispatchLots(
    @PrimaryKey
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "",
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
    var receivingStorageLocation: String? = "",
    var mergedLotId: String? = "",
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,

    /*for stock recon, we have added 3 more coloumns*/
    var bagType: String? = "",
    var totalBagWeight: String? = "",
    var totalNoOfBags: String? = "",
    @Ignore
    var isLowerWeight: Boolean = true
) : Parcelable




