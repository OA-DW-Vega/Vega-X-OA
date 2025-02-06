package com.olam.warehouse.master.dorigin.model

import com.olam.warehouse.master.dorigin.entity.DOReceivingMtn
import com.olam.warehouse.master.dorigin.entity.DOReceivingMtnLots
import com.olam.warehouse.master.dorigin.entity.DOReceivingWarehouse

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
data class DOReceivingMtnWrapper(
    val stockSupplyingPlants: List<DOReceivingWarehouse>,
    val mtns: List<DOReceivingMtn>,
    val batchDetails: List<DOReceivingMtnLots>
)
