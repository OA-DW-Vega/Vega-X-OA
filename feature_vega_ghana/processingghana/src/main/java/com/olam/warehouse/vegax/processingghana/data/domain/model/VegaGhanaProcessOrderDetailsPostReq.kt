package com.olam.warehouse.vegax.processingghana.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaGhanaProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
