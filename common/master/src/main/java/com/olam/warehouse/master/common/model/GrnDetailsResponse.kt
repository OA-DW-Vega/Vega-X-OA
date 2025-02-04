package com.olam.warehouse.master.common.model

import com.olam.warehouse.master.veganicaragua.entity.GrnDetails

data class GrnDetailsResponse(
    val vendor: String? = "",
    val grnDetails: List<GrnDetails>
)
