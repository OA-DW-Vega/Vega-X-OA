package com.olam.warehouse.vegax.qualityofanylot.data.domain.model

import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData

/**
 * Created by Ramesh Rm on 07/11/2022.
 */

data class PostApprovalData(
    val key: String,
    val plant: Plant,
    var approvalDetails: VegaQualityApproveCameroonPostData? = VegaQualityApproveCameroonPostData(),
    var approvalDetailsList: List<VegaQualityApproveCameroonPostData?> = emptyList()
)

data class VegaCocoaQcPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<VegaQualityWBDetails?> = emptyList()
)

data class VegaCocoaQualityApprovePostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var grnNumber: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var encodedImageContent: String? = "",
    var success: Boolean = false
)
