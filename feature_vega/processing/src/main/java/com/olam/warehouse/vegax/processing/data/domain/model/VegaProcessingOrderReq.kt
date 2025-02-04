package com.olam.warehouse.vegax.processing.data.domain.model

import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 2/25/2020.
 */
class VegaProcessingOrderReq(
    var key: String? = "",
    var plant: Plant = Plant()
)
