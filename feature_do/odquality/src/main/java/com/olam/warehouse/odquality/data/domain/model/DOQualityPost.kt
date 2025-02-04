package com.olam.warehouse.odquality.data.domain.model

import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.master.user.model.Plant

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class DOQualityPost(
    val key: String,
    val plant: Plant,
    var lotDetails: List<DOQualityWBDetails?> = emptyList()
)

data class DOQualityPostResponse(
    var charg: String? = "",
    var currentWbid: String? = "",
    var message: String? = "",
    var previousWbid: String? = "",
    var success: Boolean = false
)
