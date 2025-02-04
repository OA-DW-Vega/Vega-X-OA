package com.olam.warehouse.vegax.inventorycameroon.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.inventorycameroon.data.api.VegaCameroonInventoryApi
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.VegaInventoryAndSyncModel

interface VegaCameroonInventoryRepository {
    suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}

class VegaCameroonInventoryRepositoryImpl(
    private val api: VegaCameroonInventoryApi,
    private val dao: VegaInventoryDao
) : VegaCameroonInventoryRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getInventoryDetails(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaInventoryAndSyncModel> =
                api.getEcuadorInventoryList(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
}

