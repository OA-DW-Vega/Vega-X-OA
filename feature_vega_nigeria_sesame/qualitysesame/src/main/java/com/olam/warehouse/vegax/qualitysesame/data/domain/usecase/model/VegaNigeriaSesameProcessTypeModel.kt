package com.olam.warehouse.vegax.qualitysesame.data.domain.usecase.model

data class VegaNigeriaSesameProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaNigeriaSesameProcessType>
)

data class VegaNigeriaSesameProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)


