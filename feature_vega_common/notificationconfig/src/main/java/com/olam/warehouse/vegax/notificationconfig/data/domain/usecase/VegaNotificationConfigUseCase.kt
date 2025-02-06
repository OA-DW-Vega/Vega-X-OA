package com.olam.warehouse.vegax.notificationconfig.data.domain.usecase

import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigPostReqResp
import com.olam.warehouse.vegax.notificationconfig.data.repo.VegaNotificationConfigRepo

class VegaNotificationConfigUseCase(private val repo: VegaNotificationConfigRepo) {

    suspend fun getUserList() = repo.getUserList()

    suspend fun getUserAlreadyModuleConfigList(user: String) = repo.getUserAlreadyModuleConfigDetails(user)

    suspend fun postUserNotifyConfig(req: VegaNotifyConfigPostReqResp) = repo.postUserNofifyConfig(req)

}
