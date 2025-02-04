package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQualityParams

class VegaLotQualityParamsWithQualitative {
    @Embedded
    lateinit var qualityParameter: VegaQualityParams

    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = VegaQualitative::class)
    var qualitative: List<VegaQualitative>? = emptyList()
}
