package com.olam.warehouse.vegax.processingghana.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaGhanaProcessingRminBomPost(
    val cfgNo: String,
    val fevor: String,
    val materialCode: String,
    val key: String,
    val plant: Plant
)


