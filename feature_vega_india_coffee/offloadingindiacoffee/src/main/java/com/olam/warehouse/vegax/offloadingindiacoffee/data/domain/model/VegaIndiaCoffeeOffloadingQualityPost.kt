package com.olam.warehouse.vegax.offloadingindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks


data class VegaIndiaCoffeeOffloadingQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaOffloadingTrucks?> = emptyList()
)

data class IndiaCoffeeOffloadingQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)

data class IndiaCoffeeBatchNumResponse(
    var batchNumber: String? = ""
)
