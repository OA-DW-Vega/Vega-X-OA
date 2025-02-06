package com.olam.warehouse.vegax.nigeriaweighment.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaMtnt

data class VegaNigeriaMtntPost(
    val key: String,
    val plant: Plant,
    val weighDetails: List<VegaMtnt>,
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = "",
    var environment: String? = ""
    )
