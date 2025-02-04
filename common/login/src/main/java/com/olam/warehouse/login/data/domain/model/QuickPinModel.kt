package com.olam.warehouse.login.data.domain.model

/**
 * Created by Baskaran Kannan on 11/26/2020.
 */
data class QuickPinModel(
    var deviceId: String? = "",
    var quickPin: String? = "",
    var systemPin: Boolean? = false
)
