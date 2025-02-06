package com.olam.warehouse.master.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Parcelize
data class StorageLocation(
    var plant: String = "",
    var storageLocationCode: String = "",
    var storageLocationName: String? = "") : Parcelable
