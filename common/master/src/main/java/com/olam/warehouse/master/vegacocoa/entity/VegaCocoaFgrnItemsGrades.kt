package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 6/9/2020.
 */

@Parcelize
@Entity(primaryKeys = ["fgrnId", "processOrderNo", "materialCode"])
data class VegaCocoaFgrnItemsGrades(
    var fgrnId: String = "",
    var processOrderNo: String = "",
    var fgrnIdMaterialCode: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var storageLocationCode: String? = "",
    var netWeight: String? = "",
    var grossWeight: String? = "",
    var tareWeight: String? = "",
    var meins: String? = "",
    var rsNum: String? = "",
    var rsPos: String? = "",
    var bwart: String? = "",//movementType
    var resource: String? = "",
    var phase: String? = "",
    var plant: String? = "",
    var deliveryItem: String? = "",
    var xchpf: String? = "",
    var isGradeChecked: Boolean? = false,
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "",
    var isDefaultLot: Boolean? = false,
    var batchNumber: String = "", // Lot Info start
    var bkBez: String? = "",
    var bkLas: String? = "",
    var cinsm: String? = "",
    var msg: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var sequence: String? = "",
    var isCreateNewLot: Boolean? = false,
    var lotStorageLocationCode: String? = "",
    var eligibeWeight: String? = "", // Lot info end
    var startTime: String? = "",
    var isIndexweighmenttype: Boolean? = false,
    var isRoundOff: Boolean? = false,
    var endTime: String? = "",
    @Ignore
    var bagWeightList: List<VegaCocoaFgrnGradesMatrialWeights>? = emptyList()

) : Parcelable
