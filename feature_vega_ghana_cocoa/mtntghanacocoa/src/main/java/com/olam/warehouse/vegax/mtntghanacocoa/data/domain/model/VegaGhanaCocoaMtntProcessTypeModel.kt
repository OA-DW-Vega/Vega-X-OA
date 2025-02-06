package com.olam.warehouse.vegax.mtntghanacocoa.data.domain.model

data class VegaGhanaMtntProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaGhanaMtntProcessType>
)

data class VegaGhanaMtntProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)
