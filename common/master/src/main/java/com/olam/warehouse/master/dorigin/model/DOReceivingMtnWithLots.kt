package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtn
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtnLots

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOReceivingMtnWithLots {
    @Embedded
    lateinit var mtn: DOReceivingMtn
    @Relation(parentColumn = "mtnNumber", entityColumn = "mtnNumber")
    var lots: List<DOReceivingMtnLots> = emptyList()
}
