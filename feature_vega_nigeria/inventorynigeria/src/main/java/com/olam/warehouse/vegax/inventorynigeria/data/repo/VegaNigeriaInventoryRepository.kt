package com.olam.warehouse.vegax.inventorynigeria.data.repo

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
import com.olam.warehouse.vegax.inventorynigeria.data.api.VegaNigeriaInventoryApi
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.MaterialQuality
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaCocoaInventoryStocks
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaInventoryAndSyncModel
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaNigeriaInventoryLots

/**
 * Created by Roshna Parambil on 9/3/2020.
 */
interface VegaNigeriaInventoryRepository {
    suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>>
    suspend fun fetchQualityDetails(
        charge: String,
        material: String
    ): LiveData<Resource<GenericReqAndResp<List<MaterialQuality>>>>

    suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>>
    suspend fun getProducts(): LiveData<List<VegaMaterial>>
    suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>>
    suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>>
}

class VegaNigeriaInventoryRepositoryImpl(private val api: VegaNigeriaInventoryApi, private val dao: VegaInventoryDao) :
    VegaNigeriaInventoryRepository {

    /* override suspend fun getPurchaseOrder(): LiveData<Resource<GenericReqAndResp<List<VegaPurchaseOrder>>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaPurchaseOrder>>>() {
             override suspend fun createCall(): GenericReqAndResp<List<VegaPurchaseOrder>> =
                 api.getPurchaseOrder(getCurrentKey())
         }.build().asLiveData()
     }*/

    override suspend fun getScanLotDetail(lotId: String): LiveData<Resource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNigeriaInventoryLots>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNigeriaInventoryLots>> =
                api.getScanLotDetail(
                    lotId,
                    getCurrentKey(),
                    "",
                    PreferenceHelper.get(Constants.WERKS, "")
                )
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

    override suspend fun getInventoryList(): LiveData<Resource<GenericReqAndResp<VegaInventoryAndSyncModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaInventoryAndSyncModel>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaInventoryAndSyncModel> =
                api.getInventoryList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun getProducts() = dao.getProducts()
    override suspend fun getConfigItems(role: String): LiveData<List<VegaConfigDetails>> =
        dao.getConfigItems(role)

    override suspend fun getQualityParams(
        charge: String,
        material: String, whId: String
    ): LiveData<Resource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>> {

        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaCocoaInventoryStocks>>>() {

            override suspend fun createCall(): GenericReqAndResp<List<VegaCocoaInventoryStocks>> =
                api.getLotInfo(getCurrentKey(), charge, material, whId)

        }.build().asLiveData()
    }
}
