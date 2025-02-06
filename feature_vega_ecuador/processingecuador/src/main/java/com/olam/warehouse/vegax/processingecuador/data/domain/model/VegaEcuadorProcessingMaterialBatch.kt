package com.olam.warehouse.vegax.processingecuador.data.domain.model

import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaRminLots


data class VegaEcuadorProcessingFgrnMaterialBatch(
    var bagId: String = "",
    var materialCode: String = "",
    var materialName: String = "",
    var selection: Int = -1,
    var isValueAdded: Boolean = false,
    var batchList: List<VegaCocoaRminLots> = emptyList()
)

data class VegaEcuadorProcessingFgrnBagMaterialWithId(
    var bagMaterialCode: String = "",
    var fgrnIdMaterial: String = "",
    var batchNumber: String = ""
)
