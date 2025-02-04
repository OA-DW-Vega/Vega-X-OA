package com.olam.warehouse.master.vegaecuador.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorOffloadingBagMaterial

/**
 * Created by Baskaran Kannan on 7/20/2020.
 */

class VegaEcuaOffloadingWithLineItems {
    @Embedded
    lateinit var receiving: VegaReceiving
    @Relation(parentColumn = "tmpWbId", entityColumn = "tmpWbId", entity = VegaEcuadorOffloadingBagMaterial::class)
    var lineItems: List<VegaEcuadorOffloadingBagMaterial> = emptyList()
}
