package com.olam.warehouse.master.vegaghana.model


data class VegaGhanaRminBom(
    var boms: List<VegaGhanaRminBOMDetails> = emptyList()
)

data class VegaGhanaRminBOMDetails(
    var cfgno: String? = "",
    var materialCode: String? = "",
    var baseMaterialCode: String? = "",
    var inputMaterialCode: String? = "",
    var materialName: String? = "",
    var versionId: String? = ""
)
