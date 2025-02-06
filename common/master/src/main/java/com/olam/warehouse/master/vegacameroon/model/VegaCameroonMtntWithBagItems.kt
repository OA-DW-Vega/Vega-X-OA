package com.olam.warehouse.master.vegacameroon.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial

class VegaCameroonMtntWithBagItems{

    @Embedded
    lateinit var dispatchLots: VegaCocoaDispatchLots

    @Relation(
        parentColumn = "batchNumber",
        entityColumn = "batchNumber",
        entity = VegaCocoaSweepingBagMaterial::class
    )
    var bagMaterialList: List<VegaCocoaSweepingBagMaterial> = emptyList()
}
