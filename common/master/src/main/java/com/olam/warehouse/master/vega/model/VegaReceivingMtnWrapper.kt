package com.olam.warehouse.master.vega.model

import com.olam.warehouse.master.vega.entity.VegaReceivingMtn
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaReceiveLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCoCoaStorageLocation
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots

/**
 * Created by Baskaran Kannan on 1/16/2020.
 */
data class VegaReceivingMtnWrapper(
    val stockSupplyingPlants: List<VegaReceivingWarehouse>,
    val mtns: List<VegaReceivingMtn>,
    val batchDetails: List<VegaReceivingMtnLots>,
    val storageLocationLst: List<VegaSupplyStorageLocation>
)

data class VegaCoffeeReceivingMtnWrapper(
    val stockSupplyingPlants: List<VegaReceivingWarehouse>,
    val mtns: List<VegaReceivingMtn>,
    val batchDetails: List<VegaCoffeeReceiveLots>,
    val storageLocationLst: List<VegaSupplyStorageLocation>
)

data class VegaCoCoaReceivingMtnWrapper(
    val stockSupplyingPlants: List<VegaReceivingWarehouse>,
    val mtns: List<VegaReceivingMtn>,
    val batchDetails: List<VegaCoCoaReceiveLots>,
    val storageLocationLst: List<VegaCoCoaStorageLocation>
)

