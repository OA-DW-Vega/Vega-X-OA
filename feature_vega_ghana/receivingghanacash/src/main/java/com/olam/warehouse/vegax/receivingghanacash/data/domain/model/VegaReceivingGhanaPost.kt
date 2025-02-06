package com.olam.warehouse.vegax.receivingghanacash.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */

data class VegaReceivingGhanaPost(val key: String, val plant: Plant, val weighDetails: List<VegaReceiving>)
