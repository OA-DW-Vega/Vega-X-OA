package com.olam.warehouse.presentation.data.remote.interceptor

import com.olam.warehouse.presentation.utils.Constants.AUTHORIZATION
import com.olam.warehouse.presentation.utils.Constants.CONTENT_TYPE
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent

/**
 * Created by SangiliPandian C on 17-11-2019.
 */
class TruckManageAuthInterceptor : Interceptor, KoinComponent {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, "application/json")
            .addHeader(AUTHORIZATION, "No Auth")
            .build()

        val response = chain.proceed(request)

        if (response.isSuccessful) {
            return response
        } else
            return response
    }

}
