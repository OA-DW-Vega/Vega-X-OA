package com.olam.warehouse.vegax.processingecuador.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaEcuadorProcessingOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
