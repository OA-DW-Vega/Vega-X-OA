package com.olam.warehouse.vegax.dispatchnigeria.data.domain.model

data class VegaNigeriaCocoaMtntProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaNigeriaCocoaMtntProcessType>
)

data class VegaNigeriaCocoaMtntProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)
