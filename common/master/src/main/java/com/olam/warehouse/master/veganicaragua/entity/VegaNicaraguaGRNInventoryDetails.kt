package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["materialCode", "lotId", "qcName"])
class VegaNicaraguaGRNInventoryDetails(
    var id: String? = "",

    @Ignore
    var warehouseLocation: String? = null,

    @Ignore
    var warehouse: String? = null,
    var warehouseId: String? = "",
    var warehouseName: String? = "",
    @Ignore
    var plant: String? = null,
    var plantId: String? = "",
    var plantName: String? = "",

    var procureLocationCode: String? = "",
    var procureLocationName: String? = "",

    var materialCode: String = "",
    var materialName: String? = "",
    var lotId: String = "",
    var stockQty: String? = "",
    var grnDate: String? = "",
    var createdDateTime: String? = "",
    var createdBy: String? = "",
    var updatedAt: String? = "",
    var updatedBy: String? = "",
    @Ignore
    var inventoryQC: String? = null,
    var qcId: String? = "",
    var qcName: String = "",
    var dataType: String? = "",
    var numberDecimals: String? = "",
    var entryObligatory: String? = "",
    var value: String? = "",
    var qualityGrade: String? = "",
    var certification: String? = "",
    var vendorCode: String? = "",
    @Ignore
    var vendorName: String? = "",
    var uom: String? = "",
    var netWeight: String? = "0",
    var grossWeight: String? = "0",
    var tareWeight: String? = "0",
    var netPrice: String? = "0",
    var humidity: String? = "0",
    var bagCount: String? = "0",
    var availablebagCount: String? = "0",
    var exportable: String? = "0",
    @Ignore
    var isChecked: Boolean = false,
    @Ignore
    var editedWeight: String? = "",
    @Ignore
    var isEndLot: Boolean? = false,
    @Ignore
    var isProgress: Boolean? = false,
    @Ignore
    var possition: Int? = 0
) : Parcelable
