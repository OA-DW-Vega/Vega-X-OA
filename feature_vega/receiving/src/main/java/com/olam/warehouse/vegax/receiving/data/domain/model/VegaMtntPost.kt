package com.olam.warehouse.vegax.receiving.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMtnt

/**
 * Created by Baskaran Kannan on 2/18/2020.
 */
data class VegaMtntPost(val key: String, val plant: Plant, val weighDetails: List<VegaMtnt>)
