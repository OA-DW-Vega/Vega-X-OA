package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots


data class VegaIndiaCoffeeFgrnMaerialBatch(
    var bagId: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var selection: Int = -1,
    var isValueAdded: Boolean = false,
    var batchList: List<VegaCocoaRminLots> = emptyList()
)

data class VegaIndiaCoffeeFgrnBagMaterialWithId(
    var bagMaterialCode: String = "",
    var fgrnIdMaterial: String = "",
    var batchNumber: String = ""
)

data class VegaQualityBatchAndTicket(
    var batchNumber:String ="",
    var ticket:String ="",
    var isSelected: Boolean = false
)
