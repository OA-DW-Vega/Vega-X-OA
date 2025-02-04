package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaQuality

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
class VegaQualityWithQualitative {
    @Embedded
    lateinit var quality: VegaQuality
    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = VegaQualitative::class)
    var qualitative: List<VegaQualitative>? = emptyList()
}
