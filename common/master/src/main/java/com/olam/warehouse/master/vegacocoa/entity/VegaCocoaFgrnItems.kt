package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 6/9/2020.
 */

@Parcelize
@Entity(primaryKeys = ["fgrnId", "processOrderNo"])
data class VegaCocoaFgrnItems(
    var fgrnId: String = "",
    var processOrderNo: String = "",  //po no
    var stageFevor: String? = "",
    var cfgNo: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",//mNetWeightFGRNProcessingOrder
    var meins: String = "",
    var materialName: String? = "",
    var operatorName: String? = "",
    var auart: String? = "",
    @Ignore
    var processName: String? = "",
    var materialCode: String? = "",
    var rminTotal: String? = "",
    var rfgrnTotal: String? = "",
    var startDate: String? = "",
    var weight: String? = "",
    var unitsOfMeasure: String? = "",
    var weighmentType: String? = "",
    var shiftSelection: String? = "",
    var message: String? = "",
    var isProgress: Boolean = false,
    var isRoundOff: Boolean? = false,
    var isIndexweighmenttype: Boolean? = false,
    var status: Int? = 1,
    var versionId: String? = "",
    var synStatus: Boolean? = false,
    @Ignore
    var gradeList: List<VegaCocoaFgrnItemsGrades>? = emptyList(),
    @Ignore
    var rminList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var rfgrnList: List<VegaProcessingList>? = emptyList()
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
