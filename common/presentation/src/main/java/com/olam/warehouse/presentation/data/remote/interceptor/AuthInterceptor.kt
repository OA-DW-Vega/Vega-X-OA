package com.olam.warehouse.presentation.data.remote.interceptor

/*import com.dynatrace.android.agent.DTXAction
import com.dynatrace.android.agent.Dynatrace*/
//import com.olam.warehouse.presentation.utils.extension.networkSpeed
import android.content.Context
import com.olam.warehouse.presentation.BuildConfig.KEY_CLIENT_ID
import com.olam.warehouse.presentation.BuildConfig.KEY_CLIENT_SECRET
import com.olam.warehouse.presentation.data.api.AuthApi
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ACCEPT
import com.olam.warehouse.presentation.utils.Constants.ACCESS_TOKEN
import com.olam.warehouse.presentation.utils.Constants.AUTHORIZATION
import com.olam.warehouse.presentation.utils.Constants.AppCenterApiKeyName
import com.olam.warehouse.presentation.utils.Constants.AppCenterFullAccessTokenProd
import com.olam.warehouse.presentation.utils.Constants.AppCenterFullAccessTokenUat
import com.olam.warehouse.presentation.utils.Constants.CLIENT_ID
import com.olam.warehouse.presentation.utils.Constants.CLIENT_SECRET
import com.olam.warehouse.presentation.utils.Constants.CL_Fetch_Commodity
import com.olam.warehouse.presentation.utils.Constants.CL_Fetch_OperatingUnit
import com.olam.warehouse.presentation.utils.Constants.CL_Fetch_UserID
import com.olam.warehouse.presentation.utils.Constants.CONTENT_TYPE
import com.olam.warehouse.presentation.utils.Constants.GRANT_TYPE_REFRESH
import com.olam.warehouse.presentation.utils.Constants.LANGUAGE
import com.olam.warehouse.presentation.utils.Constants.REFRESH_TOKEN
import com.olam.warehouse.presentation.utils.Constants.UN_AUTHORIZED
import com.olam.warehouse.presentation.utils.Constants.USER_NAME
import com.olam.warehouse.presentation.utils.Constants.WAREHOUSE_ID
import com.olam.warehouse.presentation.utils.Constants.X_FRAME_OPTIONS
import com.olam.warehouse.presentation.utils.PreferenceHelper
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.KoinComponent
import org.koin.core.error.NoBeanDefFoundException
import org.koin.core.inject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * Created by SangiliPandian C on 17-11-2019.
 */
class AuthInterceptor(context: Context) : Interceptor, KoinComponent {
    var context: Context? = null
//    var res: Response? = null

    init {
        this.context = context
    }

    val authApi: AuthApi? by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val appcenterToken = when (AppUtils.getEnviroment()) {
            "release" -> AppCenterFullAccessTokenProd
            "uat" -> AppCenterFullAccessTokenUat
            else -> AppCenterFullAccessTokenUat
        }

        val warehouseId = PreferenceHelper.get(WAREHOUSE_ID, 0)
        val userName = PreferenceHelper.get(USER_NAME, "")
        val bearerToken = PreferenceHelper.get(ACCESS_TOKEN, "")
        val refreshToken = PreferenceHelper.get(REFRESH_TOKEN, "")
        val language = PreferenceHelper.get(LANGUAGE, "en")
        val currrentKeyOrign = PreferenceHelper.get(Constants.CURRENT_ORIGIN_KEY, "")
        val values = currrentKeyOrign.split("-")
        val unit = if (values.isEmpty()) values[0] else "OFI"
        val comm = if (values.size > 1) values[2] else "OD"

