package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaReceiving

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaReceivingPost(val key: String, val plant: Plant, val weighDetails: List<VegaReceiving>)
