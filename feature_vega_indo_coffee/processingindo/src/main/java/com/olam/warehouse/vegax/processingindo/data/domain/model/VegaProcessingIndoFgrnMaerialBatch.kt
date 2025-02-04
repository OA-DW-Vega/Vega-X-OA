package com.olam.warehouse.vegax.processingindo.data.domain.model

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots

/**
 * Created by Baskaran Kannan on 7/15/2020.
 */
data class VegaProcessingIndoFgrnMaerialBatch(
    var bagId: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var selection: Int = -1,
    var isValueAdded: Boolean = false,
    var batchList: List<VegaCocoaRminLots> = emptyList()
)

data class VegaProcessingIndoFgrnBagMaterialWithId(
    var bagMaterialCode: String = "",
    var fgrnIdMaterial: String = "",
    var batchNumber: String = ""
)
