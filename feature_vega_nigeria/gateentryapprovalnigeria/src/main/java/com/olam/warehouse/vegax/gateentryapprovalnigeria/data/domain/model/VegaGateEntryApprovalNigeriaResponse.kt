package com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model

/**
 * Created by Baskaran Kannan on 3/11/2020.
 */

data class VegaGateEntryApprovalNigeriaResponse(val wbId: String? = "", val txnId: String? = "", val batchNumber:String?="")

data class VegaDMSImageResponse(
    var wbId: String? = "",
    var encodedImageContent: String? = "",
    var message: String? = "",
    var success: Boolean? = false
)
