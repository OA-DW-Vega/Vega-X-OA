package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
class MtnWithBales : Parcelable {
    @Embedded
    lateinit var mtn: PortMtn

    @Relation(parentColumn = "mtnNumber", entityColumn = "mtnNumber", entity = PortMtnBales::class)
    var bales: List<PortMtnBales> = emptyList()
}
