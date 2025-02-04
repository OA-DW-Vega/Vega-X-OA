package com.olam.warehouse.vegax.offloadingcoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper

data class VegaCoffeeOffloadingSupplierPostRequest(
    var bagList: List<VegaCoffeeOffloadingBagMaterial>,
    var key: String? = "",
    var weighmentType: String? = "",
    var plant: Plant,
    var weighBridgeId: String = "",
    var delivery: String = "",
    var grossWeight: String? = "0",
    var deliveryItem: String? = "",
    var purchaseDocNum: String? = "",
    var purchaseDocDesc: String? = "",
    var purchaseDocQty: String? = "",
    var materialCode: String? = "",
    var materialName: String? = "",
    var netWeight: String = "0",
    var supplierCode: String? = "",
    var supplierName: String? = "",
    var unitsOfMeasure: String = "",
    var plantId: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wsGate: String = "",
    var truckDirection: String? = "",
    var vehicleNumber: String? = "",
    var contactNumber: String? = "",
    var driverName: String? = "",
    var erdat: String? = "",
    var ertim: String? = "",
    var direction: String? = "",
    var storageLocationCode: String? = "",
    var storageLocationName: String? = "",
    var dstorageLocationCode: String? = "",
    var dstorageLocationName: String? = "",
    var declaredBagCount: String? = "",
    var declaredWeight: String? = "",
    var vendorDeclaredWeight: String? = "",
    var origin: String? = "",
    var department: String? = "",
    var plantName: String? = "",
    var item: String? = "",
    var challan: String? = "",
    var remarks: String? = ""

)
