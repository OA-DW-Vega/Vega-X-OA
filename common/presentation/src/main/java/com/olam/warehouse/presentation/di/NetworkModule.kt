package com.olam.warehouse.presentation.di

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.olam.warehouse.presentation.BuildConfig.*
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.data.remote.AppDispatchers
import com.olam.warehouse.presentation.data.remote.interceptor.AuthInterceptor
import com.olam.warehouse.presentation.data.remote.interceptor.DoAuthInterceptor
import com.olam.warehouse.presentation.data.remote.interceptor.TruckManageAuthInterceptor
import com.olam.warehouse.presentation.utils.Constants
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

/**
 * Created by SangiliPandian C on 17-11-2019.
 */

fun initStetho(context: Context) {
    if (DEBUG) {
        Stetho.initializeWithDefaults(context.applicationContext)
    }
}

fun createNetworkModule(context: Context) = module {

    single { Gson() }

    single { GsonConverterFactory.create() }

    single { AppDispatchers(Dispatchers.Main, Dispatchers.IO) }

    single {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        val clientBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                setupCert(context).socketFactory,
                trustAllCerts[0] as X509TrustManager
            )
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor(AuthInterceptor(context))
            .retryOnConnectionFailure(true)
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.DO_CLIENT)) {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor(DoAuthInterceptor())
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.TRUCK_MANAGE_CLIENT)) {

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        val clientBuilder = OkHttpClient.Builder()
            .sslSocketFactory(setupCert(context).socketFactory, trustAllCerts[0] as X509TrustManager)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor(TruckManageAuthInterceptor())
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.APPCENTER_CLIENT)) {
        val clientBuilder = OkHttpClient.Builder()
            /* .addInterceptor(HttpLoggingInterceptor().apply {
                 level = HttpLoggingInterceptor.Level.BODY
             })*/
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
//            .addInterceptor(AuthInterceptor(context))
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.APPCENTER_URL_CLIENT)) {
        val clientBuilder = OkHttpClient.Builder()
            /* .addInterceptor(HttpLoggingInterceptor().apply {
                 level = HttpLoggingInterceptor.Level.BODY
             })*/
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor(AuthInterceptor(context))
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.KEYCLOAK_URL_CLIENT)) {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<java.security.cert.X509Certificate>,
                authType: String
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<java.security.cert.X509Certificate>,
                authType: String
            ) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        val clientBuilder = OkHttpClient.Builder()
            .sslSocketFactory(
                setupCert(context).socketFactory,
                trustAllCerts[0] as X509TrustManager
            )
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MINUTES)
            .addInterceptor(AuthInterceptor(context))
            .retryOnConnectionFailure(true)
        if (DEBUG) {
            clientBuilder.addNetworkInterceptor(StethoInterceptor())
        }
        clientBuilder.build()
    }

    single(named(Constants.BASE)) {
        Retrofit.Builder()
            .client(get())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.BASE_OD)) {
        Retrofit.Builder()
            .client(get())
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.BASE_DO)) {
        Retrofit.Builder()
            .client(get(named(Constants.DO_CLIENT)))
            .baseUrl(BASE_DO_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.TRUCK_MANAGE)) {
        Retrofit.Builder()
            .client(get(named(Constants.TRUCK_MANAGE_CLIENT)))
            .baseUrl(BASE_TRUCK_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.LOCAL)) {
        Retrofit.Builder()
            .client(get())
            .baseUrl(Constants.BASE_LOCAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.APPCENTER)) {
        Retrofit.Builder()
            .client(get(named(Constants.APPCENTER_CLIENT)))
            .baseUrl(Constants.BASE_APPCENTER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single(named(Constants.APPCENTER_URL)) {
        Retrofit.Builder()
            .client(get(named(Constants.APPCENTER_URL_CLIENT)))
            .baseUrl(Constants.BASE_APPCENTER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named(Constants.KEYCLOAK_URL)) {
        Retrofit.Builder()
            .client(get(named(Constants.KEYCLOAK_URL_CLIENT)))
            .baseUrl(BASE_URL_KEY)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}


private fun setupCert(context: Context?): SSLContext {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    })

    val cf: CertificateFactory? = CertificateFactory.getInstance("X.509")
    var ca: Certificate? = null
    // I'm using Java7. If you used Java6 close it manually with finally.
    context?.resources?.openRawResource(R.raw.olamdigital_cert)
        .use { cert -> ca = cf!!.generateCertificate(cert) }

    // Creating a KeyStore containing our trusted CAs
    val keyStoreType = KeyStore.getDefaultType()
    val keyStore = KeyStore.getInstance(keyStoreType)
    keyStore.load(null, null)
    keyStore.setCertificateEntry("ca", ca)

    // Creating a TrustManager that trusts the CAs in our KeyStore.
    val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
    tmf.init(keyStore)

    // Creating an SSLSocketFactory that uses our TrustManager
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    return sslContext
}

