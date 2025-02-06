package com.olam.warehouse.vegax.ginningwarehouse.ui.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.api.IncomingLotApi
import java.util.*

/**
 * Created by SangiliPandian C on 05-03-2020.
 */
interface IncomingLotRepository {
    suspend fun fetchingIncomingLots(): LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>>

    suspend fun postLotForGinning(incomingLot: IncomingLot): LiveData<Resource<GenericReqAndResp<GenericMessage>>>
}

   /* fun fetchIncomingLots(): LiveData<Resource<GenericResponse<List<IncomingLot>>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<List<IncomingLot>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<List<IncomingLot>>>> {
                return api.getIncomingLots(getWHId())
            }

        }.asLiveData()
    }

    fun sendLotForGinning(mIncomingLot: IncomingLot): LiveData<Resource<GenericResponse<GenericMessage>>> {
        return object : NetworkOnlyBoundResource<GenericResponse<GenericMessage>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<GenericResponse<GenericMessage>>> {
                return api.sendLotForGinning(mIncomingLot)
            }

        }.asLiveData()
    }
}*/
   class VegaIncomingLotRepositoryImpl(private val api: IncomingLotApi) :
       IncomingLotRepository {
       //val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")

       //       val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
       val currentDate = DateUtils.getDate(Calendar.getInstance().timeInMillis, "yyyyMMdd")

       override suspend fun fetchingIncomingLots(): LiveData<Resource<GenericReqAndResp<List<IncomingLot>>>> {
           return object : NetworkOnlyBoundResource<GenericReqAndResp<List<IncomingLot>>>() {
               override suspend fun createCall() = api.getIncomingLots(getCurrentKey())
           }.build().asLiveData()
       }

       override suspend fun postLotForGinning( incomingLot: IncomingLot): LiveData<Resource<GenericReqAndResp<GenericMessage>>> {
           return object : NetworkOnlyBoundResource<GenericReqAndResp<GenericMessage>>() {
               override suspend fun createCall() =
                   api.sendLotForGinning(getCurrentKey(), incomingLot)
           }.build().asLiveData()
       }
   }
