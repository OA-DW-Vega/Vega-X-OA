package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant

data class VegaIndiaCoffeeProcessingRminBomPost(
    val cfgNo: String,
    val fevor: String,
    val materialCode: String,
    var materialCodes: List<String> = emptyList(),
    val key: String,
    val plant: Plant
)

data class VegaNicaRminMaterialProcessType(
    val STAGE_NAME: String? = "",
    val MATERIAL_LIST: List<String> = emptyList(),
    val STAGE_TYPE: String? = ""
)

data class VegaNicaRminProcessTypeModel(
    val RMIN_PROCESS_STAGE_LIST: List<VegaNicaRminMaterialProcessType>
)



