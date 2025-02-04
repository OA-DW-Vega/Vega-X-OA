package com.olam.warehouse.vegax.processingghana.data.domain.model


data class VegaGhanaShiftProcessTypeModel(
    val PROCESS_TYPE_LIST: List<VegaGhanaProcessType>,
    val SHIFT_DETAILS_LIST: List<String>
)

data class VegaGhanaProcessType (
    val FGRN : String? = "",
    val RMIN: String? = "",
    val GRN: String? = "",
    val MTNT: String? = ""
)
