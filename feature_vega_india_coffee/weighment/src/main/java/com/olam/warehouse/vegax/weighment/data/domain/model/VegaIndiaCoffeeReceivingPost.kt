package com.olam.warehouse.vegax.weighment.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving


data class VegaIndiaCoffeeReceivingPost(val key: String, val plant: Plant, val weighDetails: List<VegaReceiving>)
