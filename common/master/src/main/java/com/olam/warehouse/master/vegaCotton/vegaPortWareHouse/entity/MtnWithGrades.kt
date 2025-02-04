package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
class MtnWithGrades : Parcelable {
    @Embedded
    lateinit var mtn: PortMtn

    @Relation(parentColumn = "mtnNumber", entityColumn = "mtnNumber", entity = PortMtnGrades::class)
    var grades: List<PortMtnGrades> = emptyList()
}
