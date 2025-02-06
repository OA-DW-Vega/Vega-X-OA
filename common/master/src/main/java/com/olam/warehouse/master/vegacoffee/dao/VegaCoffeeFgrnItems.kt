package com.olam.warehouse.master.vegacoffee.dao

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 6/9/2020.
 */

@Parcelize
@Entity(primaryKeys = ["fgrnId", "processOrderNo"])
data class VegaCoffeeFgrnItems(
    var fgrnId: String = "",
    var processOrderNo: String = "",  //po no
    var stageFevor: String? = "",
    var cfgNo: String? = "",
    var plant: String? = "",
    var netWeight: String? = "",//mNetWeightFGRNProcessingOrder
    var meins: String = "",
    var materialName: String? = "",
    var remarks: String? = "",
    var auart: String? = "",
    var materialCode: String? = "",
    var rminTotal: String? = "",
    var rfgrnTotal: String? = "",
    var startDate: String? = "",
    var weight: String? = "",
    var unitsOfMeasure: String? = "",
    var shiftSelection: String? = "",
    var message: String? = "",
    var isProgress: Boolean = false,
    var status: Int? = 1,
    var synStatus: Boolean? = false,
    @Ignore
    var gradeList: List<VegaCoffeeFgrnItemsGrades>? = emptyList(),
    @Ignore
    var rminList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var rfgrnList: List<VegaProcessingList>? = emptyList(),
    @Ignore
    var materialBatchList: List<VegaCoffeeFgrnMaerialBatch>? = emptyList(),
    var vendor: String? = "",
    var storageLocationCode: String? = "",
    var rminId: String? = "",
    var rminQty: String? = "",
    var palletWeight: String? = "",
    var palletCount: String? = ""
) : Parcelable


@Parcelize
@Entity(primaryKeys = ["fgrnId", "processOrderNo", "materialCode"])
data class VegaCoffeeFgrnItemsGrades(
    var fgrnId: String = "",
    var processOrderNo: String = "",
    var fgrnIdMaterialCode: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var storageLocationCode: String? = "",
    var netWeight: String? = "",
    var meins: String? = "",
    var status: Int? = 1,
    var synStatus: Boolean? = false,
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
    var isCreateNewLot: Boolean? = false,
    var lotStorageLocationCode: String? = "",
    var eligibeWeight: String? = "", // Lot info end
    var startTime: String? = "",
    var endTime: String? = "",
    var weightToProcess: String? = "",
    var isThirdPartyMaterial: Boolean = false,
    @Ignore
    var bagWeightList: List<VegaCoffeeFgrnGradesMatrialWeights>? = emptyList()

) : Parcelable

@Parcelize
@Entity(primaryKeys = ["id", "fgrnId", "materialCode"])
data class VegaCoffeeFgrnGradesMatrialWeights(
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
    var palletWeight: String? = "0",
    var noOfPallet: String? = "0",
    var palletAverage: String? = "",
    var createdTime: Int? = 0,
    var isBagConsumed: Boolean = false,
    var totalBagCount: String? = "0",
    var bagCount1: String? = "0",
    var tareWeight1: String? = "0",
    var bagType1: String? = "",
    var bagMaterialCode1: String? = ""
) : Parcelable

data class VegaCoffeeFgrnBagCosumption(
    var batchNumber: String = "",
    var bagType: String = "",
    var bagCount: String = "0"

)

@Parcelize
data class VegaCoffeeFgrnMaerialBatch(
    var bagId: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var selection: Int = -1,
    var isValueAdded: Boolean = false,
    var batchList: List<VegaCoffeeRminLots> = emptyList()
) : Parcelable

class VegaCoffeeFgrnGradesWithBagMatrialWeights{
    @Embedded
    lateinit var fgrnItem: VegaCoffeeFgrnItemsGrades

    @Relation(parentColumn = "fgrnIdMaterialCode", entityColumn = "fgrnIdMaterialCode", entity = VegaCoffeeFgrnGradesMatrialWeights::class)
    var lineItems: List<VegaCoffeeFgrnGradesMatrialWeights> = emptyList()

    @Relation(parentColumn = "fgrnIdMaterialCode", entityColumn = "fgrnIdMaterialCode", entity = VegaCoffeeRminLots::class)
     var lots: List<VegaCoffeeRminLots> = emptyList()
}
