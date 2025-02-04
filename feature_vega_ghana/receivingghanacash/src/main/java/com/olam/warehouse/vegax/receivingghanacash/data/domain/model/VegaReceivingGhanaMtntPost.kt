package com.olam.warehouse.vegax.receivingghanacash.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMtnt

/**
 * Created by Baskaran Kannan on 2/18/2020.
 */
data class VegaReceivingGhanaMtntPost(val key: String, val plant: Plant, val weighDetails: List<VegaMtnt>)
