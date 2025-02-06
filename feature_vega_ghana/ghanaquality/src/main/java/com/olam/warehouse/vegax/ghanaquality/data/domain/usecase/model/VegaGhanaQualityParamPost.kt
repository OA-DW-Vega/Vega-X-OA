package com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot

/**
 * Created by Baskaran Kannan on 5/12/2020.
 */

data class VegaGhanaQualityParamPost(
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
