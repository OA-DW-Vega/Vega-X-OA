package com.olam.warehouse.vegax.portwarehouse.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.InventoryBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PilesMaster
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.portwarehouse.data.api.InventoryApi
import java.util.*

interface InventoryRepository
{
    suspend fun  chageBaleStatus(changeBaleStatus: ChangeBaleStatus):LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun getInventoryGrades(selectedPileList: List<String>): LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>>
    suspend fun syncBaleByGrade(grade: String): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
    suspend fun getPileList():LiveData<Resource<GenericReqAndResp<PilesMaster>>>
    suspend fun fetchInventoryBaleList(
        baleNo: String,
        baleTypeList: ArrayList<String>,
        gradeList: ArrayList<String>,
        pageNo: Int,
        pageSize: Int,
        marks: ArrayList<String>,
        years: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<InventoryBale>>>
}
class VegaPortWareHouseInventoryRepositoryImpl(private val api: InventoryApi) :
    InventoryRepository {
    //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
//    val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
    val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

    override suspend fun getInventoryGrades(selectedPileList: List<String>): LiveData<Resource<GenericReqAndResp<List<BaleGrade>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<BaleGrade>>>() {
            override suspend fun createCall() =
                api.getInventoryGrades(getCurrentKey(), selectedPileList)
        }.build().asLiveData()
    }


    override suspend fun fetchInventoryBaleList(
        baleNo: String,
        baleTypeList: ArrayList<String>,
        gradeList: ArrayList<String>,
        pageNo: Int,
        pageSize: Int,
        marks: ArrayList<String>,
        years: ArrayList<String>
    ): LiveData<Resource<GenericReqAndResp<InventoryBale>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<InventoryBale>>() {
            override suspend fun createCall() = api.fetchInventoryBaleList(
                getCurrentKey(),
                baleNo,
                baleTypeList,
                gradeList,
                pageNo,
                pageSize,
                marks,
                years
            )
        }.build().asLiveData()

    }

    override suspend fun syncBaleByGrade(grade: String):LiveData<Resource<GenericReqAndResp<GenericMessage>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() = api.syncBaleByGrade(getCurrentKey(), grade)
        }.build().asLiveData()
    }


    override suspend fun getPileList():LiveData<Resource<GenericReqAndResp<PilesMaster>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<PilesMaster>>() {
            override suspend fun createCall() = api.getPileList(getCurrentKey())
        }.build().asLiveData()
    }

    override suspend fun  chageBaleStatus(changeBaleStatus: ChangeBaleStatus): LiveData<Resource<GenericReqAndResp<GenericMessage>>>{
        return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
            override suspend fun createCall() =
                api.chageBaleStatus(getCurrentKey(), changeBaleStatus)
        }.build().asLiveData()
    }
    /* override suspend fun  postLotForGinning(mIncomingLot: List<IncomingLot>): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
         return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
             override suspend fun createCall() = api.sendLotForGinning(mIncomingLot)
         }.build().asLiveData()
     }*/
}

/*class InventoryRepository constructor(
    private val api: InventoryApi,
    private val appExecutors: AppExecutors
) {

    fun fetchInventoryBaleList(
        baleNo: String,
        baleTypeList: ArrayList<String>,
        gradeList: ArrayList<String>,
        pageNo: Int,
        pageSize: Int,
        marks: ArrayList<String>,
        years: ArrayList<String>
    ): LiveData<Resource<GenericResponse<InventoryBale>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<InventoryBale>>(appExecutors) {
            override fun createCall() = api.fetchInventoryBaleList(
                baleNo,
                baleTypeList,
                gradeList,
                pageNo,
                pageSize,
                marks,
                years
            )
        }.asLiveData()
    }

    fun chageBaleStatus(changeBaleStatus: ChangeBaleStatus): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall() = api.chageBaleStatus(changeBaleStatus)
        }.asLiveData()
    }

    fun getInventoryGrades(selectedPileList: List<String>): LiveData<Resource<GenericResponse<List<BaleGrade>>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<List<BaleGrade>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<List<BaleGrade>>>> {
                return api.getInventoryGrades(selectedPileList)
            }
        }.asLiveData()
    }

    fun syncBaleByGrade(grade:String):LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall() = api.syncBaleByGrade(grade)
        }.asLiveData()
    }

    fun getPileList(): LiveData<Resource<GenericResponse<PilesMaster>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<PilesMaster>>(appExecutors) {
            override fun createCall() = api.getPileList()
        }.asLiveData()
    }
}*/
