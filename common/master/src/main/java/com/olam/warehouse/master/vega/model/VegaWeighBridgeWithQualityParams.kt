package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
class VegaWeighBridgeWithQualityParams {
    @Embedded
    lateinit var qualityWBDetails: VegaQualityWBDetails

    @Relation(parentColumn = "wbTempId", entityColumn = "wbTempId", entity = VegaQuality::class)
    var quality: List<VegaQuality> = emptyList()
}


class VegaIndoWeighBridgeWithQualityParams {
    @Embedded
    lateinit var qualityWBDetails: VegaQualityWBDetails

    @Relation(parentColumn = "wbTempId", entityColumn = "tempId", entity = VegaCoffeeLot::class)
    var lotList: List<VegaLotsWithQualityParams> = emptyList()
}

class VegaLotsWithQualityParams {
    @Embedded
    lateinit var lot: VegaCoffeeLot

    @Relation(parentColumn = "tempId", entityColumn = "wbTempId", entity = VegaQuality::class)
    var quality: List<VegaQuality> = emptyList()
}
