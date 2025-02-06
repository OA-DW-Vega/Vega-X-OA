package com.olam.warehouse.vegax.portwarehouse.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Deliverypostlist(
    var deliveryNumber: String? = ""
) : Parcelable

data class Deliverylist(
    var deliveryList: List<Deliverypostlist> = emptyList()

)
