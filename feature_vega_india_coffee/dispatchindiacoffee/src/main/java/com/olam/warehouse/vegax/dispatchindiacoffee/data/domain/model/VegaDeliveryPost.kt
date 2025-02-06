package com.olam.warehouse.vegax.dispatchindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaDispatchTrucks

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
data class VegaDeliveryPost(
    val key: String,
    val plant: Plant,
    val batchNumber: String,
    var deliveryDetails: List<VegaDispatchTrucks> = emptyList()
)
