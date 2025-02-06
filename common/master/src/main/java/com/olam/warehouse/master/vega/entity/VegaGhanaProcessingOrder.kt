package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(primaryKeys = ["processOrderNo", "materialCode"])
data class VegaGhanaProcessingOrder(
        var processOrderNo: String = "",
        var plant: String? = "",
        var netWeight: String? = "",
        var unitsOfMeasure: String? = "",
        var materialName: String? = "",
        var auart: String? = "",
        var materialCode: String = "",
        var rminTotal: String? = "",
        var rfgrnTotal: String? = "",
        var startDate: String? = "",
        @Ignore
        var rmin: List<VegaGhanProcessingList>? = emptyList(),
        @Ignore
        var rminList: List<VegaGhanProcessingList>? = emptyList(),
        @Ignore
        var rfgrnList: List<VegaGhanProcessingList>? = emptyList(),
        var vendor: String? = "",
        var storageLocationCode: String? = "",
        var materialType: String? = ""
) : Parcelable


@Parcelize
data class VegaGhanProcessingList(
    var processOrderNo: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var batchNumber: String? = ""
) : Parcelable

@Parcelize
@Entity(primaryKeys = ["processOrderNo", "rsPos"])
data class VegaGhanaProcessingOrderDetails(
    var processOrderNo: String = "",
    var rsPos: String = "",
    var bwart: String? = "",
    var deliveryItem: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var meins: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var resource: String? = "",
    var rsNum: String? = "",
    var storageLocationCode: String? = "",
    var xchpf: String? = ""
) : Parcelable





