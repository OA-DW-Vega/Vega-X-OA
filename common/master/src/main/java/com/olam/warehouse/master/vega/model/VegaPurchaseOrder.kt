package com.olam.warehouse.master.vega.model

import com.olam.warehouse.master.vega.entity.VegaPurchaseOrders

/**
 * Created by Baskaran Kannan on 2/18/2020.
 */
data class VegaPurchaseOrder(
    var materialCode: String? = "",
    var purchaseOrders: List<VegaPurchaseOrders> = emptyList()
)
