package com.olam.warehouse.vegax.inventoryghanacocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.inventoryghanacocoa.data.api.VegaGhanaCocoaInventoryApi
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaGhanaInventoryStocks
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaInventoryAndSyncModel

interface VegaGhanaCocoaInventoryRepository {
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getuomDetail(): LiveData<List<VegaUomDetails>>
    suspend fun fetchWarehouseWithMtns(isInventory: Boolean): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>>
}

class VegaGhanaCocoaInventoryRepositoryImpl(
    private val api: VegaGhanaCocoaInventoryApi,
    private val dao: VegaInventoryDao
) : VegaGhanaCocoaInventoryRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaInventoryAndSyncModel> =
                api.getSesameInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun fetchWarehouseWithMtns(isInventory: Boolean): LiveData<Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaReceivingMtnWrapper>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaReceivingMtnWrapper> =
                api.fetchWarehouseWithMtns(currentKey, isInventory)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        dao.getConfigItems(role)

    override suspend fun getuomDetail() = dao.getuomDetail()
    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaGhanaInventoryStocks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaGhanaInventoryStocks>> =
                api.getLotInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }
}

