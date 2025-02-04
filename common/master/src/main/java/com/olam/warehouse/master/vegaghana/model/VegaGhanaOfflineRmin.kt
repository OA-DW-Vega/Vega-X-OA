package com.olam.warehouse.master.vegaghana.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminData
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineRminProcessLotDetails


class VegaGhanaOfflineRmin {
    @Embedded
    lateinit var rminData: VegaGhanaOfflineRminData
    @Relation(parentColumn = "rminTempId", entityColumn = "rminTempId", entity = VegaGhanaOfflineRminProcessLotDetails::class)
    var rminLot: List<VegaGhanaOfflineRminProcessLotDetails> = emptyList()
}
