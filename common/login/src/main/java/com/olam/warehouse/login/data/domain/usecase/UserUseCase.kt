package com.olam.warehouse.login.data.domain.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.olam.warehouse.login.data.repository.UserRepository
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.LoginInfo
import com.olam.warehouse.presentation.data.remote.Resource

class UserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(loginInfo: LoginInfo): LiveData<Resource<Auth>> {
        return Transformations.map(repository.getAuthDetail(loginInfo)) {
            it // Place here your specific logic actions (if any)
        }
    }

    suspend fun invokeUser(deviceID: String): LiveData<Resource<User>> {
        return Transformations.map(repository.getUserDetail(deviceID)) {
            it
        }
    }

    suspend fun getUserRoles(currentKey: String) = repository.getUserRoles(currentKey)
    suspend fun getUserRole(currentKey: String) = repository.getUserRole(currentKey)
    suspend fun updateKeyCloakId() = repository.updateKeyCloakId()
}
