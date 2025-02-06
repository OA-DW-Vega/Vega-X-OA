package com.olam.warehouse.vegax.mtntnicaragua.data.api

import com.olam.warehouse.master.common.model.OfflineInventory
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicDispatchLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.mtntnicaragua.data.domain.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Baskaran Kannan on 11/11/2020.
 */
interface VegaNicaraguaMtntApi {

    @GET("x-dispatch/vega/dispatch/getPurchaseOrdersByDispatchPlant")
    suspend fun getPurchaseOrder(@Query("key") key: String): GenericReqAndResp<List<VegaNicPurchaseOrderModel>>

    @GET("x-master/isValidLotIDByMaterials")
    suspend fun getLotDetails(
        @Query("key") key: String,
        @Query("lotID") charge: String,
        @Query("matnr") materialList: List<String>, @Query("werks") whId: String
    ): GenericReqAndResp<List<VegaNicDispatchLots>>

    @GET("x-master/getInventoryByMaterial")
    suspend fun getStockList(
        @Query("key") currentKey: String,
        @Query("materialCode") materialList: String
    ): GenericReqAndResp<OfflineInventory>

    @GET("x-master/getStockByMaterials")
    suspend fun getStockListSap(
        @Query("key") currentKey: String,
        @Query("materialCodes") materialList: ArrayList<String>
    ): GenericReqAndResp<List<VegaCocoaDispatchLots>>

    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
    suspend fun postMtntWeighScaleDeliveryDetails(@Body vegaDeliveryPost: VegaNicMtntDeliveryPost): GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>

    @POST("x-dispatch/vega/dispatch/create-delivery-weighscale")
    fun syncMtntData(@Body vegaNicMtntDeliveryPost: VegaNicMtntDeliveryPost): Call<GenericReqAndResp<List<VegaNicMtntDeliveryDetail>>>

    @GET("x-master/getReprintDetailsForGRN")
    suspend fun getReprintDetailsForMtnt(
        @Query("key") key: String
    ): GenericReqAndResp<List<VegaReceiving>>

    //added for reprint - ticket
    @POST("x-master/inventoryprint/getInventoryForPrintDetails?")
    suspend fun fetchWeighBridgeDetail(
        @Query("key") currentKey: String,
        @Query("materialCode") materialCode: String?
    ): GenericReqAndResp<List<VegaNicTicketListModel>>

    @POST("/x-master/lotSequence")
    suspend fun updateLotSequence(@Body postData: VegaNicaraguaUpdateLotSequencePost): GenericReqAndResp<VegaNicaraguaUpdateLotSequencePost>

    @GET("x-master/getLotQualityDetails")
    fun getQualityParams(
        @Query("key") key: String,
        @Query("charg") charge: String,
        @Query("material") material: String
    ): Call<GenericReqAndResp<List<VegaDispatchLotQuality>>>

    @GET("x-dispatch/reprint/download")
    suspend fun getMTNRPrintRDetails(
        @Query("id") type: Int,
    ):  GenericReqAndResp<String>


    @GET("x-dispatch/reprint/list")
    suspend fun getMTNRreprintList(
        @Query("moduleName") moduleName: String,
        @Query("companyCode") companycode: String
    ):  GenericReqAndResp<List<VegaMtnrReprintList>>

}
