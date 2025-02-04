package com.olam.warehouse.vegax.processingcameroon.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaCameroonProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
