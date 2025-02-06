package com.olam.warehouse.vegax.secretidcommon.data.domain.usecase

import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminProcessing
import com.olam.warehouse.vegax.secretidcommon.data.repo.VegaCommonSecretIdRepository

class VegaCommonSecretIdUseCase(val repository: VegaCommonSecretIdRepository) {


    suspend fun getAllStockByPlant()= repository.getStockByPlant()

    suspend fun getWeighBridgeDetailOnline() = repository.getWeighBridgeDetailOnline()

    suspend fun getValidLotsList(charge: String, material: String, whId: String) =
        repository.getValidLots(charge, material, whId)

    suspend fun getSecretIdForLot(batchNo: String,
                                  isSecretIdExists: Boolean,
                                  materialCode: String,
                                  plantFk:String)= repository.getSecretId(batchNo, isSecretIdExists, materialCode, plantFk)



    // suspend fun saveLot(list: MutableList<VegaCoffeeRminLots>) = repository.saveLots(list)

}
