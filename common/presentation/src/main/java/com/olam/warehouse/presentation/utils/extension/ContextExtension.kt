package com.olam.warehouse.presentation.utils.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat


/**
 * Created by SangiliPandian C on 16-11-2019.
 */

fun getNetworkBandwidth(context: Context): Int {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return 0
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return 0

    return networkCapabilities.linkDownstreamBandwidthKbps
}

fun getWifiBandwidth(context: Context): Int {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return 0
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return 0

    return if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        networkCapabilities.linkDownstreamBandwidthKbps
    } else {
        0
    }
}

fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.isNetworkAvailable(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = manager.activeNetwork
        val capabilities = manager.getNetworkCapabilities(network)
        capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    } else {
        return manager.activeNetworkInfo?.isConnected ?: false
    }
}

fun isInternetAvailable(context: Context): Boolean {
    var result = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
    }

    return result
}

fun Context.networkType(): String {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        manager.activeNetwork
    } else {
        null
    }
    val actNw = manager.getNetworkCapabilities(network) ?: null
    val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    when {
        actNw?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> return "WIFI"
        actNw?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
            return  try{
                when (telephonyManager.networkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN,
                    TelephonyManager.NETWORK_TYPE_GSM
                    -> "2G"
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA
                    -> "3G"
                    TelephonyManager.NETWORK_TYPE_LTE
                    -> "4G"
                    TelephonyManager.NETWORK_TYPE_NR
                    -> "5G"
                    else -> "Unknown"
                }
            } catch (e:SecurityException){
                ""
            }
        }
    }
    return "Unknown"
}

fun Context.networkSpeed(): String {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = manager.activeNetwork
        val nc = manager.getNetworkCapabilities(manager.activeNetwork)
        val capabilities = manager.getNetworkCapabilities(network)
        // DownSpeed in MBPS
        val downSpeed = (nc?.linkDownstreamBandwidthKbps ?: 0) / 1000
        // UpSpeed  in MBPS
        val upSpeed = (nc?.linkUpstreamBandwidthKbps ?: 0) / 1000
        "DownSpeed = ${downSpeed} MBPS-UpSpeed = ${upSpeed} MBPS"

    } else {
        return ""
    }
}

fun Context.networkDownloadSpeed(): String {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = manager.activeNetwork
        val nc = manager.getNetworkCapabilities(manager.activeNetwork)
        val capabilities = manager.getNetworkCapabilities(network)
        // DownSpeed in MBPS
        val downSpeed = (nc?.linkDownstreamBandwidthKbps)
        // UpSpeed  in MBPS
        val upSpeed = (nc?.linkUpstreamBandwidthKbps ?: 0) / 1000
        "${downSpeed} kbps"

    } else {
        return ""
    }
}

fun Context.getWifiLevel(): Int {
    val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val info = wifiManager.connectionInfo
    val linkSpeed = wifiManager.connectionInfo.rssi
    val speed = info.linkSpeed
    return/* WifiManager.calculateSignalLevel(linkSpeed, 5)*/ speed
}

fun Context.getColorId(id: Int) = ContextCompat.getColor(this, id)

