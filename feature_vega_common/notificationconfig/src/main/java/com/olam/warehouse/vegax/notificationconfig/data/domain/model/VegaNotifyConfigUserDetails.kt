package com.olam.warehouse.vegax.notificationconfig.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VegaNotifyConfigUserDetails(
    var id: String = "",
    var userName: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var emailId: String = "",
    var plantId: String = "",
    var warehouseName: String = "",
    var active: Boolean = false
) : Parcelable
