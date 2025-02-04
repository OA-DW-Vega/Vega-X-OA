package com.olam.warehouse.vegax.gateentryghanacocoa.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry


data class VegaGateEntryGhanaCocoaPost(val key: String, val plant: Plant, val weighDetails: List<VegaGateEntry>)
