package com.olam.warehouse.master.vega.model

import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaGhanaProcessingOrderDetails

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
data class VegaGhanaProcessOrder(
    val vegaProcessOrderList: List<VegaGhanaProcessingOrder>,
    val vegaProcessOrderDetailsList: List<VegaGhanaProcessingOrderDetails>
)


