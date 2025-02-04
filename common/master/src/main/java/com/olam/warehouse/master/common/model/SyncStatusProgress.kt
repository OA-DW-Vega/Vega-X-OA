package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Baskaran Kannan on 9/28/2020.
 */
@Parcelize
data class SyncStatusProgress(
    var itemOrder: Int = 0,
    var itemName: String = "",
    var itemStatus: Boolean = false
) : Parcelable
