package com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot


data class VegaIndiaCoffeePpqQualityParamPost(
    val grnApplicable: Boolean,
    val grnFlag: Boolean,
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaCoffeeLot>? = emptyList(),
    val bcMessage: String,
    val charg: String,
    val currentWbid: String,
    val errorMessage: String,
    val grnNumber: String
)
