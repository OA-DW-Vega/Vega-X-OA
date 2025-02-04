package com.olam.warehouse.presentation.data.domain.model

import com.olam.warehouse.presentation.utils.Constants.DO_CLIENT_ID
import com.olam.warehouse.presentation.utils.Constants.DO_LANGUAGE_ISO_CODE

/**
 * Created by SangiliPandian C on 17-11-2019.
 */

data class DoAuth(
    var appInstanceId: String = "",
    var clientId: Int = DO_CLIENT_ID,
    var id: String = "",
    var languageIsoCode: String = DO_LANGUAGE_ISO_CODE
//    var password: String = DO_PASS_WORD
)

data class DOAuthResponse(
    var accessToken: String = "",
    var languageIsoCode: String = "",
    var originId: Int = 0,
    var otpStatus: Boolean = false,
    var refreshToken: String = "",
    var systemUserId: String = "",
    var timeZoneIsoCode: String = "",
    var tokenExpiry: String = "",
    var userName: String = ""
)
