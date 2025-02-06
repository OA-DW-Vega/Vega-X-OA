package com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.PODetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import kotlinx.parcelize.Parcelize

data class VegaCoffeeThirdPartyLotListModel(
    var selectedList: ArrayList<VegaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false,
    var vendorCode: String = ""
)

data class VegaCoffeeWeighbridgePostRequest(
    var key: String,
    var lots: List<VegaCocoaDispatchLots> = emptyList(),
    var pileDetails: VegaWeighbridgeSelectionModel,
    var plant: Plant
)


@Parcelize
data class VegaWeighbridgeSelectionModel(
    var plantId: String? = "",
    var plantName: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var storageLocationCode: String? = "",
    var bkLas: String? = "",
    var bkBez: String? = "",
    var batchNumber: String? = "",
    var unitOfMeasure: String? = "",
    var weight: String? = "",
    var cinsm: String? = "",
    var materialText: String? = "",
    var vendor: String? = "",
    var year: String? = "",
    var thirdPartyFlag: String? = "",
    var createdDate: String? = "",
    var materialQuality: String? = "",
    var vendorName: String? = "",
    var isChecked: Boolean? = false

) : Parcelable

data class VegaPileSequence(
    var pileSequence: String = ""

)

data class VegaCoffeePilePlantDetails(
    var plant: Plant

)


data class VegaCoffeeTPDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    val grnPricePerUnit: String = "",
    val totalGrnPrice: String = "",
    var deliveryDetails: List<VegaCoffeeTPDeliveryDetail> = emptyList()
)

data class VegaCoffeeTPDeliveryDetail(
    var batchNumber: String? = "",
    var eudrStatus: String? = "",
    var bltxt: String? = "",
    var deliveryStatus: Boolean = false,
    var frbnr1: String? = "",
    var materialCode: String? = "",
    var msg: String? = "",
    var netWeight: String? = "",
    var plantId: String? = "",
    var recPlantId: String? = "",
    var recStorageLocationCode: String? = "",
    var remarks: String? = "",
    var salesItem: String? = "",
    var salesOrderNum: String? = "",
    var startTime: String? = "",
    var storageLocationCode: String? = "",
    var unitsOfMeasure: String? = "",
    var updatedDate: String? = "",
    var wayBillNo: String? = "",
    var weighBridgeId: String? = "",
    var huno: String? = "", // bag uom
    var huwt: String? = "", // bag tare weight
    var huno2: String? = "", //pallet uom
    var huwt2: String? = "", // pallet tare weight
    var nohu1: String? = "", // no of bags
    var nohu2: String? = "", // no of pallet
    var grossWeight: String? = "",
    var year: String? = "",
    var toVendorCode: String = "",
    var fromVendorCode: String = "",
    var grnPrice: String = "",
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList(),
    var storageLossFlag: Boolean? = false,
    var processOrderNo: String? = "",
    var delivery: String? = "",
    var newBatchNumber: String? = ""
)

data class VegaCoffeeWeighScalePallet(
    var weighbridgeID: String? = "",
    var serialNumber: String? = "",
    var plant: String? = "",
    var material: String? = "",
    var batch: String? = "",
    var gossWeight: Boolean = false,
    var packingWeight: String? = "",
    var netWeight: String? = "",
    var packingMaterial1: String? = "",
    var noofPackingMat1: String? = "",
    var packingMatweight1: String? = "",
    var packingMaterial2: String? = "",
    var noofPackingMat2: String? = "",
    var packingMatweight2: String? = "",
    var unit: String? = "",
    var storageLocation: String? = "",
    var noOfPallet: String? = "",
    var totalPalletWeight: String? = ""
)

data class VegaCoffeeWSBagModel(
    var weight: String = "",
    var isPalletMatched: Boolean = true
)

data class VegaNicaraguaCoffeeTPDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    val grnPricePerUnit: String = "",
    val totalGrnPrice: String = "",
    var deliveryDetails: List<VegaCoffeeTPDeliveryDetail> = emptyList(),
    var exchangeRate: String? = "",
    var priceDetails: List<VegaNicaraguaGrnPriceDetails>? = null,
    var transactionMode: String? = "",
    var certificate: String? = "",
    var cascara: String? = "",
    var humedad: String? = "",
    var rendimientoBruto: String? = "",
    var procureType: String? = "",
    var poNumber: String? = "",
    var poDetails: PODetails? = null
)

data class UpdateTPLotSequencePost(
    var plant: Plant,
    var prefix1: String? = "",
    var year: String? = "",
    var sequence: String? = "",         // Batch No Sequnce
    var isLotSequence: String? = "",    // Batch Sequnce flag
    var isInSequence: String? = "",     // Invoice Sequnce flag
    var invoiceSequence: String? = "",  // Invoice No Sequnce
    var grnSequence: String? = "", // Grn No Sequnce
    var isGrnRefSequence: String? = "",
    var poSequence: String? = "", // Po Sequnce flag
    var isPoRefSequence: String? = "",
    var isTallySheetSequence: String? = "",
    var isMTNRDocSequence: String? = "",
    var mtnrDocSequence: String? = "",
    var prefix3: String? = "",
    var key: String? = getCurrentKey()
)


