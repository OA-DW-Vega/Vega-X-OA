package com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge

/**
 * Created by Baskaran Kannan on 1/22/2020.
 */

data class PostApprovalData(
    val key: String,
    val plant: Plant,
    var approvalDetails: VegaQualityApproveCameroonPostData? = VegaQualityApproveCameroonPostData(),
    var approvalDetailsList: List<VegaQualityApproveCameroonPostData?> = emptyList(),
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = ""
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

data class VegaNigeriaQualityApprovePost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityApproveCameroonWeighBridge?> = emptyList()
)

data class VegaNigeriaQcPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList(),
    var notificationFlag: Boolean = false,
    var nextWorkFlowRole: String? = "",
    var navId: String? = "",
    var currentWorkFlowRole: String? = "",
    var environment: String? = ""
)

data class VegaNigeriaQualityApprovePostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var encodedImageContent: String? = "",
    var success: Boolean = false
)
