package com.olam.warehouse.master.common.data.repo

import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper


/**
 * Created by Baskaran Kannan on 17-11-2019.
 */

interface MasterRepository {
    //suspend fun getMasterData(userName: String, warehouseID: String): LiveData<Resource<GenericReqAndResp<List<Master>>>>
    //suspend fun saveMasterData(data: List<Master>)
    //suspend fun updateTempIdToWbId(wbid: String, tempId: String)
   }

class MasterRepositoryImpl(/*private val api: MasterApi,*/ private val dao: MasterDao) :
       MasterRepository {
       val werks = PreferenceHelper.get(Constants.WERKS, "")
    /*override suspend fun saveMasterData(data: List<Master>) {
        dao.saveMasterData(data)
    }*/

    /*override suspend fun getMasterData(
        userName: String,
        warehouseID: String
    ): LiveData<Resource<GenericReqAndResp<List<Master>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<Master>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<Master>> =
                api.fetchMasterData(userName, werks)

        }.build().asLiveData()
    }*/

    //override suspend fun updateTempIdToWbId(wbid: String, tempId: String) = dao.updateTempIdToWbid(wbid, tempId)

}
