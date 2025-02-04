package com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntry

/**
 * Created by Baskaran Kannan on 3/11/2020.
 */

data class VegaGateEntryApprovalNigeriaPost(
    val key: String,
    val plant: Plant,
    val gateEntry: Boolean,
    val status: String,
    val weighDetails: List<VegaGateEntry>
)
