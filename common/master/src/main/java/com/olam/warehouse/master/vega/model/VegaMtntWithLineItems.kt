package com.olam.warehouse.master.vega.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaMtntLineItem

/**
 * Created by Baskaran Kannan on 5/9/2020.
 */
class VegaMtntWithLineItems {
    @Embedded
    lateinit var mtnt: VegaMtnt
    @Relation(parentColumn = "tmpWbId", entityColumn = "tmpWbId", entity = VegaMtntLineItem::class)
    var lineItems: List<VegaMtntLineItem> = emptyList()
}
