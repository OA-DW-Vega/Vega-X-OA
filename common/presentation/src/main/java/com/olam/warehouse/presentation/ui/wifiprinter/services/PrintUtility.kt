package com.olam.warehouse.presentation.ui.wifiprinter.services

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.print.PrintJob
import android.print.PrintJobInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mywifiprinter.observers.Observable
import com.example.mywifiprinter.observers.Observer
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_MOBILE
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_PRINTER
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.CONTROLLER_WIFI
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.REQUEST_CODE_PRINTER
import com.olam.warehouse.presentation.ui.wifiprinter.Constants.REQUEST_CODE_WIFI
import com.olam.warehouse.presentation.ui.wifiprinter.ObservableSingleton
import com.olam.warehouse.presentation.ui.wifiprinter.WifiListActivity
import com.olam.warehouse.presentation.ui.wifiprinter.utils.Util
import java.io.File
import java.util.*

/**
 * Created by Baskaran Kannan on 10/15/2020.
 */

class PrintUtility : Observer {
    private var mActivity: Activity
    private var mFragment: Fragment? = null
    private var mPrinterConfiguration: WifiConfiguration? = null
    private var mOldWifiConfiguration: WifiConfiguration? = null
    private var mWifiManager: WifiManager? = null
    private var mWifiScanner: WifiScanner? = null
    private var mScanResults: ArrayList<ScanResult> = ArrayList()
    private var mPrintManager: PrintManager? = null
    private var mPrintJobs: List<PrintJob> = emptyList()
    private var mCurrentPrintJob: PrintJob? = null
    private var pdfFile: File? = null
    private var externalStorageDirectory: String? = ""
    private val mPrintStartHandler = Handler()
    private val mPrintCompleteHandler = Handler()
    private val mWifiConnectHandler = Handler()
    private var connectionInfo: String? = null
    private var isMobileDataConnection = false
    private var mPrintCompleteService: PrintCompleteService

    //    Observer pattern
    private var mObservable: Observable? = null

    constructor(mActivity: Activity, mWifiManager: WifiManager, mWifiScanner: WifiScanner) {
        this.mActivity = mActivity
        this.mWifiManager = mWifiManager
        this.mWifiScanner = mWifiScanner
        mPrintCompleteService = mActivity as PrintCompleteService
        mObservable = ObservableSingleton.getInstance()
        mObservable?.attach(this)
    }

    constructor(mActivity: Activity, mFragment: Fragment, mWifiManager: WifiManager, mWifiScanner: WifiScanner) {
        this.mActivity = mActivity
        this.mFragment = mFragment
        this.mWifiManager = mWifiManager
        this.mWifiScanner = mWifiScanner
        mPrintCompleteService = mFragment as PrintCompleteService
        mObservable = ObservableSingleton.getInstance()
        mObservable?.attach(this)
    }

    fun print(pdfFile: File?) {
        this.pdfFile = pdfFile

        // check connectivity info -> mobile or wifi.
        connectionInfo = Util.connectionInfo(mActivity)
        if (connectionInfo?.equals(CONTROLLER_MOBILE, true) == true) {
            // follow mobile flow.
            isMobileDataConnection = true
            if (mWifiManager?.isWifiEnabled == false) {
                mWifiManager?.isWifiEnabled = true
            }
            mWifiManager?.startScan()
            mWifiScanner?.getScanResults()?.let { setScanResults(it) }
            printerConfiguration()
        } else if (connectionInfo?.equals(CONTROLLER_WIFI, true) == true) {
            // follow wifi flow..

            // this will get current wifiInfo and store it in shared preference.
            Util.storeCurrentWiFiConfiguration(mActivity)
            printerConfiguration()
        } else {
            mObservable?.notifyObserver(true)
        }
    }

    private fun printerConfiguration() {

        // check printer detail is available or not.
        mPrinterConfiguration = Util.getWifiConfiguration(mActivity, CONTROLLER_PRINTER)
        if (mPrinterConfiguration == null) {
            // printer configuration is not available.
            // display list of wifi available in an activity
            showWifiListActivity(REQUEST_CODE_PRINTER)
        } else {
            // get list of wifi available. if printer configuration available then connect it.
            // else.. show list of available wifi nearby.
            var isPrinterAvailable = false

            // scans nearby wifi..
            mWifiManager?.startScan()
            mWifiScanner?.getScanResults()?.let { setScanResults(it) }


            // checks this wifi in scan result list..
            for (i in mScanResults) {
                if (mPrinterConfiguration?.SSID == "\"" + i.SSID + "\"") {
                    isPrinterAvailable = true
                    break
                }
            }
            if (isPrinterAvailable) {

                // connect to printer wifi and show print settings dialog and continue with print flow.
                connectToWifi(mPrinterConfiguration)

                // prints document.
                doPrint()
            } else {
                showWifiListActivity(REQUEST_CODE_PRINTER)
            }
        }
    }

    private fun showWifiListActivity(requestCode: Int) {
        val iWifi = Intent(mActivity, WifiListActivity::class.java)
        mActivity.startActivityForResult(iWifi, requestCode)
    }

    private fun connectToWifi(mWifiConfiguration: WifiConfiguration?) {
        mWifiConfiguration?.networkId?.let { mWifiManager?.enableNetwork(it, true) }
    }

