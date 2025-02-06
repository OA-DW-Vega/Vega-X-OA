package com.olam.warehouse.master.vega.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class VegaFgrnProcessingOrder(
    @PrimaryKey
    var processOrderNo: String = "",  //po no
    var plant: String? = "",
    var netWeight: String? = "",//mNetWeightFGRNProcessingOrder
    var unitsOfMeasure: String? = "",
    var meins: String = "",
    var materialName: String? = "",
    var auart: String? = "",
    var materialCode: String? = "",
    var rminTotal: String? = "",
    var rfgrnTotal: String? = "",
    var startDate: String? = "",
    @Ignore
    var rmin: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var rminList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var rfgrnList: List<VegaProcessingList>? = emptyList(),
    var isGradeChecked: Boolean? = true,
    var vendor: String? = "",
    @Ignore
    var vendorName:String?="",
    var versionId: String? = "",
    var storageLocationCode: String? = ""
) : Parcelable


@Parcelize
data class VegaProcessingList(
    var processOrderNo: String = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var storageLocationCode: String? = "",
    var batchNumber: String? = ""
) : Parcelable





