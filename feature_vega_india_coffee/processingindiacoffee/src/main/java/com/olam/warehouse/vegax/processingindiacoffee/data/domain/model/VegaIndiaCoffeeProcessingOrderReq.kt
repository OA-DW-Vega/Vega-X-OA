package com.olam.warehouse.vegax.processingindiacoffee.data.domain.model

import com.olam.warehouse.master.common.data.domain.model.messageDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.enums.Status

class VegaIndiaCoffeeProcessingOrderReq(
    var key: String? = "",
    var auart: String? = "",
    var cfgNo: String? = "",
    var fevor: String? = "",
    var plant: Plant = Plant()
)

data class VegaIndiaCoffeeRminProcessingPost(
    var processingStage: String? = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var poQuantity: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<VegaIndiaCoffeeProcessingRMINLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var versionId: String? = "",
    var remarks: String? = "",
    var shiftType: String? = "",
    var rsnum: String? = "",
    var rspos: String? = "",
    var xchpf: String? = "",
    var bwart: String? = ""
)

data class VegaIndiaCoffeeProcessingRminResponse(
    var processingStageId: String = "",
    var batchNumber: String? = "",
    var netWeight: String? = "",
    var messages: List<messageDetails>? = emptyList()
)

data class VegaIndiaCoffeeProcessingCreatePoReq(
    var cfgNo: String = "",
    var processingStage: String? = "",
    var batchNumber: String = "",
    var key: String? = "",
    var outputMaterialCode: String? = "",
    var materialName: String? = "",
    var materialCode: String? = "",
    var plant: Plant? = null,
    var processingLotDtls: List<VegaIndiaCoffeeProcessingRMINLotDetails> = emptyList(),
    var rmin: Boolean? = false,
    var poQuantity: String? = "",
    var versionId: String? = "",
    var isSynced: Boolean = false,
    var status: Status = Status.SYNC_PENDING,
    var date: String? = "",
    var syncStatusMsg: String? = "",
    var isProgress: Boolean = false
)
