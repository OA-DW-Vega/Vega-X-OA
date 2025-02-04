package com.olam.warehouse.presentation.ui.wifiprinter.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager

/**
 * Created by Baskaran Kannan on 10/13/2020.
 */

class WifiScanner : BroadcastReceiver() {
    private var mScanResults: List<ScanResult>? = null
    override fun onReceive(context: Context, intent: Intent?) {
        // fetch list of available wifi nearby.
        val mWifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        setScanResults(mWifiManager.scanResults)
    }

    fun getScanResults(): List<ScanResult>? {
        return mScanResults
    }

    fun setScanResults(mScanResults: List<ScanResult>?) {
        this.mScanResults = mScanResults
    }
}
