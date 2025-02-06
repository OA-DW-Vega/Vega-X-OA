package com.olam.warehouse.vegax.notificationconfig.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNotifyModuleList(
    var moduleName: String = "",
    var subModuleName: String? = "",
    var isConfigured: Boolean = false
) : Parcelable
