package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.olam.warehouse.ginning.ui.pile.db.dao.GinningPileDao
import com.olam.warehouse.ginning.ui.pile.db.entity.GinningPileStorageLocationModel
import com.olam.warehouse.ginning.ui.pile.db.entity.GinningPileSuccessResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.GinningPileApi
import java.util.*


interface GinningPileRepository
{
    suspend fun getPileLocations(): LiveData<Resource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>>
    suspend fun validateBale(
        baleId: String,
        storageLoctaion: String
    ): LiveData<Resource<GenericReqAndResp<List<PileBale>>>>

    suspend fun insertBaleToStorage(bale: List<PileBale>)
    suspend fun getBaleListByStorageId(id: String): List<PileBale>
    suspend fun removeBaleFromStorage(bale: PileBale)
    suspend fun getExistedBaleList(): LiveData<List<PileBale>>
    suspend fun deleteBalesDB(pileId: String)
    suspend fun postPile(mCurrentPiles: GinningPileStorageLocationModel): LiveData<Resource<GenericReqAndResp<GinningPileSuccessResponse>>>

}

class VegaGinningPileRepositoryImpl(private val api: GinningPileApi, private val dao: GinningPileDao) :
    GinningPileRepository {
    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    //val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")


    override suspend fun getPileLocations():  LiveData<Resource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<GinningPileStorageLocationModel>>>() {
            override suspend fun createCall() = api.getStorageLocationList(getCurrentKey())
        }.build().asLiveData()
    }


    override suspend fun  postPile(mCurrentPiles: GinningPileStorageLocationModel): LiveData<Resource<GenericReqAndResp<GinningPileSuccessResponse>>>
    {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GinningPileSuccessResponse>>() {
            override suspend fun createCall() = api.updateBaleToPile(getCurrentKey(), mCurrentPiles)
        }.build().asLiveData()
    }

    override suspend fun validateBale(
        baleId: String,
        storageLoctaion: String
    ): LiveData<Resource<GenericReqAndResp<List<PileBale>>>>  {
        var jsonObject = JsonObject()
        jsonObject.addProperty("baleId", baleId)
        jsonObject.addProperty("storageLocationCode", storageLoctaion)
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<PileBale>>>() {
            override suspend fun createCall() = api.validateBale(getCurrentKey(), jsonObject)
        }.build().asLiveData()
    }


    override suspend fun insertBaleToStorage(bale: List<PileBale>) = dao.saveListItem(bale)

    override suspend fun getBaleListByStorageId(id: String) = dao.getBaleByStorageLocationId(id)

    override suspend fun  removeBaleFromStorage(bale: PileBale)=dao.remove(bale)

    override suspend fun  getExistedBaleList()=dao.getExistedBaleList()

    override suspend fun deleteBalesDB(pileId: String) =dao.deleteBalesDB(pileId)

    /*  override suspend fun fetchingIncomingLots(): LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> {
          return object : NetworkOnlyBoundResource<GenericReqAndResp<List<IncomingLot>>>() {
              override suspend fun createCall() = api.getIncomingLots(warehouseId)
          }.build().asLiveData()
      }

      override suspend fun postLotForGinning( incomingLot: IncomingLot): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
          return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
              override suspend fun createCall() = api.sendLotForGinning(incomingLot)
          }.build().asLiveData()
      }*/
}

/*constructor(
    val api: GinningPileApi,
    val appExecutors: AppExecutors,
    val dao: GinningPileDao
) {
    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    fun getPileLocationList(
    ): LiveData<Resource<GenericResponse<List<GinningPileStorageLocationModel>>>> {
        return object :
            NetworkOnlyBoundResource<GenericResponse<List<GinningPileStorageLocationModel>>>(
                appExecutors
            ) {
            override fun createCall() =
                api.getStorageLocationList(PreferenceHelper.get(Constants.WAREHOUSE_ID, 0))
        }.asLiveData()
    }

    fun validateBale(
        baleId: String, locationId: String
    ): LiveData<Resource<GenericResponse<List<PileBale>>>> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("baleId", baleId)
        jsonObject.addProperty("storageLocationCode", locationId)
        return object : NetworkOnlyBoundResource<GenericResponse<List<PileBale>>>(appExecutors) {
            override fun createCall() = api.validateBale(jsonObject)
        }.asLiveData()
    }

    /*fun validateBale(baleId: String, locationId: String): LiveData<Resource<List<PileBale>>> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("baleId", baleId)
        jsonObject.addProperty("storageLocationCode", locationId)
        return object :
            NetworkBoundResource<List<PileBale>, GenericResponse<List<PileBale>>>(appExecutors) {
            override fun saveCallResult(item: GenericResponse<List<PileBale>>) {
                item.data?.let { item1 ->
                    item1.forEach {
                        it.storageLocationTo = locationId
                        dao.saveItem(it)
                    }

                }
            }

            override fun shouldFetch(data: List<PileBale>?) = true

            override fun loadFromDb() = dao.getBaleByStorageLocation(locationId)

            override fun createCall() = api.validateBale(jsonObject)
        }.asLiveData()
    }*/

    suspend fun insertBale(bale: List<PileBale>) = dao.saveListItem(bale)

    fun getBaleByLocationId(id: String) = dao.getBaleByStorageLocationId(id)

    suspend fun removeBale(bale: PileBale) = dao.remove(bale)

    fun isAlreadyExistBale(baleId: String) = dao.isBaleAlreadyExist(baleId)

    fun postPile(mCurrentPiles: GinningPileStorageLocationModel): LiveData<Resource<GenericResponse<GinningPileSuccessResponse>>> {
        return object :
            NetworkOnlyBoundResource<GenericResponse<GinningPileSuccessResponse>>(appExecutors) {
            override fun createCall() = api.updateBaleToPile(mCurrentPiles)
        }.asLiveData()
    }

    fun deleteBalesDB(pileId: String) = dao.deleteBalesDB(pileId)
}
*/
