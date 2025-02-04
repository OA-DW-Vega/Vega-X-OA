package com.olam.warehouse.vegax.processingcameroon.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaCameroonProcessingRminBomPost(
    val cfgNo: String,
    val fevor: String,
    val materialCode: String,
    val key: String,
    val plant: Plant
)


