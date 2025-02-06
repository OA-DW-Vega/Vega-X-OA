package com.olam.warehouse.vegax.approve.data.domain.model

import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */

data class PostApprovalData(
    val key: String,
    val plant: Plant,
    var approvalDetails: VegaApprovePostData? = VegaApprovePostData()
)

data class VegaApprovePostData(
    var weighBridgeId: String? = "",
    var Item: String? = "",
    var materialCode: String? = "",
    var supplierCode: String? = "",
    var batchNumber: String? = "",
    var Lifnr: String? = "",
    var plant: String? = "",
    var Discount: String? = "",
    var UnitPrice: String? = "",
    var paidWeight: String? = "",
    var GrnQty: String? = "",
    var messageNav: List<MessageNav> = emptyList(),
    var charsNav: List<CharsNav> = emptyList()
)

data class CharsNav(
    var CDesc: String? = "",
    var CValue: String? = "",
    var Atnam: String? = "",
    var Disp: String? = ""
)

data class MessageNav(
    var Type: String? = "",
    var Id: String? = "",
    var Number: String? = "",
    var Message: String? = ""
)
