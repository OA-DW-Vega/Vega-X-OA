package com.olam.warehouse.vegax.inventoryindo.data.repo

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
import com.olam.warehouse.vegax.inventoryindo.data.api.VegaIndoCoffeeInventoryApi
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLotHead
import com.olam.warehouse.vegax.inventoryindo.data.domain.model.VegaIndoCoffeeInventoryLots

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
interface VegaIndoCoffeeInventoryRepository {
    suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeInventoryLots>>>>
    suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>>

    suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeInventoryLotHead>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
}

class VegaIndoCoffeeInventoryRepositoryImpl(
    private val api: VegaIndoCoffeeInventoryApi,
    private val dao: VegaInventoryDao
) :
    VegaIndoCoffeeInventoryRepository {

    /* override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndoCoffeeInventoryLotsVegaPurchaseOrder>>>() {
             override suspend fun createCall(): GenericReqAndResp<List<VegaPurchaseOrder>> =
                 api.getPurchaseOrder(getCurrentKey())
         }.build().asLiveData()
     }*/

    override suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaIndoCoffeeInventoryLots>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaIndoCoffeeInventoryLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaIndoCoffeeInventoryLots>> =
                api.getScanLotDetail(lotId, getCurrentKey(), "", PreferenceHelper.get(Constants.WERKS, ""))
        }.build().asLiveData()
    }

    override suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<MaterialQuality>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<MaterialQuality>> =
                api.fetchQualityDetails(getCurrentKey(), charge, material)

        }.build().asLiveData()
    }

    override suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaIndoCoffeeInventoryLotHead>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaIndoCoffeeInventoryLotHead>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaIndoCoffeeInventoryLotHead> =
                api.getInventoryList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> = dao.getConfigItems(role)
}

