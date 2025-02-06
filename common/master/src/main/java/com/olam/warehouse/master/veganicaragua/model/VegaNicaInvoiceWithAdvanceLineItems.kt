package com.olam.warehouse.master.veganicaragua.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaInvoiceDetails

/**
 * Created by Baskaran Kannan on 6/9/2023.
 */
class VegaNicGrnInvoiceWithAdvanceItems {
    @Embedded
    lateinit var grn: VegaReceiving

    @Relation(
        parentColumn = "tmpWbId",
        entityColumn = "tmpWbId",
        entity = VegaNicaraguaAdvanceLineItemGrn::class
    )
    var advanceItems: List<VegaNicaraguaAdvanceLineItemGrn>? = emptyList()
}

class VegaNicPtbfInvoiceWithAdvanceItems {
    @Embedded
    lateinit var invoice: VegaNicaraguaInvoiceDetails

    @Relation(
        parentColumn = "tempId",
        entityColumn = "tmpWbId",
        entity = VegaNicaraguaAdvanceLineItemGrn::class
    )
    var advanceItems: List<VegaNicaraguaAdvanceLineItemGrn>? = emptyList()
}