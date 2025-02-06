package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem

/**
 * Created by Baskaran Kannan on 12/19/2019.
 */
class DOReceivingWithLineItems {
    @Embedded
    lateinit var receiving: DOReceiving
    @Relation(parentColumn = "tmpWbId", entityColumn = "tmpWbId", entity = DOReceiving::class)
    var lineItems: List<DOReceivingLineItem> = emptyList()

    override fun toString(): String {
        return "DOReceivingWithLineItems(receiving=$receiving, lineItems=$lineItems)"
    }


}
