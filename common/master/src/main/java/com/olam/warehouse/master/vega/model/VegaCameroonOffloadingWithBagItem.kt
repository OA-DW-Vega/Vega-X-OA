package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaCameroonOffloadingBagMaterial
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial

class VegaCameroonOffloadingWithBagItem {
    @Embedded
    lateinit var receiving: VegaReceiving
    @Relation(parentColumn = "weighBridgeId", entityColumn = "wbId", entity = VegaCameroonOffloadingBagMaterial::class)
    var lineItems: List<VegaEcuadorOffloadingBagMaterial> = emptyList()
}
