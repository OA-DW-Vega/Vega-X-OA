package com.olam.warehouse.vegax.notificationconfig.data.repo

import androidx.lifecycle.LiveData
import com.olam.warehouse.master.vega.dao.VegaNotificationConfigDao
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.notificationconfig.data.api.VegaNotificationConfigAPI
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigPostReqResp
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserModuleConfigurationDetails


interface VegaNotificationConfigRepo {
    suspend fun getUserList(): LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>>

    suspend fun getUserAlreadyModuleConfigDetails(user: String): LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>>

    suspend fun postUserNofifyConfig(req: VegaNotifyConfigPostReqResp): LiveData<Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>>

}

class VegaNotificationConfigRepoImpl(
    private val dao: VegaNotificationConfigDao,
    private val api: VegaNotificationConfigAPI
) : VegaNotificationConfigRepo {

    val currentKey = PreferenceHelper.get(Constants.CURRENT_KEY, "")
    override suspend fun getUserList(): LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNotifyConfigUserDetails>> =
                api.getReconReportAuditDetails(currentKey)
        }.build().asLiveData()
    }

    override suspend fun getUserAlreadyModuleConfigDetails(user: String): LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>> {
        return object :
            NetworkOnlyBoundResource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>() {
            override suspend fun createCall(): GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>> =
                api.getUserAreadyModuleConfiDetails(user)
        }.build().asLiveData()
    }

    override suspend fun postUserNofifyConfig(req: VegaNotifyConfigPostReqResp): LiveData<Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>() {
            override suspend fun createCall(): GenericReqAndResp<VegaNotifyConfigPostReqResp> =
                api.postUserNotifyConfig(req)
        }.build().asLiveData()
    }
}
