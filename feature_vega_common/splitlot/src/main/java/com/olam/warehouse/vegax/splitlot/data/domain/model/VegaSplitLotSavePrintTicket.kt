package com.olam.warehouse.vegax.splitlot.data.domain.model

import com.olam.warehouse.master.common.utils.getCurrentKey

/**
 * Created by Baskaran Kannan on 10/5/2022.
 */
data class VegaSplitLotSavePrintTicket(
    var moduleName: String = "",
    var type: String = "",
    var moduleNo: String = "",
    var trasanctionNo: String = "",
    var file: String = "",
    var materialName: String = "",
    var date: String = "",
    var companyCode: String = getCurrentKey().split("_")[1]
)
