package com.olam.warehouse.vegax.offloading.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaOffloadingTrucks

/**
 * Created by Baskaran Kannan on 2/3/2020.
 */
data class VegaOffloadingQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaOffloadingTrucks?> = emptyList()
)

data class OffloadingQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)

data class BatchNumResponse(
    var batchNumber: String? = ""
)
