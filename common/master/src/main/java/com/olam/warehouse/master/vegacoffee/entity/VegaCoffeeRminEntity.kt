package com.olam.warehouse.master.vegacoffee.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItemsGrades
import kotlinx.parcelize.Parcelize

@Entity(primaryKeys = arrayOf("rminId", "materialCode", "cgfNo", "bom", "poNumber"))
@Parcelize
data class VegaCoffeeRminProcessing(
    var materialCode: String = "",
    var materialName: String = "",
    var stage: String = "",
    var cgfNo: String = "",
    var foreverNo: String = "",
    var bom: String = "",
    var poNumber: String = "",
    var weightToProcess: String = "",
    var remark: String? = "",
    var shift: String? = "",
    var auartNo: String? = "",
    var baseMaterialCode: String? = "",
    var baseMaterialName: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var xchpf: String? = "",
    var bwart: String? = "",
    var phase: String? = "",
    var resource: String? = "",
    var versionId: String? = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    @Ignore
    var lotList: ArrayList<VegaCoffeeRminLots>? = ArrayList(),
    var rminId: String = "",
    var gradeListDetails: String? = "",
    @Ignore
    var gradeList: List<VegaCoffeeFgrnItemsGrades>? = emptyList(),
    @Ignore
    var rminList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var rfgrnList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var poQuantity: String? = "",
    var vendor: String? = "",
    var atSourceVal: String? = ""
) : Parcelable

@Entity(primaryKeys = arrayOf("batchNumber", "materialCode"))
@Parcelize
data class VegaCoffeeRminLots(
    var batchNumber: String = "",
    var status: Int? = 1,
    var isSyncStatus: Boolean = false,
    var weighBridgeId: String = "",
    var materialCode: String = "",
    var materialName: String? = "",
    var plantId: String? = "",
    var plantName: String? = "",
    var storageLocationCode: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var editedWeight: String? = "0.0",
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
    var isLowerWeight: Boolean = true,
    var stage: String = "",
    var cgfNo: String = "",
    var poNumber: String = "",
    var BOMNumber: String = "",
    var baseMaterialCode: String = "",
    var remarks: String = "",
    var shift: String = "",
    var rminId: String = "",
    var weightToDispatchUOM: String = "",
    var vendor: String? = "",
    var fgrnIdMaterialCode: String = "",
    var isEndLot: Boolean? = false,
    var storageLossFlag: Boolean = false,
    var palletCount: String? = "",
    var palletWeight: String? = ""
) : Parcelable
