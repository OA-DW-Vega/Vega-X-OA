package com.olam.warehouse.presentation.data.remote.interceptor

import com.olam.warehouse.presentation.data.api.DoAuthApi
import com.olam.warehouse.presentation.data.domain.model.DoAuth
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.AUTHORIZATION
import com.olam.warehouse.presentation.utils.Constants.CONTENT_TYPE
import com.olam.warehouse.presentation.utils.Constants.COUNTRY_CODE
import com.olam.warehouse.presentation.utils.Constants.DO_BEARER_TOKEN
import com.olam.warehouse.presentation.utils.Constants.UN_AUTHORIZED
import com.olam.warehouse.presentation.utils.Constants.VEGA_DO_INTERFACE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
class DoAuthInterceptor : Interceptor, KoinComponent {

    val authApi: DoAuthApi by inject()

    override fun intercept(chain: Interceptor.Chain): Response {

        val bearerToken = "Bearer ".plus(PreferenceHelper.get(DO_BEARER_TOKEN, ""))

        val warehouseId = PreferenceHelper.get(Constants.WAREHOUSE_ID, 0)
        val userName = PreferenceHelper.get(Constants.USER_NAME, "")
        val refreshToken = PreferenceHelper.get(Constants.REFRESH_TOKEN, "")
        val language = PreferenceHelper.get(Constants.LANGUAGE, "en")
        val currrentKeyOrign = PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
        val values = currrentKeyOrign.split("-")
        val unit = if (values.isNotEmpty()) values[0] else "OFI"
        val origin = if (values.size > 1) values[1] else "ORIGIN"
        val comm = if (values.size > 2) values[2] else "OD"

        val request = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, "application/json")
            .addHeader(Constants.ACCEPT, "application/json")
            .addHeader(AUTHORIZATION, bearerToken)
            .addHeader(Constants.WAREHOUSE_ID, "" + warehouseId)
            .addHeader(Constants.USER_NAME, userName)
            .addHeader(Constants.CL_Fetch_UserID, userName)
            .addHeader(Constants.CL_Fetch_Commodity, comm)
            .addHeader(Constants.CL_Fetch_OperatingUnit, unit)
            .addHeader(Constants.CL_Fetch_Origin, origin)
            .addHeader(Constants.X_FRAME_OPTIONS, "SAMEORIGIN")
            .addHeader(Constants.LANGUAGE, language)
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
                            val authHead = "Bearer $it"
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
