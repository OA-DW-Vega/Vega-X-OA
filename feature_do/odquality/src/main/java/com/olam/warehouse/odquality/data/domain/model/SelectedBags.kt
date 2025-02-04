package com.olam.warehouse.odquality.data.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class SelectedBags {
    var map: HashMap<String, MutableSet<QrBag>> = HashMap()
}

@Parcelize
data class QrBag(val qrCode: String, val isSelected: Boolean? = false) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QrBag

        if (qrCode != other.qrCode) return false

        return true
    }

    override fun hashCode(): Int {
        return qrCode.hashCode()
    }
}
