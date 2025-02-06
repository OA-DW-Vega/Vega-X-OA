package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(primaryKeys = ["vendor"])
data class VegaNicaraguaAdvanceDetails(
    var availableLimit: String? = "",
    var creditLimit: String? = "",
    var vendor: String = "",
    var availableLimitSign: String? = "",
    var isLocalCurrency: Boolean? = false,
    @Ignore
    var deletedFlag: Boolean? = false
) : Parcelable
