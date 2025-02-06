package com.olam.warehouse.master.veganicaragua.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminData
import com.olam.warehouse.master.veganicaragua.entity.VegaNicOfflineRminProcessLotDetails

class VegaNicaraguaOfflineRmin {
    @Embedded
    lateinit var rminData: VegaNicOfflineRminData

    @Relation(
        parentColumn = "rminTempId",
        entityColumn = "rminTempId",
        entity = VegaNicOfflineRminProcessLotDetails::class
    )
    var rminLot: List<VegaNicOfflineRminProcessLotDetails> = emptyList()
}
