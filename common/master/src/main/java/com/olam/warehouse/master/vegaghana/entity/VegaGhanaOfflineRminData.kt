package com.olam.warehouse.master.vegaghana.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class VegaGhanaOfflineRminData(
//    @ColumnInfo(index = true)
    @PrimaryKey
    var rminTempId: String = "",
    var processingStage: String? = "",
    var outputMaterialCode: String? = "",
    var plant: String? = null,
    @Ignore
    var processingLotDtls: List<VegaGhanaOfflineRminProcessLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var shiftType: String? = "",
    var operatorName: String? = "",
    var versionId: String? = ""
) : Parcelable

@Parcelize
@Entity(primaryKeys = ["rminTempId"])
data class VegaGhanaOfflineRminProcessLotDetails(
//    @ColumnInfo(index = true)
    var rminTempId: String = "",
    var bagCount: String? = "",
    var batchNumber: String? = "",
    var confText: String? = "",
    var deliveryItem: String? = "",
    var materialCode: String? = "",
    var menge: String? = "",
    var movementType: String? = "",
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var bagType: String? = "",
    var year: String? = "",
    var processOrderNum: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var xchpf: String? = "",
    var huno: String? = "", // bag uom
    var huwt: String? = "", // bag tare weight
    var huno2: String? = "", //pallet uom
    var huwt2: String? = "", // pallet tare weight
    var nohu1: String? = "", // no of bags
    var nohu2: String? = "", // no of pallet
    var bagMaterialCode: String? = "",
    var endLotFlag: Boolean? = false,
    var vendorCode: String? = "",
    var storageLossFlag: Boolean? = false,
    @Ignore
    var bagList: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList(),
    var poNo: String? = "",
    var stage: String? = "",
    var materialName: String? = "",
    var status: Int? = 0,
    var syncStatusMsg: String? = "",
    var fgrnStatus: Boolean? = false
) : Parcelable


