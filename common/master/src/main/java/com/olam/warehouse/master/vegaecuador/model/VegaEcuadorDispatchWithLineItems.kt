package com.olam.warehouse.master.vegaecuador.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatch
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchLots

/**
 * Created by Keerthi Santhanam on 8/02/2020.
 */

class VegaEcuadorDispatchWithLineItems {
    @Embedded
    lateinit var dispatch: VegaEcuadorDispatch
    @Relation(parentColumn = "wbTempId", entityColumn = "wbTempId", entity = VegaEcuadorDispatchLots::class)
    var lineItems: List<VegaEcuadorDispatchLots> = emptyList()
    var deliveryDetails: String? = ""
}
