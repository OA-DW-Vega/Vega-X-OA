package com.olam.warehouse.vegax.offloadingcocoa.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.TrackTraceFarmerModel
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaOffloadingBagMaterial

data class VegaCoCoaOffloadingPostRequest(
    var contactNumber: String? = "",
    var driverName: String? = "",
    var grnFlag: Boolean,
    var grnNumber: String? = "",
    var imageString: String? = "",
    var imageUploadMsg: String? = "",
    var key: String? = "",
    var lotDetails: List<VegaCoCoaOffloadingDeliveryDetail>,
    var message: String? = "",
    var plant: Plant,
    var success: Boolean,
    var transportVendorCode: String? = "",
    var vehicleNumber: String? = "",
    var vehicleType: String? = "",
    var wayBillNo: String? = "",
    var weighBridgeId: String? = "",

)

data class VegaCoCoaOffloadingDeliveryDetail(
    var bagList: List<VegaCoCoaOffloadingBagMaterial> = emptyList(),
    var batchNumber: String? = "",
    var delivery: String? = "",
    var deliveryItem: String? = "",
    var documentNum: String? = "",
    var endLotFlag: Boolean? = false,
    var grnNumber: String? = "",
    var grossWeight: String? = "",
    var item: String? = "",
    var materialCode: String? = "",
    var netWeight: String? = "",
    var plant: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocNum: String? = "",
    var qualityFlag: Boolean? = false,
    var recStorageLocation: String? = "",
    var storageLocation: String? = "",
    var storageLocationCode: String? = "",
    var storageLossFlag: Boolean? = false,
    var supplierCode: String? = "",
    var tareWeight: String? = "",
    var unitsOfMeasure: String? = "",
    var weighBridgeId: String? = "",
    var weighBridgeType: String? = ""
)


