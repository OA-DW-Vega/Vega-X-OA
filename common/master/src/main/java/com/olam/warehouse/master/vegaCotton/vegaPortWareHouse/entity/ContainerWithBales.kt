package com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by SangiliPandian C on 19-02-2019.
 */
class ContainerWithBales {
    @Embedded
    lateinit var container: Container

    @Relation(parentColumn = "containerNumber", entityColumn = "containerNumber", entity = PortBale::class)
    var bales: List<PortBale> = emptyList()
}
