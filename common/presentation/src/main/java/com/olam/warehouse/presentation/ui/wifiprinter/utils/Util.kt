package com.olam.warehouse.presentation.ui.wifiprinter.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.RandomAccessFileOrArray
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_MOBILE
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_PRINTER
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_PRINTER_CONFIGURATION
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_WIFI
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_WIFI_CONFIGURATION
import com.olam.warehouse.presentation.utils.PreferenceHelper
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.reflect.Type

/**
 * Created by Baskaran Kannan on 10/15/2020.
 */
object Util {
    fun connectionInfo(mActivity: Activity): String {
        var result = "not connected"
        val cm = mActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals(CONTROLLER_WIFI, true)) {
                if (ni.isConnected) {
                    result = CONTROLLER_WIFI
                    break
                }
            } else if (ni.typeName.equals(CONTROLLER_MOBILE, true)) {
                if (ni.isConnected) {
                    result = CONTROLLER_MOBILE
                    break
                }
            }
        }
        return result
    }

    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiNetwork != null && wifiNetwork.isConnectedOrConnecting) {
            return true
        }
        val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mobileNetwork != null && mobileNetwork.isConnectedOrConnecting) {
            return true
        }
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    fun saveWifiConfiguration(mActivity: Activity, mWifiConfiguration: WifiConfiguration?) {
        val mGson = Gson()
        val mType: Type = object : TypeToken<WifiConfiguration?>() {}.type
        val sJson: String = mGson.toJson(mWifiConfiguration, mType)
        PreferenceHelper.save(CONTROLLER_WIFI_CONFIGURATION, sJson)
    }

    fun savePrinterConfiguration(mActivity: Activity, mPrinterConfiguration: WifiConfiguration?) {
        val mGson = Gson()
        val mType: Type = object : TypeToken<WifiConfiguration?>() {}.type
        val sJson: String = mGson.toJson(mPrinterConfiguration, mType)
        PreferenceHelper.save(CONTROLLER_PRINTER_CONFIGURATION, sJson)
    }

    fun getWifiConfiguration(mActivity: Activity, configurationType: String): WifiConfiguration? {
        var mWifiConfiguration: WifiConfiguration? = WifiConfiguration()
        val mGson = Gson()
        val mWifiConfigurationType: Type = object : TypeToken<WifiConfiguration?>() {}.type
        var mWifiJson: String? = ""
        if (configurationType.equals(CONTROLLER_WIFI, true)) {
            mWifiJson = PreferenceHelper.get(CONTROLLER_WIFI_CONFIGURATION, "")
        } else if (configurationType.equals(CONTROLLER_PRINTER, true)) {
            mWifiJson = PreferenceHelper.get(CONTROLLER_PRINTER_CONFIGURATION, "")
        }
        mWifiConfiguration = if (mWifiJson?.isNotEmpty() == true) {
            mGson.fromJson(mWifiJson, mWifiConfigurationType)
        } else {
            null
        }
        return mWifiConfiguration
    }

    fun storeCurrentWiFiConfiguration(mActivity: Activity) {
        try {
            val wifiManager = mActivity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {
                val mWifiConfiguration = WifiConfiguration()
                mWifiConfiguration.networkId = connectionInfo.networkId
                mWifiConfiguration.BSSID = connectionInfo.bssid
                mWifiConfiguration.hiddenSSID = connectionInfo.hiddenSSID
                mWifiConfiguration.SSID = connectionInfo.ssid

                // store it for future use -> after print is complete you need to reconnect wifi to this network.
                saveWifiConfiguration(mActivity, mWifiConfiguration)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun computePDFPageCount(file: File?): Int {
        var raf: RandomAccessFile? = null
        var pages = 0
        try {
            raf = RandomAccessFile(file, "r")
            val pdfFile = RandomAccessFileOrArray(
                RandomAccessSourceFactory().createSource(raf)
            )
            val reader = PdfReader(pdfFile, ByteArray(0))
            pages = reader.numberOfPages
            reader.close()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return pages
    }
}
