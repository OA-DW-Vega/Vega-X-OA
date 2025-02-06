package com.olam.warehouse.vegax.processingcoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaCoffeeProcessingRminBomPost(
    val cfgNo: String,
    val fevor: String,
    val materialCode: String,
    val key: String,
    val plant: Plant
)


