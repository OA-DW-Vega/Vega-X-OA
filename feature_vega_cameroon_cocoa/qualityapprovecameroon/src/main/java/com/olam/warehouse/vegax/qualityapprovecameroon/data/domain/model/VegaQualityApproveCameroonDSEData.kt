package com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model

import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper

// Model class to store DSE Values
data class VegaQualityApproveCameroonDSEData(

    var id: Int,
    var requestId: String? = "",
    var plant: String? = PreferenceHelper.get(Constants.WERKS, ""),
    var wbId: String? = "",
    var materialCode: String? = "",
    var lotId: String? = "",
    var dwRequestTime: String? = "",
    var dwStatus: String? = "",
    var dseStatus: String? = "",
    var dseResponseTime: String? = "",
    var dseRemarks: String? = "",
    var createdAt: String? = "",
    var createdBy: String? = "",
    var updatedAt: String? = "",
    var updatedBy: String? = ""

)

