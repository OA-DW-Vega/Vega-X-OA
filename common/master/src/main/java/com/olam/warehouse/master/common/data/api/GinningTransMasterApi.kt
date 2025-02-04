package com.olam.warehouse.master.common.data.api
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.model.GenericScanDetails
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 3/26/2020.
 */
interface GinningTransMasterApi {


    @GET("x-ginning/warehouse/getDeliveryDetails")
    fun fetchDeliveryDetails(@Query("key") key: String): Call<GenericReqAndResp<List<VegaCottonGinningDispatchDelivery>>>

    @GET("x-ginning/warehouse/getBaleDetails")
    fun fetchInventoryBaleList(@Query("key") warehouseId: String): Call<GenericReqAndResp<List<Bale>>>

    @GET("x-ginning/warehouse/dataSync")
    fun dataSync(@Query("key") key: String): Call<GenericReqAndResp<GenericMessage>>

    @GET("x-ginning/warehouse/baleDataSync")
    fun baleSync(@Query("key") key: String): Call<GenericReqAndResp<GenericMessage>>

    @GET("x-ginning/warehouse/getScanDetails")
    fun getScanDetails(
        @Query("qrValue") qrValue: String,
        @Query("key") key: String
    ): Call<GenericReqAndResp<GenericScanDetails>>
}
