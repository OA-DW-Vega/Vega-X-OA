package com.olam.warehouse.master.vegacocoa.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 6/9/2020.
 */

@Parcelize
@Entity(primaryKeys = ["id", "fgrnId", "materialCode"])
data class VegaCocoaFgrnGradesMatrialWeights(
    var id: Int = 0,
    var fgrnId: String = "",
    var fgrnIdMaterialCode: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var grossWeight: String = "0",
    var netWeight: String? = "",
    var batchNumber: String = "",
    var bagType: String = "",
    var bagCount: String = "0",
    var bagMaterialCode: String = "",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var isRoundOff: Boolean? = false,
    var palletAverage: String? = "",
    var createdTime: Int? = 0
) : Parcelable

data class VegaCocoaFgrnBagCosumption(
    var batchNumber: String = "",
    var bagType: String = "",
    var bagType1: String? = "",
    var bagCount: String = "0",
    var bagCount1: String = "0"

)

@Entity
@Parcelize
data class VegaCocoaSweepingBagMaterial(
    @PrimaryKey
    var id: Int = 0,
    var batchNumber: String = "",
    var weighBridgeId: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "",
    var bagType: String = "",
    var bagMaterialCode: String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "0",
    var truckOutWeight: String = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    @Ignore
    var tempWBId: String? = "",
    var palletAverage: String? = "",
    var status: Int? = 1,
    var message: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var isSyncStatus: Boolean? = false,
    var createdPosition: Int? = 0,
    var baseMaterial: String? = "",
    var bagCount1: String? = "0",
    var tareWeight1: String? = "0",
    var bagType1: String? = "",
    var bagMaterialCode1: String? = ""
) : Parcelable
