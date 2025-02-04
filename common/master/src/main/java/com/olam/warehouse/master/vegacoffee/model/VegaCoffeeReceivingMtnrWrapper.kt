package com.olam.warehouse.master.vegacoffee.model

import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation

data class VegaCoffeeReceivingMtnrWrapper(
    val stockSupplyingPlants: List<VegaReceivingWarehouse>,
    val mtns: List<VegaReceivingMtn>,
    val batchDetails: List<VegaReceivingMtnLots>,
    val storageLocationLst: List<VegaSupplyStorageLocation>
)
