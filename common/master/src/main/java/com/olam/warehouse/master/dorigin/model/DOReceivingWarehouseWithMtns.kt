package com.olam.warehouse.master.dorigin.model

import androidx.room.Embedded
import androidx.room.Relation
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtn
import com.olam.warehouse.master.dorigin.entity.DOReceivingWarehouse

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
class DOReceivingWarehouseWithMtns {
    @Embedded
    lateinit var warehouse: DOReceivingWarehouse
    @Relation(parentColumn = "supplyingPlantId", entityColumn = "supplyingPlantId", entity = DOReceivingMtn::class)
    var mtns: List<DOReceivingMtnWithLots> = emptyList()
}
