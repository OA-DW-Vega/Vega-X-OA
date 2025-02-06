package com.olam.warehouse.vegax.qualitycoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaCoffeeQualityParamPost(
    val grnApplicable: Boolean,
    val grnFlag: Boolean,
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaCoffeeLot>? = emptyList(),
    val bcMessage: String,
    val charg: String,
    val currentWbid: String,
    val errorMessage: String,
    val grnNumber: String,
    val remarks: String?="",
    var isGain: Int = 0
)
