package com.olam.warehouse.vegax.processingsesame.data.domain.model


data class VegaSesameShiftProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaSesameProcessType>,
    val SHIFT_DETAILS_LIST: List<String>
)

data class VegaSesameProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)
