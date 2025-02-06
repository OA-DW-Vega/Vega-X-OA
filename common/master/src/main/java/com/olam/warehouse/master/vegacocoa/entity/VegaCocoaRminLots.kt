package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = arrayOf("rminId", "batchNumber", "cgfNo", "poNumber", "BOMNumber"))
@Parcelize
data class VegaCocoaRminLots(
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
    var xchpf: String? = "",
    var noOfBags: String? = "",
    var slPostion: Int? = 0,
    var isProgress: Boolean? = false,
    @Ignore
    var isLowerWeight: Boolean = true,
    var stage: String = "",
    var cgfNo: String = "",
    var poNumber: String = "",
    var BOMNumber: String = "",
    var baseMaterialCode: String = "",
    var remarks: String = "",
    var shift: String = "",
    var rminId: String = "",
    @Ignore
    var vendor: String? = "",
    @Ignore
    var vendorName:String? =""
) : Parcelable
