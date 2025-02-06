package com.olam.warehouse.vegax.inventorynicaragua.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.veganicaragua.dao.VegaNicInventoryDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.vegax.inventorynicaragua.data.api.VegaNicInventoryApi
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.VegaNicInventoryStocks

/**
 * Created by Baskaran Kannan on 12/11/2020.
 */
interface VegaNicInventoryRepository {
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getQualityParams(
        charge: String,
        material: String,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>>
}

class VegaNicInventoryRepositoryImpl(
        private val api: VegaNicInventoryApi,
        private val dao: VegaNicInventoryDao
) : VegaNicInventoryRepository {
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNicInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNicInventoryAndSyncModel> =
                api.getInventoryList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getProducts(): LiveData<List<VegaMaterial>> = dao.getProducts()
    override suspend fun getQualityParams(
        charge: String,
        material: String,
        whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaNicInventoryStocks>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNicInventoryStocks>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNicInventoryStocks>> =
                api.getLotInfo(getCurrentKey(), charge, material, whId)
        }.build().asLiveData()
    }

}
