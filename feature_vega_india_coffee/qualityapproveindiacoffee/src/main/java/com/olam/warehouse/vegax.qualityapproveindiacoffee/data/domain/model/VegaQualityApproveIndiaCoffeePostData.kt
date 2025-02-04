package com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge


data class PostApprovalData(
    val key: String,
    val plant: Plant,
    var approvalDetails: VegaQualityApproveCameroonPostData? = VegaQualityApproveCameroonPostData()
)



//    var messageNav: List<MessageNav> = emptyList(),


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

data class VegaCameroonQualityApprovePost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityApproveCameroonWeighBridge?> = emptyList()
)

data class VegaCameroonQcPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList()
)

data class VegaIndiaCoffeeQualityApprovePostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var encodedImageContent: String? = "",
    var success: Boolean = false
)
