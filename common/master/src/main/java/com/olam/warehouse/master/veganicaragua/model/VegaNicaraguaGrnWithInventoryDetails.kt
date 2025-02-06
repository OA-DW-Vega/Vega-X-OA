package com.olam.warehouse.master.veganicaragua.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.veganicaragua.entity.GrnDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGRNInventoryDetails

/**
 * Created by Baskaran Kannan on 10/22/2020.
 */
class VegaNicaraguaGrnWithInventoryDetails {
    @Embedded
    lateinit var grn: GrnDetails

    @Relation(
        parentColumn = "batchNumber",
        entityColumn = "lotId",
        entity = VegaNicaraguaGRNInventoryDetails::class
    )
    var inventory: List<VegaNicaraguaGRNInventoryDetails>? = emptyList()
}
