package com.olam.warehouse.portwarehouse.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Deliverypostlist(
    var deliveryNumber: String? = ""
) : Parcelable

data class Deliverylist(
    var deliveryList: List<Deliverypostlist> = emptyList()

)
