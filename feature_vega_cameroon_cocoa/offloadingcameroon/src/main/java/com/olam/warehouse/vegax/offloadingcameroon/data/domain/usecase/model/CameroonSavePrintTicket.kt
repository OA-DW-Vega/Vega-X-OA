package com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model

import com.olam.warehouse.master.common.utils.getCurrentKey

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
