package com.olam.warehouse.vegax.processingindo.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaProcessingIndoProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
