package com.olam.warehouse.presentation.data.remote.interceptor

import com.olam.warehouse.presentation.data.api.DoAuthApi
import com.olam.warehouse.presentation.data.domain.model.DoAuth
import com.olam.warehouse.presentation.utils.Constants.AUTHORIZATION
import com.olam.warehouse.presentation.utils.Constants.CONTENT_TYPE
import com.olam.warehouse.presentation.utils.Constants.COUNTRY_CODE
import com.olam.warehouse.presentation.utils.Constants.DO_BEARER_TOKEN
import com.olam.warehouse.presentation.utils.Constants.UN_AUTHORIZED
import com.olam.warehouse.presentation.utils.Constants.VEGA_DO_INTERFACE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
class DoAuthInterceptor : Interceptor, KoinComponent {

    val authApi: DoAuthApi by inject()

    override fun intercept(chain: Interceptor.Chain): Response {

        val bearerToken = "bearer ".plus(PreferenceHelper.get(DO_BEARER_TOKEN, ""))

        val request = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, "application/json")
            .addHeader(AUTHORIZATION, bearerToken)
            .build()

        val response = chain.proceed(request)

        if (response.isSuccessful) {
            return response
        } else {
            when (response.code) {
                UN_AUTHORIZED -> {

                    val authResponse = authApi.getAuthToken(getAuthDetail()).execute()

                    if (authResponse.isSuccessful) {
                        val auth = authResponse.body()
                        auth?.accessToken?.let {
                            val authHead = "bearer $it"
                            PreferenceHelper.save(DO_BEARER_TOKEN, it)
                            val retryRequest = chain.request()
                                .newBuilder()
                                .addHeader(CONTENT_TYPE, "application/json")
                                .addHeader(AUTHORIZATION, authHead)
                                .build()
                            val retryResponse = chain.proceed(retryRequest)
                            return if (retryResponse.isSuccessful) {
                                retryResponse
                            } else {
                                retryResponse
                            }
                        }
                        return response
                    } else {
                        return response
                    }
                }
                else -> return response
            }
        }
    }

    private fun getAuthDetail(): DoAuth {
        val auth = DoAuth().copy()
        val interfaceId = PreferenceHelper.get(COUNTRY_CODE, "").plus(VEGA_DO_INTERFACE)
        auth.appInstanceId = interfaceId
        auth.id = interfaceId
        return auth
    }
}
