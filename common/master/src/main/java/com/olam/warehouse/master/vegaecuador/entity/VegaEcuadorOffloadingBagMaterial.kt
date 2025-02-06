package com.olam.warehouse.master.vegaecuador.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 5/28/2020.
 */

@Entity(primaryKeys = ["tmpWbId", "id"])
@Parcelize
data class VegaEcuadorOffloadingBagMaterial(
    var id: Int = 0,
    var tmpWbId: String = "",
    var materialCode: String = "",
    var supplierCode: String = "",
    var plant: String? = "",
    var procureType: String = "",
    var purcheseOrderNo: String = "",
    var purchaseDocDesc: String = "",
    var batchNumber: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "",
    var bagType: String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "0",
    var status: Int? = 1,
    var message: String? = "",
    var isSyncStatus: Boolean? = false,
    var truckNo: String? = "",
    var mtntNo: String? = "",
    var receivingLocation: String? = "",
    var wbId: String? = "",
    var supplierName: String? = "",
    var materialName: String? = "",
    var mtntWeight: String? = "",
    var isAutoBatchId: Boolean? = false,
    var receivingPlant: String? = "",
    var tempBagCount: String? = "",
    var otLotNumber: String? = "",
    var bagType1: String? = "",
    var bagCount1: String? = "0",
    var tareWeight1: String? = "0",
) : Parcelable

@Entity(primaryKeys = ["tmpWbId", "id"])
@Parcelize
data class VegaCameroonOffloadingBagMaterial(
    var id: Int = 0,
    var tmpWbId: String = "",
    var materialCode: String = "",
    var supplierCode: String = "",
    var plant: String? = "",
    var procureType: String = "",
    var purcheseOrderNo: String = "",
    var purchaseDocDesc: String = "",
    var batchNumber: String = "",
    var grossWeight: String = "0",
    var netWeight: String = "",
    var bagType: String = "",
    var bagCount: String = "0",
    var tareWeight: String? = "0",
    var unitsOfMeasure: String? = "",
    var palletWeight: String? = "",
    var noOfPallet: String? = "",
    var palletAverage: String? = "0",
    var status: Int? = 1,
    var message: String? = "",
    var isSyncStatus: Boolean? = false,
    var truckNo: String? = "",
    var mtntNo: String? = "",
    var receivingLocation: String? = "",
    var wbId: String? = "",
    var supplierName: String? = "",
    var materialName: String? = "",
    var mtntWeight: String? = "",
    var isAutoBatchId: Boolean? = false,
    var receivingPlant: String? = "",
    var tempBagCount: String? = "",
    var otLotNumber: String? = "",
    var bagType1: String? = "",
    var bagCount1: String? = "0",
    var tareWeight1: String? = "0",
) : Parcelable