        val request = chain.request()
            .newBuilder()
            .addHeader(CONTENT_TYPE, "application/json")
            .addHeader(ACCEPT, "application/json")
            .addHeader(AUTHORIZATION, getHeader(bearerToken))
            .addHeader(WAREHOUSE_ID, "" + warehouseId)
            .addHeader(USER_NAME, userName)
            .addHeader(CL_Fetch_UserID, userName)
            .addHeader(CL_Fetch_Commodity, comm)
                .addHeader(CL_Fetch_OperatingUnit, unit)
            .addHeader(X_FRAME_OPTIONS, "SAMEORIGIN")
            .addHeader(LANGUAGE, language)
            .addHeader(AppCenterApiKeyName, appcenterToken)
            .build()
//        requestDyanatraceMonitoring(request)
        // context?.let { networkSpeed(it) }
        /*context?.sendBroadcast(Intent(CONNECTIVITY_TYPE))
        when (context?.networkType()) {
            "Unknown" -> return returnErrorResponse(request)
            else -> Log.d(
                AuthInterceptor::class.java.name,
                "Internet Status: ${context?.networkType()}"
            )
        }*/
        val response = chain.proceed(request)
        //  res = response
//        responseDyanatraceMonitoring(res)
        if (response.isSuccessful) {
            return response
        } else {
            when (response.code) {
                UN_AUTHORIZED -> {
                    @Suppress("NAME_SHADOWING")
                    val userName = PreferenceHelper.get(USER_NAME, "")
//                    var passWord = PreferenceHelper.get(PASS_WORD, "")
                    if (!userName.isEmpty()) {
//                        val byte = android.util.Base64.decode(passWord, android.util.Base64.DEFAULT)
//                        passWord = String(byte, StandardCharsets.UTF_8)
//                        PreferenceHelper.save(ACCESS_TOKEN, "")
                        try {
                            val authResponse = authApi?.getAuthToken(
//                            userName = userName,
//                            passWord = passWord,
                                    grantType = GRANT_TYPE_REFRESH,
                                    refreshToken = refreshToken,
                                    clientId = KEY_CLIENT_ID,
//                            uri = redirectUri,
                                    clientSecret = KEY_CLIENT_SECRET
                            )?.execute()

                            if (authResponse?.isSuccessful == true) {
                                val auth = authResponse.body()
                                auth?.access_token?.let {
                                    val authHead = "bearer $it"
                                    PreferenceHelper.save(ACCESS_TOKEN, it)
                                    val retryRequest = chain.request()
                                            .newBuilder()
                                            .addHeader(CONTENT_TYPE, "application/json")
                                            .addHeader(AUTHORIZATION, authHead)
                                            .addHeader(WAREHOUSE_ID, "" + warehouseId)
                                            .addHeader(USER_NAME, userName)
                                            .addHeader(CL_Fetch_UserID, userName)
                                            .addHeader(CL_Fetch_Commodity, comm)
                                            .addHeader(CL_Fetch_OperatingUnit, unit)
                                            .addHeader(X_FRAME_OPTIONS, "SAMEORIGIN")
                                            .build()
                                    response.body?.close()
//                                requestDyanatraceMonitoring(retryRequest)
//                                context?.let { networkSpeed(it) }
                                    val retryResponse = chain.proceed(retryRequest)
//                                responseDyanatraceMonitoring(retryResponse)
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
                        } catch (e: NoBeanDefFoundException) {
                            return response
                        }
                    } else {
                        return response
                    }
                }
                else -> return response
            }
        }
    }

}

private fun getHeader(bearerToken: String): String {
    val header = "Basic " + android.util.Base64.encodeToString(
        CLIENT_ID.plus(":").plus(CLIENT_SECRET).toByteArray(),
        android.util.Base64.DEFAULT
    ).trim()

    return if (bearerToken.isEmpty())
        "" else "bearer ".plus(bearerToken)
}


/*private fun bodyToString(request: Request): String {
    return try {
        val copy = request.newBuilder().build()
        val buffer = Buffer()
        copy.body?.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: IOException) {
        "did not work"
    }
}*/

/*fun requestDyanatraceMonitoring(request: Request) {
    try {
        *//* Dynatrace.modifyUserAction { userAction: ModifiableUserAction ->
             userAction.actionName = "Request Url : ${request.url}"
             userAction.reportValue("Request : ", bodyToString(request))
         }*//*
        val action: DTXAction = Dynatrace.enterAction("${request.url}")
        action.reportValue("Request Payload", bodyToString(request))
        action.leaveAction()

//        Log.d(AuthInterceptor::class.java.name, "RequestResource: ${bodyToString(request)}")
    } catch (e: IOException) {
            e.printStackTrace()
        }
}

fun networkSpeed(context: Context) {
    try {
        val action: DTXAction = Dynatrace.enterAction("Network Speed")
        val netSpeed = context.networkSpeed().split("-")
        if (netSpeed.size > 0) {
            action.reportValue("Download Speed", netSpeed[0])
            action.reportValue("Upload Speed", netSpeed[1])
        }
        action.leaveAction()
//        Log.d(AuthInterceptor::class.java.name, "NetworkSpeed: ${context.networkSpeed()}")
    } catch (e: IOException) {
            e.printStackTrace()
        }
}

fun responseDyanatraceMonitoring(response: Response?) {
    try {
        val resBody: ResponseBody? = response?.peekBody(Long.MAX_VALUE)
        if (response != null) {
            val action: DTXAction = Dynatrace.enterAction("Response Payload")
            action.reportValue("Response Payload", resBody?.string())
            action.leaveAction()
        }
//        Log.d(AuthInterceptor::class.java.name, "ResponseResource: ${resBody?.string()}")
    } catch (e: IOException) {
            e.printStackTrace()
        }
}*/

fun returnErrorResponse(request: Request): Response {
    var msg = "Network not available, check your data connection"
    try {

        return Response.Builder()
            .request(request)
            .code(999)
            .message(msg)
            .body(
                "{${msg}}"
                    .toResponseBody(null)
            ).build()
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        e.printStackTrace()
        when (e) {
            is SocketTimeoutException -> {
                msg = "Timeout - Please check your internet connection"
            }
            is UnknownHostException -> {
                msg = "Unable to make a connection. Please check your internet"
            }
            is IOException -> {
                msg = "Server is unreachable, please try again later."
            }
            is IllegalStateException -> {
                // msg = "${e.message}"
            }
            else -> {
                msg = "${e.message}"
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(999)
            .message(msg)
            .body("{${e.message}}".toResponseBody(null)).build()
    }
}

