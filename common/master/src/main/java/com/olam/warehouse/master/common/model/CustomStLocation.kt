package com.olam.warehouse.master.common.model

/**
 * Created by Baskaran Kannan on 1/30/2020.
 */
data class CustomStLocation(
    var plant: String? = "",
    var procureLocationCode: String? = "",
    var procureLocationName: String? = "",
    var storageLocationType: String? = "",
    var binDetails: List<BinDetails>? = emptyList()
)

data class BinDetails(
    var binLocationCode: String? = "",
    var procureLocationCode: String? = "",
    var binLocationName: String? = ""
)
