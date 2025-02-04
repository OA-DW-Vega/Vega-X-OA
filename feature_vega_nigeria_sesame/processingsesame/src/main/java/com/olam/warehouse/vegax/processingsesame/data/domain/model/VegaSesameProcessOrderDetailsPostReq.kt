package com.olam.warehouse.vegax.processingsesame.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaSesameProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
