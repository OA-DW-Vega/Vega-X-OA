package com.olam.warehouse.vegax.gateentrycameroon.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry

/**
 * Created by Baskaran Kannan on 3/11/2020.
 */

data class VegaGateEntryCameroonPost(val key: String, val plant: Plant, val weighDetails: List<VegaGateEntry>)
