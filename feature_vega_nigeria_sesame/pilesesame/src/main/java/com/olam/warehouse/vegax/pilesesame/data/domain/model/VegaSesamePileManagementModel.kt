package com.olam.warehouse.vegax.pilesesame.data.domain.model

import android.os.Parcelable
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import kotlinx.android.parcel.Parcelize

data class VegaSesamePileManagementLotListModel(
    var selectedList: ArrayList<VegaCocoaDispatchLots> = ArrayList(),
    var isMultipleAdd: Boolean = false,
    var material: ArrayList<String> = ArrayList(),
    var isThirdParty: Boolean = false,
    var vendorCode: String = ""
)

data class VegaSesameTPDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    val grnPricePerUnit: String = "",
    val totalGrnPrice: String = "",
    var deliveryDetails: List<VegaSesameTPDeliveryDetail> = emptyList()
)

data class VegaSesameTPDeliveryDetail(
    var batchNumber: String? = "",
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
    var bagList: List<VegaCocoaSweepingBagMaterial> = emptyList()
)

data class VegaSesameWeighScalePallet(
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

data class VegaSesameWSBagModel(
    var weight: String = "",
    var isPalletMatched: Boolean = true
)

@Parcelize
data class VegaSesamePileSelectionModel(
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

data class VegaSesamePileSequence(
    var pileSequence: String = ""

)

data class VegaSesamePilePlantDetails(
    var plant: Plant

)


data class VegaSesamePilePostRequest(
    var key: String,
    var lots: List<VegaCocoaDispatchLots> = emptyList(),
    var pileDetails: VegaCocoaDispatchLots,
    var plant: Plant

)

data class VegaSesamePileSuccessResponse(
    var key: String,
    var lots: List<VegaCocoaDispatchLots> = emptyList(),
    var pileDetails: VegaCocoaDispatchLots,
    var plant: Plant,
    var pileNo: String
)

data class VegaSesamePileProcessType(
    val FGRN: String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = "",
    val PILE: String? = ""
)

data class VegaSesamePileProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaSesamePileProcessType>,
    val SHIFT_DETAILS_LIST: List<String>
)

@Parcelize
data class VegaDispatchLotQuality(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityGrade: String? = "",
    var certification: String? = ""
) : Parcelable
