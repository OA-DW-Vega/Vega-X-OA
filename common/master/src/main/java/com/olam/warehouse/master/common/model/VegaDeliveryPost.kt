package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails

/**
 * Created by Baskaran Kannan on 5/13/2020.
 */

data class VegaDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaDispatchTrucks> = emptyList()
)

data class VegaGateEntryApprovalPostResponse(
    var vegaGateEntryDetails: List<VegaGateEntryDetails> = emptyList()
)

data class VegaDeliveryPostResponse(
    var delivery: String? = "",
    var msg: String? = "",
    var deliveryItem: String? = "",
    var encodedImageContent: String? = "",
    var deliveryStatus: Boolean = false,
    var deliveryFlag: Boolean = false,
    var pickingFlag: Boolean = false,
    var batchNumber: String = "",
    var storageLossFlag: Boolean = false
)
