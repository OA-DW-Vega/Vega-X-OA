package com.olam.warehouse.vegax.ghanaquality.data.domain.usecase.model

data class VegaGhanaProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaGhanaProcessType>
)

data class VegaGhanaProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)


