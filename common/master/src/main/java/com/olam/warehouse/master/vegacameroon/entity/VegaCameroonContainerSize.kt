package com.olam.warehouse.master.vegacameroon.entity

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
@Parcelize
@Entity(primaryKeys = ["id"])
data class VegaCameroonContainerSize(
    var id: String ="",
    var containerSize:String = ""
) : Parcelable


