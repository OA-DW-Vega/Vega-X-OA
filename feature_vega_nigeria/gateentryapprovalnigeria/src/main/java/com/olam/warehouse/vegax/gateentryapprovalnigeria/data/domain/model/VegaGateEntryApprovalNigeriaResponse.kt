package com.olam.warehouse.vegax.gateentryapprovalnigeria.data.domain.model

/**
 * Created by Baskaran Kannan on 3/11/2020.
 */

data class VegaGateEntryApprovalNigeriaResponse(val wbId: String? = "", val txnId: String? = "")

data class VegaDMSImageResponse(
    var delInd: String? = "",
    var encodedString: String? = "",
    var fileName: String? = ""
)
