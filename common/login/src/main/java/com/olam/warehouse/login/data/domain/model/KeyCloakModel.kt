package com.olam.warehouse.login.data.domain.model

/**
 * Created by Baskaran Kannan on 2/5/2021.
 */
data class KeyCloakModel(
    var keycloakId: String? = "",
    var userName: String? = "",
    var plantId: String? = "",
    var userSec: String? = "",
    var active: Boolean? = false
)
