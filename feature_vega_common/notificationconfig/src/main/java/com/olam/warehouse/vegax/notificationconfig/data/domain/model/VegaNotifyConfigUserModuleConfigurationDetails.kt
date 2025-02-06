package com.olam.warehouse.vegax.notificationconfig.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNotifyConfigUserModuleConfigurationDetails(
    var id: String = "",
    var userName: String = "",
    var moduleName: String = "",
    var subModuleName: String = "",
    var notificationFlag: Boolean = false,
    var deleteFlag: Boolean = false
) : Parcelable
