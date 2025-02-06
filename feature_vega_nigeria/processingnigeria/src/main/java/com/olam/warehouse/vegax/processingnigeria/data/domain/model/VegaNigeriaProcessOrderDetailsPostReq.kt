package com.olam.warehouse.vegax.processingnigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaNigeriaProcessOrderDetailsPostReq(
    var key: String? = "",
    var plant: Plant = Plant(),
    var processOrderNo: String? = "",
    var rmin: Boolean = false
)
