package com.olam.warehouse.vegax.mtntsesame.data.domain.model

data class VegaNigeriaSesameMtntProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaNigeriaSesameMtntProcessType>
)

data class VegaNigeriaSesameMtntProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)
