package com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model

data class VegaCameroonProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaCameroonProcessType>
)

data class VegaCameroonProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)


