package com.olam.warehouse.vegax.inventorysesame.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.inventorysesame.data.api.VegaNigeriaSesameInventoryApi
import com.olam.warehouse.vegax.inventorysesame.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorysesame.data.domain.model.VegaSesameInventoryStocks

interface VegaNigeriaSesameInventoryRepository {
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaSesameInventoryStocks>>>>
}

class VegaNigeriaSesameInventoryRepositoryImpl(
    private val api: VegaNigeriaSesameInventoryApi,
    private val dao: VegaInventoryDao
) : VegaNigeriaSesameInventoryRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaInventoryAndSyncModel> =
                api.getSesameInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaSesameInventoryStocks>>>> {

        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaSesameInventoryStocks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaSesameInventoryStocks>> =
                api.getLotInfo(currentKey, charge, material, whId)

        }.build().asLiveData()
    }
}