    fun doPrint() {
        try {
            // it is taking some time to connect to printer.. so i used handler.. and waiting for its status.
            mPrintStartHandler.postDelayed(object : Runnable {
                override fun run() {
                    mPrintStartHandler.postDelayed(this, TIME_OUT.toLong())
                    if (mPrinterConfiguration?.status == WifiConfiguration.Status.ENABLED) {
                        if (mWifiManager?.connectionInfo?.supplicantState == SupplicantState.COMPLETED) {
                            try {
                                if (Util.computePDFPageCount(pdfFile) > 0) {
                                    printDocument(pdfFile)
                                } else {
                                    val alert = AlertDialog.Builder(mActivity)
                                    alert.setMessage("Can't print, Page count is zero.")
                                    alert.setNeutralButton("OK") { dialog, i ->
                                        dialog.dismiss()
                                        switchConnection()
                                    }
                                    alert.show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        mPrintStartHandler.removeCallbacksAndMessages(null)
                    } else {
                        Toast.makeText(
                            mActivity,
                            "Failed to connect to printer!.",
                            Toast.LENGTH_LONG
                        ).show()
                        switchConnection()
                        mPrintStartHandler.removeCallbacksAndMessages(null)
                    }
                }
            }, TIME_OUT.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(mActivity, "Failed to connect to printer!.", Toast.LENGTH_LONG).show()
            switchConnection()
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun printDocument(pdfFile: File?) {
        mPrintManager = mActivity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = mActivity.resources.getString(R.string.app_name) + " Document"
        mCurrentPrintJob = mPrintManager?.print(jobName, PrintServicesAdapter(mActivity, mFragment, pdfFile!!), null)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun completePrintJob() {
        mPrintJobs = mPrintManager!!.printJobs
        mPrintCompleteHandler.postDelayed(object : Runnable {
            override fun run() {
                mPrintCompleteHandler.postDelayed(this, CONNECTION_TIME_OUT.toLong())
                if (mCurrentPrintJob!!.info.state == PrintJobInfo.STATE_COMPLETED) {

                    // remove that PrintJob from PrintManager.
                    for (i in mPrintJobs) {
                        if (i.id === mCurrentPrintJob!!.id) {
                            mPrintJobs.toMutableList().removeAt(mPrintJobs.lastIndexOf(i))
                        }
                    }

                    // switching back to previous connection..
                    switchConnection()

                    // stops handler..
                    mPrintCompleteHandler.removeCallbacksAndMessages(null)
                } else if (mCurrentPrintJob!!.info.state == PrintJobInfo.STATE_FAILED) {
                    switchConnection()
                    Toast.makeText(mActivity, "Print Failed!", Toast.LENGTH_LONG).show()
                    mPrintCompleteHandler.removeCallbacksAndMessages(null)
                } else if (mCurrentPrintJob!!.info.state == PrintJobInfo.STATE_CANCELED) {
                    switchConnection()
                    Toast.makeText(mActivity, "Print Cancelled!", Toast.LENGTH_LONG).show()
                    mPrintCompleteHandler.removeCallbacksAndMessages(null)
                }
            }
        }, CONNECTION_TIME_OUT.toLong())
    }

    fun switchConnection() {
        try {
            if (!isMobileDataConnection) {
                mOldWifiConfiguration = Util.getWifiConfiguration(mActivity, CONTROLLER_WIFI)

                // get list of wifi available. if wifi configuration available then connect it.
                // else.. show list of available wifi nearby.
                var isWifiAvailable = false

                // scans nearby wifi.
                mWifiManager?.startScan()
                mWifiScanner?.getScanResults()?.let { setScanResults(it) }

                // checks this wifi in scan result list.
                for (i in mScanResults) {
                    if (mOldWifiConfiguration!!.SSID == "\"" + i.SSID + "\"") {
                        isWifiAvailable = true
                        break
                    }
                }
                if (isWifiAvailable) {

                    // connect to printer wifi and show print settings dialog and continue with print flow.
                    connectToWifi(mOldWifiConfiguration)
                    mWifiConnectHandler.postDelayed(object : Runnable {
                        override fun run() {
                            mWifiConnectHandler.postDelayed(this, TIME_OUT.toLong())
                            if (mOldWifiConfiguration!!.status == WifiConfiguration.Status.CURRENT) {
                                if (mWifiManager?.connectionInfo?.supplicantState == SupplicantState.COMPLETED) {
                                    try {
                                        mObservable?.notifyObserver(true)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    mWifiConnectHandler.removeCallbacksAndMessages(null)
                                }
                            }
                        }
                    }, TIME_OUT.toLong())
                } else {
                    showWifiListActivity(REQUEST_CODE_WIFI)
                }
            } else {
                mWifiManager?.isWifiEnabled = false
                mObservable?.notifyObserver(true)
            }
        } catch (e: Exception) {
            mObservable?.notifyObserver(true)
            e.printStackTrace()
        }
    }

    fun getPrinterConfigAndPrint() {
        mPrinterConfiguration = Util.getWifiConfiguration(mActivity, CONTROLLER_PRINTER)
        doPrint()
    }

    fun setScanResults(scanResults: List<ScanResult>) {
        mScanResults = scanResults as ArrayList<ScanResult>
    }

    fun onPrintCancelled() {
        switchConnection()
    }

    override fun update() {
        mObservable?.detach(this)
    }

    override fun updateObserver(bool: Boolean) {}
    override fun updateObserverProgress(percentage: Int) {}

    companion object {
        private const val TIME_OUT = 10000
        private const val CONNECTION_TIME_OUT = 5000
    }
}
