package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaIndiaCoffeeProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
