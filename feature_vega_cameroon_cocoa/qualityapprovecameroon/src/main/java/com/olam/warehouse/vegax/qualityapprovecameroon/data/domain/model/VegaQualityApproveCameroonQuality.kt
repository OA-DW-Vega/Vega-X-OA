package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model

import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaQualityParams


/**
 * Created by Baskaran Kannan on 1/22/2020.
 */
data class VegaQualityApproveCameroon(
    var charg: String? = "",
    var materialNumber: String? = "",
    var qualityParameters: List<VegaQualityParams>? = emptyList()
)

data class VegaReprintList(
    var date: String = "",
    var id: Int = 0,
    var materialName: String = "",
    var moduleNo: String? = "",
    var transactionNo: String? = "",
    var fileType: String = "",
    var isProgress: Boolean = false
)

data class CameroonSavePrintTicket(
    var moduleName: String = "",
    var type: String = "",
    var moduleNo: String = "",
    var trasanctionNo: String = "",
    var file: String = "",
    var materialName: String = "",
    var date: String = "",
    var companyCode: String = getCurrentKey().split("_")[1]

)
