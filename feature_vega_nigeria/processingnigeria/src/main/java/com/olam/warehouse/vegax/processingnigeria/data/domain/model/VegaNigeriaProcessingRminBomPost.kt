package com.olam.warehouse.vegax.processingnigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaNigeriaProcessingRminBomPost(
    val cfgNo: String,
    val fevor: String,
    val materialCode: String,
    val key: String,
    val plant: Plant
)


