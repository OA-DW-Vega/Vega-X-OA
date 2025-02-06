package com.olam.warehouse.vegax.bagissueindiacoffee.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import kotlinx.parcelize.Parcelize

data class VegaIndiaCoffeeCurrentBagsIssued(
    var materialCode: String = "",
    var materialName: String? = "",
    var materialQuality: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var vendor: String? = "",
    var vendorName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var batchNumber: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var year: String? = "",
    var bkLas: String? = "",
    var bkBez: String? = "",
    var cinsm: String? = "",
    var unresConStock: String? = ""
    )
@Parcelize
data class VegaIndiaCoffeeBagIssue(
    var materialCode: String = "",
    var materialName: String? = "",
    var unitsOfMeasure: String? = "",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var currentBalance: String? = "",
    var bagIssued: String? = "",
    var gatePassNum: String? = ""
):Parcelable

@Parcelize
data class VegaIndiaCoffeeBagWeighDetails(
    var materialCode: String = "",
    var materialName: String? = "",
    var unitsOfMeasure: String? = ""
):Parcelable

data class VegaCameroonContainer(
    var id: Int = 0,
    var containerNum: String = "",
    var containerWeight: String? = "",
    var containerSize: String? = "",
    var shippingLine: String? = "",
    var status: String? = "",
    var uom: String? = "",
    var entryDate: String? = "",
    val plantDto: Plant
)

data class VegaIndiaCoffeeBagIssuePost(
    val lotDetails: List<VegaIndiaCoffeeBagIssue>,
    val weighDetails: List<VegaIndiaCoffeeBagWeighDetails>,
    val batchNumber: String?,
    val materialCode: String?,
    val materialName: String?,
    val numberOfBags: String?,
    val vendorCode: String?,
    val vendorName: String?,
    val storageLoc: String?,
    val uom: String?,
    val purchaseDocType: String?,
    val purchaseGrp: String?,
    val purchaseOrg: String?,
    val systemDate: String?,
    val wbId: String?,
    val errorMessage: String?,
    val plant: Plant,
    val key: String
    )

data class VegaIndiaCoffeeBagIssueResponse(
    var errorMessage: String? = "",
    var status: String? = "",
    val message: String? = ""
)



