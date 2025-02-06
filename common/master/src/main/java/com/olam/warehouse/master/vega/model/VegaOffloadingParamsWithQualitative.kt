package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaOffloadingParameter
import com.olam.warehouse.master.vega.entity.VegaQualitative

/**
 * Created by Baskaran Kannan on 2/1/2020.
 */
class VegaOffloadingParamsWithQualitative {
    @Embedded
    lateinit var offloadingParam: VegaOffloadingParameter
    @Relation(parentColumn = "nameChar", entityColumn = "nameChar", entity = VegaQualitative::class)
    var qualitative: List<VegaQualitative>? = emptyList()
}
