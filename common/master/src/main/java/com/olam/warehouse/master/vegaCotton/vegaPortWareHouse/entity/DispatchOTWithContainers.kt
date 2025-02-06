package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by SangiliPandian C on 19-02-2019.
 */
class DispatchOTWithContainers {
    @Embedded
    lateinit var dispatchOT: DispatchOT
    @Relation(parentColumn = "otNumber", entityColumn = "otNumber", entity = Container::class)
    var containers: List<ContainerWithBales> = emptyList()
}
