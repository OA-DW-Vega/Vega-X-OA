package com.olam.warehouse.vegax.stockrecon.data.domian.model

data class VegaStockReconPostAuditDataResp(
    var id: String? = "",
    var reconId: String? = "",
    var storageLocation: String? = "",
    var material: String? = "",
    var lotNumber: String? = "",
    var bagTypeId1: String? = "",
    var noOfBags1: String? = "",
    var bagWeight1: String? = "",
    var noOfFullBags: String? = "",
    var noOfHalfBags: String? = "",
    var systemNetWeight: String? = "",
    var stockAuditWeight: String? = "",
    var weightGainLoss: String? = "",
    var noOfDamagedBags: String? = "",
    var spillage: Boolean = false,
    var bagDamaged: Boolean = false,
    var unitOfMeasure: String = "",
    var remarks: String = "",
    var imageUrl: String = "",
)
