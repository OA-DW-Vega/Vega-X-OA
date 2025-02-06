package com.olam.warehouse.vegax.inventoryghana.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry


data class VegaGhanaInventoryPost(val key: String, val plant: Plant, val fromDate: String,
                                  val toDate: String)
