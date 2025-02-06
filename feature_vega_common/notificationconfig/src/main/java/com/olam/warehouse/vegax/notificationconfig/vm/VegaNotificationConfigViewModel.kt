package com.olam.warehouse.vegax.notificationconfig.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigPostReqResp
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserModuleConfigurationDetails
import com.olam.warehouse.vegax.notificationconfig.data.domain.usecase.VegaNotificationConfigUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VegaNotificationConfigViewModel(
    private val useCase: VegaNotificationConfigUseCase, private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private var userListSource: LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>> =
        MutableLiveData()
    private val _userList = MediatorLiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>>()
    val userList: LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>> get() = _userList

    private var userAlreadyModuleConfigListSource: LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>> =
        MutableLiveData()
    private val _userAlreadyModuleConfigList =
        MediatorLiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>>()
    val userAlreadyModuleConfigList: LiveData<Resource<GenericReqAndResp<List<VegaNotifyConfigUserModuleConfigurationDetails>>>> get() = _userAlreadyModuleConfigList

    private var userNotifyConfigResponseResource: LiveData<Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>> =
        MutableLiveData()
    private val _userNotifyConfigResponse =
        MediatorLiveData<Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>>()
    val userNotifyConfigResponse: LiveData<Resource<GenericReqAndResp<VegaNotifyConfigPostReqResp>>> get() = _userNotifyConfigResponse

    fun getUserList() {
        viewModelScope.launch(dispatchers.main) {
            _userList.removeSource(userListSource)
            withContext(dispatchers.io) {
                userListSource = useCase.getUserList()
            }
            _userList.addSource(userListSource) {
                _userList.value = it
            }
        }
    }

    fun getUserAlreadyModuleConfigList(userName: String) {
        viewModelScope.launch(dispatchers.main) {
            _userAlreadyModuleConfigList.removeSource(userAlreadyModuleConfigListSource)
            withContext(dispatchers.io) {
                userAlreadyModuleConfigListSource = useCase.getUserAlreadyModuleConfigList(userName)
            }
            _userAlreadyModuleConfigList.addSource(userAlreadyModuleConfigListSource) {
                _userAlreadyModuleConfigList.value = it
            }
        }
    }

    fun postUserNotifyConfig(req: VegaNotifyConfigPostReqResp) {
        viewModelScope.launch(dispatchers.main) {
            _userNotifyConfigResponse.removeSource(userNotifyConfigResponseResource)
            withContext(dispatchers.io) {
                userNotifyConfigResponseResource = useCase.postUserNotifyConfig(req)
            }
            _userNotifyConfigResponse.addSource(userNotifyConfigResponseResource) {
                _userNotifyConfigResponse.value = it
            }
        }
    }
}
