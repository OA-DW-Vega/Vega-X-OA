package com.olam.warehouse.vegax.notificationconfig.data.api

import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigPostReqResp
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserModuleConfigurationDetails
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VegaNotificationConfigAPI {

    @GET("/x-master/user/getOriginBasedUsers")
    suspend fun getReconReportAuditDetails(
        @Query("key") currentKey: String
    ): GenericReqAndResp<List<VegaNotifyConfigUserDetails>>

    @GET("/x-notification/vega/notifications/getall")
    suspend fun getUserAreadyModuleConfiDetails(
        @Query("userName") currentKey: String
    ): GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>

    @POST("/x-notification/vega/notifications/saveNotificationsConfig")
    suspend fun postUserNotifyConfig(
        @Body request: VegaNotifyConfigPostReqResp
    ): GenericReqAndResp<VegaNotifyConfigPostReqResp>

}
