package com.olam.warehouse.login.vm

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.olam.warehouse.login.data.domain.model.FaqModel
import com.olam.warehouse.login.data.domain.model.KeyCloakModel
import com.olam.warehouse.login.data.domain.usecase.UserUseCase
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.LoginInfo
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseViewModel
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val getAuthDetailUseCase: UserUseCase, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private var roles: List<UserRole>? = emptyList()
    private var authSource: LiveData<Resource<Auth>> = MutableLiveData()
    private val _auth = MediatorLiveData<Resource<Auth>>()
    val auth: LiveData<Resource<Auth>> get() = _auth

    private var userSource: LiveData<Resource<User>> = MutableLiveData()
    private val _user = MediatorLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> get() = _user

    private var keyCloakIdSource: LiveData<Resource<GenericReqAndResp<KeyCloakModel>>> = MutableLiveData()
    private val _keyCloakId = MediatorLiveData<Resource<GenericReqAndResp<KeyCloakModel>>>()
    val keyCloakId: LiveData<Resource<GenericReqAndResp<KeyCloakModel>>> get() = _keyCloakId

    private var userRoleSource: LiveData<List<UserRole>> = MutableLiveData()
    private val _userRoles = MediatorLiveData<List<UserRole>>()
    val userRoles: LiveData<List<UserRole>> get() = _userRoles

    private var userRoleSource1: LiveData<List<UserRole>> = MutableLiveData()
    private val _userRoles1 = MediatorLiveData<List<UserRole>>()
    val userRoles1: LiveData<List<UserRole>> get() = _userRoles1

    private var userRoleSource2: LiveData<List<UserRole>> = MutableLiveData()
    private val _userRoles2 = MediatorLiveData<List<UserRole>>()
    val userRoles2: LiveData<List<UserRole>> get() = _userRoles2

    private var faqSource: LiveData<Resource<GenericReqAndResp<List<FaqModel>>>> = MutableLiveData()
    private val _faqData = MediatorLiveData<Resource<GenericReqAndResp<List<FaqModel>>>>()
    val faqData: LiveData<Resource<GenericReqAndResp<List<FaqModel>>>> get() = _faqData


    //faq
    fun getFaq() = viewModelScope.launch(dispatchers.main) {
        _faqData.removeSource(faqSource)
        withContext(dispatchers.io) {
            faqSource = getAuthDetailUseCase.getFaqAll()
        }
        _faqData.addSource(faqSource) {
            _faqData.value = it
        }
    }

    // Auth Details
    fun getAuthDetail(loginInfo: LoginInfo) = viewModelScope.launch(dispatchers.main) {
        _auth.removeSource(authSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            authSource = getAuthDetailUseCase.getAuthDetail(loginInfo)
        }
        _auth.addSource(authSource) {
            _auth.value = it
        }
    }

    fun saveToken(auth: Auth?) {
        auth?.access_token?.let {
            PreferenceHelper.save(Constants.ACCESS_TOKEN, it)
            PreferenceHelper.save(Constants.USER_NAME, "")
            PreferenceHelper.save(Constants.PASS_WORD, "")
        }
        auth?.refresh_token?.let {
            PreferenceHelper.save(Constants.REFRESH_TOKEN, it)
        }
    }

    //User Details
    fun getUserDetail(deviceID: String, context: Context) = viewModelScope.launch(dispatchers.main) {
        _user.removeSource(userSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            userSource = getAuthDetailUseCase.getUserDetail(deviceID, context)
        }
        _user.addSource(userSource) {
            _user.value = it
        }
    }

    fun updateKeyCloakId() = viewModelScope.launch(dispatchers.main) {
        _keyCloakId.removeSource(keyCloakIdSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            keyCloakIdSource = getAuthDetailUseCase.updateKeyCloakId()
        }
        _keyCloakId.addSource(keyCloakIdSource) {
            _keyCloakId.value = it
        }
    }

    fun saveWarehouseId(user: User, passWord: String) {
        val currentUserName = PreferenceHelper.get(Constants.SAME_USER_NAME, "")
        if (!currentUserName.equals(user.username, true)) {
            PreferenceHelper.save(Constants.LOT_SEQUENCE, "")
            PreferenceHelper.save(Constants.GRN_SEQUENCE, "")
            PreferenceHelper.save(Constants.PO_SEQUENCE, "")
            PreferenceHelper.save(Constants.INVOICE_SEQUENCE, "")
        }
        PreferenceHelper.save(Constants.USER_NAME, user.username)
        PreferenceHelper.save(Constants.SAME_USER_NAME, user.username)
        // PreferenceHelper.save(Constants.PASS_WORD, passWord.encrypt())
    }

    fun getRoles(currentKey: String): List<UserRole>? {
        getUserRole(currentKey)
        return roles
    }

    fun getUserRoles(currentKey: String) = viewModelScope.launch(dispatchers.main) {
        _userRoles.removeSource(userRoleSource) // We make sure there is only one source of livedata (allowing us properly refresh)
        withContext(dispatchers.io) {
            userRoleSource = getAuthDetailUseCase.getUserRoles(currentKey)
        }
        _userRoles.addSource(userRoleSource) {
            _userRoles.value = it
        }
    }
    fun getUserRole(currentKey: String?) = viewModelScope.launch(dispatchers.main) {
        withContext(dispatchers.io) {
            currentKey?.let { roles = getAuthDetailUseCase.getUserRole(currentKey).value }
        }
    }


}
