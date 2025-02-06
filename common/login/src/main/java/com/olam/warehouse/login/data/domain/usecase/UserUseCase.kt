package com.olam.warehouse.login.data.domain.usecase

import android.content.Context
import androidx.lifecycle.LiveData
import com.olam.warehouse.login.data.repository.UserRepository
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.LoginInfo
import com.olam.warehouse.presentation.data.remote.Resource

class UserUseCase(private val repository: UserRepository) {
   suspend fun getAuthDetail(loginInfo: LoginInfo) = repository.getAuthDetail(loginInfo)
   suspend fun getUserDetail(deviceID: String, context: Context) = repository.getUserDetail(deviceID, context)

    suspend fun getUserRoles(currentKey: String) = repository.getUserRoles(currentKey)
    suspend fun getUserRole(currentKey: String) = repository.getUserRole(currentKey)
    suspend fun updateKeyCloakId() = repository.updateKeyCloakId()
    suspend fun getFaqAll() = repository.getFaq()

}
