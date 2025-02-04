package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
class VegaReceivingMtnWithLots {
    @Embedded
    lateinit var mtn: VegaReceivingMtn
    @Relation(parentColumn = "mtnNumber", entityColumn = "mtnNumber")
    var lots: List<VegaReceivingMtnLots> = emptyList()
}
