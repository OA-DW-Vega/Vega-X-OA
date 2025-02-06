package com.olam.warehouse.master.veganicaragua.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMtnt
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaWeighmentBagMaterial

/**
 * Created by Baskaran Kannan on 11/19/2020.
 */
class VegaMtntWithLotsWithBags {
    @Embedded
    lateinit var mtnt: VegaNicaraguaMtnt

    @Relation(parentColumn = "tempId", entityColumn = "tempId", entity = VegaNicDispatchLots::class)
    var lineItems: List<VegaNicDispatchLotsWithBags> = emptyList()
}


class VegaNicDispatchLotsWithBags {
    @Embedded
    lateinit var lots: VegaNicDispatchLots

    @Relation(
        parentColumn = "tempIdWithBatch",
        entityColumn = "tmpWbId",
        entity = VegaNicaraguaWeighmentBagMaterial::class
    )
    var bagItems: List<VegaNicaraguaWeighmentBagMaterial> = emptyList()
}
