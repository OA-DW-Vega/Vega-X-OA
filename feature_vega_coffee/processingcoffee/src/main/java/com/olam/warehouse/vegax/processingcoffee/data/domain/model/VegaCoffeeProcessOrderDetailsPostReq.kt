package com.olam.warehouse.vegax.processingcoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaCoffeeProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
