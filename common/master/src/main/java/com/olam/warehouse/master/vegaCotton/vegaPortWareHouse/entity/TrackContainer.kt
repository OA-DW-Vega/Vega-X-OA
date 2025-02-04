package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["id"])])
data class TrackContainer(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var containerNumber:String = "",
    var containerType:String = "",
    @Ignore
    var updatedTS:String = ""
)
