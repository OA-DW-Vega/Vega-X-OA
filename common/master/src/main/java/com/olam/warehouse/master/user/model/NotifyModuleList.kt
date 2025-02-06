package com.olam.warehouse.master.user.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifyModuleList(
    var id: String = "",
    var moduleDesc: String = "",
    var moduleName: String = "",
    var notificationProcessName: String? = "",
    var isConfigured: Boolean = false
) : Parcelable
