package com.olam.warehouse.vegax.portwarehouse.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileSuccessResponse
import com.olam.warehouse.portwarehouse.ui.pile.db.dao.PortPileDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.portwarehouse.data.api.PortPileApi
import java.util.*

interface PortPileRepository
{
    suspend fun getStorageLocationList(): LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>>
    suspend fun validateBale(baleId: String, locationId: String): LiveData<Resource<GenericReqAndResp<PortPileBale>>>
    suspend fun insertBale(bale: PortPileBale)
    suspend fun getBaleByLocationId(id: String): LiveData<List<PortPileBale>>
    suspend fun getBaleByLocationIdOffline(id: String): List<PortPileBale>
    suspend fun isAlreadyExistBale(baleId: String): Int
    suspend fun deleteBalesDB(pileId: String)
    suspend fun removeBale(bale: PortPileBale)
    suspend fun postPile(mCurrentPiles: PortPileStorageLocationModel): LiveData<Resource<GenericReqAndResp<PortPileSuccessResponse>>>
}

class VegaPortWareHousePortPileRepositoryImpl(private val api: PortPileApi,val dao: PortPileDao) :
    PortPileRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
//    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

   override suspend fun postPile(mCurrentPiles: PortPileStorageLocationModel): LiveData<Resource<GenericReqAndResp<PortPileSuccessResponse>>>
    {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<PortPileSuccessResponse>>() {
            override suspend fun createCall() = api.updateBaleToPile(getCurrentKey(), mCurrentPiles)
        }.build().asLiveData()
    }

    override suspend fun getStorageLocationList(
    ): LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PortPileStorageLocationModel>>>() {
            override suspend fun createCall() = api.getStorageLocationList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun validateBale(
        baleId: String,
        locationId: String
    ): LiveData<Resource<GenericReqAndResp<PortPileBale>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<PortPileBale>>() {
            override suspend fun createCall() =
                api.validateBale(getCurrentKey(), baleId, locationId)
        }.build().asLiveData()
    }

    override suspend fun insertBale(bale: PortPileBale) = dao.saveItem(bale)
    override suspend fun getBaleByLocationId(id: String) = dao.getBaleByStorageLocationId(id)
    override suspend fun getBaleByLocationIdOffline(id: String) = dao.getBaleByLocationIdOffline(id)
    override suspend fun isAlreadyExistBale(baleId: String) = dao.isBaleAlreadyExist(baleId)
    override suspend fun deleteBalesDB(pileId: String) = dao.deleteBalesDB(pileId)
    override suspend fun removeBale(bale: PortPileBale) = dao.deleteBaleDB(bale.baleID)
}
/*class PortPileRepository constructor(
    val api: PortPileApi,
    val appExecutors: AppExecutors,
    val dao: PortPileDao
) {
    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    fun getPileLocationList(
    ): LiveData<Resource<GenericReqAndResp<List<PortPileStorageLocationModel>>>> {
        return object :
            NetworkOnlyBoundResource<GenericResponse<List<PortPileStorageLocationModel>>>(
                appExecutors
            ) {
            override fun createCall() =
                api.getStorageLocationList(PreferenceHelper.get(Constants.WAREHOUSE_ID, 0))
        }.asLiveData()
    }

    fun validateBale(
        baleId: String, locationId: String
    ): LiveData<Resource<GenericResponse<PileBale>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<PileBale>>(appExecutors) {
            override fun createCall() = api.validateBale(baleId, locationId)
        }.asLiveData()
    }

    /* fun validateBale(baleId: String, locationId: String): LiveData<Resource<List<PileBale>>> {
         val jsonObject = JsonObject()
         jsonObject.addProperty("baleId", baleId)
         jsonObject.addProperty("storageLocationCode", locationId)
         return object :
             NetworkBoundResource<List<PileBale>, GenericResponse<PileBale>>(appExecutors) {
             override fun saveCallResult(item: GenericResponse<PileBale>) {
                 item.data?.let { item1 ->
                     item1.storageLocationTo = locationId
                     dao.saveItem(item1)
                 }
             }

             override fun shouldFetch(data: List<PileBale>?) = true

             override fun loadFromDb() = dao.getBaleByStorageLocation(locationId)

             override fun createCall() = api.validateBale(baleId, locationId)
         }.asLiveData()
     }*/

    fun insertBale(bale: PileBale) = dao.saveItem(bale)

    fun getBaleByLocationId(id: String) = dao.getBaleByStorageLocationId(id)

    fun removeBale(bale: PileBale) = dao.deleteBaleDB(bale.baleID)

    fun isAlreadyExistBale(baleId: String) = dao.isBaleAlreadyExist(baleId)

    fun postPile(mCurrentPiles: PortPileStorageLocationModel): LiveData<Resource<GenericResponse<PortPileSuccessResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericResponse<PortPileSuccessResponse>>(appExecutors) {
            override fun createCall() = api.updateBaleToPile(mCurrentPiles)
        }.asLiveData()
    }

    fun deleteBalesDB(pileId: String) = dao.deleteBalesDB(pileId)
}*/
