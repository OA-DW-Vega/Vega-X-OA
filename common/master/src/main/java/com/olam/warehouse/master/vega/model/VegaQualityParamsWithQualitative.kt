package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityParameter

/**
 * Created by Baskaran Kannan on 1/13/2020.
 */
class VegaQualityParamsWithQualitative {
    @Embedded
    lateinit var qualityParameter: VegaQualityParameter
    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = VegaQualitative::class)
    var qualitative: List<VegaQualitative>? = emptyList()
}
