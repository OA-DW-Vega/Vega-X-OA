package com.olam.warehouse.vegax.processing.data.domain.model

import com.olam.warehouse.master.user.model.Plant

class VegaProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = ""
)
