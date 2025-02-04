package com.olam.warehouse.master.vegaghana.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaMtnrQualityLot

data class VegaGhanaLotQuality(
    var materialNumber: String,
    var charg: String,
    var qualityParameters: List<VegaGhanaQualityParameters>
)

data class VegaGhanaQualityParameters(
    var sapQCName: String,
    var satNam: String
)

class VegaGhanaWeighBridgeWithQualityParams {
    @Embedded
    lateinit var qualityWBDetails: VegaGhanaMtnrQualityLot

    @Relation(parentColumn = "weighBridgeId", entityColumn = "wbTempId", entity = VegaQuality::class)
    var quality: List<VegaQuality> = emptyList()
}
