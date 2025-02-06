package com.olam.warehouse.vegax.portwarehouse.data.api

import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import retrofit2.http.*

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
interface DirectDispatchApi {
    @GET("x-ginning/port/dispatch/getOTList")
    suspend fun getOTList(@Query("key") key: String): GenericReqAndResp<List<DispatchOT>>

    @POST("x-ginning/port/dispatch/getOTDetails")
    suspend fun getOTDetails(
        @Query("key") key: String,
        @Query("OTNumber") otNumber: String
    ): GenericReqAndResp<DispatchOT>

    @POST("x-ginning/port/dispatch/saveScannedBale")
    suspend fun getBaleDetail(
        @Query("key") key: String,
        @Query("isDirect") isDirect: Boolean,
        @Body bale: PortBale
    ): GenericReqAndResp<PortBale>

    @POST("x-ginning/port/dispatch/saveScannedContainer")
    suspend fun getContainerDetail(
        @Query("key") key: String,
        @Body container: Container
    ): GenericReqAndResp<Container>

    @POST("x-ginning/port/dispatch/containerWithBaleDetails")
    suspend fun getContainerBaleDetail(
        @Query("key") key: String,
        @Body container: Container
    ): GenericReqAndResp<Container>

    @DELETE("x-ginning/port/dispatch/deleteBale")
    suspend fun deleteBaleDetail(
        @Query("key") key: String,
        @Query("BaleId") baleId: String
    ): GenericReqAndResp<GenericMessage>

    @DELETE("x-ginning/port/dispatch/deleteContainer")
    suspend fun deleteContainer(
        @Query("key") key: String,
        @Query("ContainerNumber") containerNumber: String,
        @Query("OTNumber") otNumber: String
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/holdAndResumeContainer")
    suspend fun holdStuffing(
        @Query("key") key: String,
        @Query("ContainerNumber") ContainerNumber: String,
        @Query("ContainerStatus") status: Int,
        @Query("OTNumber") otNumber: String
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/sealContainer")
    suspend fun sealContainer(
        @Query("key") key: String,
        @Query("SealNumber") SealNumber: String, @Query("ContainerNumber") ContainerNumber: String,
        @Query("OTNumber") otNumber: String,
        @Query("isDirect") isDirect: Boolean
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/breakSealContainer")
    fun breakSealContainer(
        @Query("key") key: String,
        @Query("SealNumber") SealNumber: String,
        @Query("ContainerNumber") ContainerNumber: String
    ): GenericReqAndResp<Container>

    @POST("x-ginning/port/dispatch/breakSealContainer")
    suspend fun brakSealContainer(
        @Query("key") key: String,
        @Query("OTNumber") otNumber: String, @Query("ContainerNumber") ContainerNumber: String,
        @Query("SealNumber") SealNumber: String
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/confirmDispatch ")
    suspend fun confirmDispatch(
        @Query("key") key: String,
        @Query("OTNumber") otNumber: String
    ): GenericReqAndResp<GenericMessage>

    @GET("x-ginning/port/dispatch/getMtnInProgress")
    suspend fun getMtnList(@Query("key") key: String): GenericReqAndResp<List<PortMtn>>

    @POST("x-ginning/port/dispatch/updateOtMode")
    suspend fun updateDispatchMode(
        @Query("key") key: String,
        @Query("isDirect") isDirect: Boolean,
        @Query("isNormal") isNormal: Boolean,
        @Query("otNum") otNum: String
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/insertBalesinProgress")
    suspend fun updateDirectDispatchMtn(
        @Query("key") key: String,
        @Body baleList: List<PortMtnBales>
    ): GenericReqAndResp<GenericMessage>

    @POST("x-ginning/port/dispatch/splitContainers")
    suspend fun updateOTNumberDispatch(
        @Query("key") key: String, @Query("otNumber") otNumber: String,
        @Query("oldOtNumber") oldOtNumber: String,
        @Query("cntrDTO") containerNumberList: ArrayList<String>
    ): GenericReqAndResp<SplitContainer>
}
