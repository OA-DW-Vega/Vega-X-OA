package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.InventoryApi
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.BaleGrade
import java.util.*


interface InventoryRepository
{
    suspend fun getInventoryGrades():LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>>
    suspend fun fetchInventoryBaleList():LiveData<Resource<GenericReqAndResp<List<Bale>>>>
    suspend fun syncBaleByGrade(grade: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
}
class VegaGinningInventoryRepositoryImpl(private val api: InventoryApi) :
    InventoryRepository {
    //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
//    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

   override suspend fun getInventoryGrades(): LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<BaleGrade>>>() {
            override suspend fun createCall() = api.getInventoryGrades(getCurrentKey())
        }.build().asLiveData()
    }


    override suspend fun fetchInventoryBaleList(): LiveData<Resource<GenericReqAndResp<List<Bale>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<Bale>>>() {
            override suspend fun createCall() = api.fetchInventoryBaleList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun syncBaleByGrade(grade: String):LiveData<Resource<GenericReqAndResp<GenericMessage>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.syncBaleByGrade(getCurrentKey(), grade)
        }.build().asLiveData()
    }

    /* override suspend fun  postLotForGinning(mIncomingLot: List<IncomingLot>): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
             override suspend fun createCall() = api.sendLotForGinning(mIncomingLot)
         }.build().asLiveData()
     }*/
}

/*constructor(
    private val api: InventoryApi,
    private val appExecutors: AppExecutors
) {

    fun fetchInventoryBaleList(
    ): LiveData<Resource<GenericResponse<List<Bale>>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<List<Bale>>>(appExecutors) {
            override fun createCall() = api.fetchInventoryBaleList(getWHId())
        }.asLiveData()
    }

    fun chageBaleStatus(changeBaleStatus: ChangeBaleStatus): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall() = api.changeBaleStatus(changeBaleStatus)
        }.asLiveData()
    }

    fun getInventoryGrades(): LiveData<Resource<GenericResponse<List<BaleGrade>>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<List<BaleGrade>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<List<BaleGrade>>>> {
                return api.getInventoryGrades(getWHId())
            }
        }.asLiveData()
    }

    fun syncBaleByGrade(grade: String): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall() = api.syncBaleByGrade(getWHId(), grade)
        }.asLiveData()
    }
}*/
