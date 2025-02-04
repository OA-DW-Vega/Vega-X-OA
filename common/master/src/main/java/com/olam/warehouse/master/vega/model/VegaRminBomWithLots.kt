package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.ProcessingLotDetails
import com.olam.warehouse.master.vega.entity.VegaProcessingCreatePoReq

/**
 * Created by Baskaran Kannan on 5/13/2020.
 */
class VegaRminBomWithLots {

    @Embedded
    lateinit var rminPo: VegaProcessingCreatePoReq
    @Relation(parentColumn = "batchNumber", entityColumn = "batchNumber", entity = ProcessingLotDetails::class)
    var lineItems: List<ProcessingLotDetails> = emptyList()
}
