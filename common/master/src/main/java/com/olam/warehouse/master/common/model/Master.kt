package com.olam.warehouse.master.common.model

import com.google.gson.annotations.SerializedName
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.user.model.ThirdPartyMaterialItem
import com.olam.warehouse.master.vega.entity.VegaBcZoneMapping
import com.olam.warehouse.master.vega.entity.VegaCocoaMiscellaneous
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterialStlocDetails
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */

data class Master(
    var key: String = "",
    @SerializedName("masterDTO")
    var masterDTO: MasterWrapper = MasterWrapper()
)


data class MasterWrapper(
    var id: Int = 0,
    var packingMaterials: List<PackageMaterial>? = emptyList(),
    var sapMaterials: List<Material>? = emptyList(),
    var vendors: List<Vendor>? = emptyList(),
    var warehouses: List<Warehouse>? = emptyList(),
    var storageLocations: List<StorageLocation>? = emptyList(),
    var materials: List<QualityParameter>? = emptyList(),
    var customStLocation: List<CustomStLocation>? = emptyList(),
    var processingStage: List<ProcessingStage>? = emptyList(),
    var bcZoneMapping: List<VegaBcZoneMapping>? = emptyList(),
    var configInfo: List<ConfigInfo>? = emptyList(),
    var plantList: List<Plant>? = emptyList(),
    var mtntPlantList: List<Plant>? = emptyList(),
    var miscellaneousList: List<VegaCocoaMiscellaneous>? = emptyList(),
    var shippingLineList: List<VegaCameroonShippingLine>? = emptyList(),
    var containerSizeList: List<VegaCameroonContainerSize>? = emptyList(),
    var multiPlantList: List<Plant>? = emptyList(),
    var plantRouteDetails: List<PlanRoute>? = emptyList(),
    var materialStlocDetails: List<VegaMaterialStlocDetails>? = emptyList(),
//    var wsitemDetails: List<DOQualityWBDetails>? = emptyList(),
    //var purchaseOrders: List<ProcurementPurchaseOrder>? = emptyList()
    var thirdPartyMaterials: List<ThirdPartyMaterialItem>? = emptyList(),
    var priceConfigDetails: List<PriceConfigDetails>? = emptyList(),
    var positionGradeMappings: List<PositionGradeMappings>? = emptyList(),
    var lotSequence: String? = "",
    var invoiceSequence: String? = "",
    var grnSequence: String? = "",
    var poSequence: String? = "",
    var tallySheetSequence: String? = "",
    var tallySheetCropYear: String? = "",
    var timestamp: String? = "",
    var materialQualitGrades: List<MaterialQualitGrades>? = emptyList(),
    var qualityGradeDesc: List<QualitativeParams>? = emptyList()
)

data class InventorySyncResponse(
    var data: String? = "",
    var errorCode: String? = "",
    var message: String? = "",
    var success: String? = "",
    var timestamp: String? = "",
    var errors: String? = ""
)

data class ConfigInfo(
    var configDetails: List<VegaConfigDetails>? = emptyList(),
    var role: String? = ""
)

data class MiscellaneousInfo(
    var configDetails: List<VegaCocoaMiscellaneous>? = emptyList(),
    var role: String? = ""
)


data class VendorSyncResponse(
    var data: String? = "",
    var errorCode: String? = "",
    var message: String? = "",
    var success: String? = "",
    var timestamp: String? = "",
    var errors: String? = ""
)

