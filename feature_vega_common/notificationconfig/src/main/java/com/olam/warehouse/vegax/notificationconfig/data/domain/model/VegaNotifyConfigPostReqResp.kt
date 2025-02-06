package com.olam.warehouse.vegax.notificationconfig.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNotifyConfigPostReqResp(
    var loggedInUser: String = "",
    var notificationUser: String = "",
    var moduleDetails: List<VegaNotifyConfigModule> = emptyList()
) : Parcelable

@Parcelize
data class VegaNotifyConfigModule(
    var moduleName: String = "",
    var subModuleName: String = "",
    var notificationFlag: Boolean = false,
    var deleteFlag: Boolean = false
) : Parcelable
