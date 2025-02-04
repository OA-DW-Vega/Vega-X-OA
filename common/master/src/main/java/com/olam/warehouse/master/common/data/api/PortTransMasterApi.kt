package com.olam.warehouse.master.common.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PortTransMasterApi {
    @GET("x-ginning/port/transaction/getScanDetails")
    fun getScanDetails(
        @Query("BaleId") qrValue: String,
        @Query("key") key: String
    ): Call<GenericReqAndResp<PortBale>>

    @GET("x-ginning/port/incomingLot/getPWIncomingMTNList")
    fun getMtnListOffline(@Query("key") key: String): Call<GenericReqAndResp<List<PortMtn>>>
}
