package com.olam.warehouse.vegax.inventorycocoa.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.dao.VegaInventoryDao
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.inventorycocoa.data.api.VegaCocoaInventoryApi
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLotHead
import com.olam.warehouse.vegax.inventorycocoa.data.domain.model.VegaCocoaInventoryLots

/**
 * Created by Baskaran Kannan on 5/21/2020.
 */
interface VegaCocoaInventoryRepository {
    suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>>
    suspend fun fetchQualityDetails(charge: String,
        material: String): LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>>

    suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}

class VegaCocoaInventoryRepositoryImpl(private val api: VegaCocoaInventoryApi, private val dao: VegaInventoryDao) :
    VegaCocoaInventoryRepository {

    /* override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPurchaseOrder>>>() {
             override suspend fun createCall(): GenericReqAndResp<List<VegaPurchaseOrder>> =
                 api.getPurchaseOrder(getCurrentKey())
         }.build().asLiveData()
     }*/

    override suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaInventoryLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaInventoryLots>> =
                api.getScanLotDetail(lotId, getCurrentKey(), "", PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun fetchQualityDetails(charge: String,
        material: String): LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<MaterialQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<MaterialQuality>> =
                api.fetchQualityDetails(getCurrentKey(), charge, material)

        }.build().asLiveData()
    }

    override suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaCocoaInventoryLotHead>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaCocoaInventoryLotHead>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaCocoaInventoryLotHead> =
                api.getInventoryList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
}
