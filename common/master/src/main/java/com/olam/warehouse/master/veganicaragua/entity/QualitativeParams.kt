package com.olam.warehouse.master.veganicaragua.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(primaryKeys = ["paramName"])
data class QualitativeParams(
    var paramDesc: String? = "",
    var paramName: String = ""
) : Parcelable
