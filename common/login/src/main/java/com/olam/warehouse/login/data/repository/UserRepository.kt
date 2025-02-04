package com.olam.warehouse.login.data.repository

import androidx.lifecycle.LiveData
import com.olam.warehouse.login.BuildConfig.KEY_CLIENT_ID
import com.olam.warehouse.login.BuildConfig.KEY_CLIENT_SECRET
import com.olam.warehouse.login.data.api.UsersApi
import com.olam.warehouse.login.data.domain.model.KeyCloakModel
import com.olam.warehouse.master.user.dao.UserDao
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.domain.model.LoginInfo
import com.olam.warehouse.presentation.data.remote.NetworkBoundResource
import com.olam.warehouse.presentation.data.remote.NetworkOnlyBoundResource
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.SCOPE
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper

interface UserRepository {
    suspend fun getAuthDetail(loginInfo: LoginInfo): LiveData<Resource<Auth>>
    suspend fun getUserDetail(deviceID: String): LiveData<Resource<User>>
    suspend fun getUserRoles(currentKey: String): LiveData<List<UserRole>>
    suspend fun getUserRole(currentKey: String): LiveData<List<UserRole>>
    suspend fun updateKeyCloakId(): LiveData<Resource<GenericReqAndResp<KeyCloakModel>>>
}

class UserRepositoryImpl(private val usersApi: UsersApi, private val authApi: AuthApi, private val dao: UserDao) :
    UserRepository {
    override suspend fun getUserRoles(currentKey: String) = dao.getUserRoles(currentKey)

    override suspend fun getUserRole(currentKey: String) = dao.getUserRoles(currentKey)
    override suspend fun updateKeyCloakId(): LiveData<Resource<GenericReqAndResp<KeyCloakModel>>> {
        return object : NetworkOnlyBoundResource<GenericReqAndResp<KeyCloakModel>>() {
            override suspend fun createCall(): GenericReqAndResp<KeyCloakModel> =
                usersApi.updateKeyCloakId()
        }.build().asLiveData()
    }

    override suspend fun getAuthDetail(loginInfo: LoginInfo): LiveData<Resource<Auth>> {
        //val map: HashMap<String, String> = hashMapOf("username" to loginInfo.username, "password" to loginInfo.password,"grant_type" to loginInfo.grant_type)
        return object : NetworkOnlyBoundResource<Auth>() {
            override suspend fun createCall(): Auth =
                authApi.getAuthDetail(
                    loginInfo.username, loginInfo.password, loginInfo.grant_type,
                    scope = SCOPE,
                    clientId = KEY_CLIENT_ID,
//                    uri = Constants.redirectUri,
                    clientSecret = KEY_CLIENT_SECRET
                )
        }.build().asLiveData()
    }

    override suspend fun getUserDetail(deviceID: String): LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, User>() {

            override fun processResponse(response: User): User = response

            override suspend fun saveCallResults(items: User) {
                dao.save(items)
                val role = items.key.userRoles
                val plantList = arrayListOf<Plant>()
                plantList.add(items.plant)
                val gson = GsonUtils()
//                role.forEach { rol -> list.add(rol.roleName) }
                PreferenceHelper.save(Constants.USER_ROLES, gson.toJson(role))
//                PreferenceHelper.save(Constants.USER_ROLE, role[0].roleName)
                val countryCode = items.plant.countryDetail.countryCode
                PreferenceHelper.save(Constants.WERKS, items.plant.plantId)
                PreferenceHelper.save(Constants.COUNTRY_CODE, countryCode)
                PreferenceHelper.save(Constants.PLANT_DETAILS, gson.toJson(items.plant))
                PreferenceHelper.save(Constants.PLANT_LIST, gson.toJson(plantList))
                if (items.wareHouseLocationCode?.isNotEmpty() == true) {
                    PreferenceHelper.save(Constants.WAREHOUSE_ID, items.wareHouseLocationCode ?: "")
                }
                PreferenceHelper.save(
                    Constants.RECEPTION_TYPES,
                    gson.toJson(items.key.receptionTypes)
                )
                PreferenceHelper.save(Constants.COMPANY_CODE, gson.toJson(items.key.companyCodes))
                PreferenceHelper.save(Constants.PRODUCTS, gson.toJson(items.key.products))
                PreferenceHelper.save(Constants.BACK_OFFICE, gson.toJson(items.key.backOffices))
                PreferenceHelper.save(Constants.KEYS, gson.toJson(items.key.keys))
                PreferenceHelper.save(Constants.QUICK_PIN, items.quickPin ?: "")
                PreferenceHelper.save(Constants.KEYCLOAK_ID, items.keycloakId ?: "")
                PreferenceHelper.save(Constants.IS_SECURITY_PIN, items.securityEnabled ?: false)
                PreferenceHelper.save(Constants.IS_VALID_ENTITY, items.validEntity ?: true)
                val systemPin = items.systemPin ?: false
                when (systemPin) {
                    true -> PreferenceHelper.save(Constants.IS_DEVICE_PIN, true)
                    else -> PreferenceHelper.save(Constants.IS_DEVICE_PIN, false)
                }

                if (role.isNotEmpty()) {
                    role.forEach {
                        dao.insertUserRole(it)
                    }
                }
            }
            override fun shouldFetch(data: User?): Boolean = true

            override suspend fun loadFromDb(): User = dao.getUser()

            override suspend fun createCall(): User =
                usersApi.getUserDetail(
                    deviceID,
                    PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
                )

        }.build().asLiveData()
    }
}
