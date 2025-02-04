package com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
class MtnWithBales : Parcelable {
    @Embedded
    lateinit var mtn: Mtn
    @Relation(parentColumn = "mtnNumber", entityColumn = "mtnNumber", entity = MtnBales::class)
    var bales: List<MtnBales> = emptyList()
}
