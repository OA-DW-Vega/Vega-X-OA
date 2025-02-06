package com.olam.warehouse.vegax.weighment.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMtnt

data class VegaIndiaCoffeeMtntPost(val key: String, val plant: Plant, val weighDetails: List<VegaMtnt>)
