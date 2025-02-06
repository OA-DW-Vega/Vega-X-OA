package com.olam.warehouse.vegax.inventorycoffee.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeInventoryDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.inventorycoffee.data.api.VegaCoffeeInventoryApi
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeInventoryStocks
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeeInventoryAndSyncModel

interface VegaCoffeeInventoryRepository {
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>>
}

   class VegaCoffeeInventoryRepositoryImpl(
       private val api: VegaCoffeeInventoryApi,
       private val dao: VegaCoffeeInventoryDao
   ) : VegaCoffeeInventoryRepository {
       val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
       override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>> {
           return object :
               NetworkOnlyBoundResource<GenericReqAndResp<VegaCoffeeInventoryAndSyncModel>>() {
               override suspend fun createCall(): GenericReqAndResp<VegaCoffeeInventoryAndSyncModel> =
                   api.getInventoryList(currentKey)
           }.build().asLiveData()
       }

    override suspend fun getProducts() = dao.getProducts()

    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCoffeInventoryStocks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCoffeInventoryStocks>> =
                api.getLotInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }
}
