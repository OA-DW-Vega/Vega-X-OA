package com.olam.warehouse.presentation.ui.wifiprinter

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mywifiprinter.observers.Observable
import com.example.mywifiprinter.observers.Observer
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.wifiprinter.utils.Util
import kotlinx.android.synthetic.main.item_wifi_list.view.*
import java.util.*


/**
 * Created by Baskaran Kannan on 10/13/2020.
 */
class WifiListActivity : Activity(), View.OnClickListener, Observer {
    private var mListWifi: RecyclerView? = null
    private var mBtnScan: Button? = null
    private var mWifiManager: WifiManager? = null
    private var mWifiListener: WifiListener? = null
    private var mScanResults: List<ScanResult> = ArrayList()
    private var mObservable: Observable? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_list)
        try {
            mObservable = ObservableSingleton.getInstance()
            mObservable?.attach(this)
            mBtnScan = findViewById<View>(R.id.btnScan) as Button
            mBtnScan?.setOnClickListener(this)
            mListWifi = findViewById<View>(R.id.rvWifiList) as RecyclerView
            mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (mWifiManager?.isWifiEnabled == false) {
                Toast.makeText(applicationContext, "Enabling WiFi..", Toast.LENGTH_LONG).show()
                mWifiManager?.isWifiEnabled = true
            }
            mWifiListener = WifiListener()
            /* adapter = WifiAdapter(this, mScanResults)
             mListWifi?.adapter = adapter
             mListWifi?.onItemClickListener = OnItemClickListener { adapterView, view, i, l -> connectToWifi(i) }*/
        } catch (e: Exception) {
            mObservable?.notifyObserver(true)
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            registerReceiver(mWifiListener, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            mWifiManager?.startScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(mWifiListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun finishActivity(mWifiConfiguration: WifiConfiguration?, networkId: Int, resultCode: Int) {
        // Don't know how to get class of device -> wifi or printer..
        // for now whatever user clicks assuming that.. It is a printer.. and saving that details in shared Prefs.
        if (resultCode == Constants.RESULT_CODE_PRINTER) {
            mWifiConfiguration!!.networkId = networkId
            //if (networkId != -1) {
            Util.savePrinterConfiguration(this, mWifiConfiguration)
            val intent = Intent()
            setResult(resultCode, intent)
            finish()
            /* } else {
                 Toast.makeText(this, "Failed to connect to wifi", Toast.LENGTH_SHORT).show()
             }*/
        } else if (resultCode == Constants.RESULT_CODE_PRINTER_CONNECT_FAILED) {
            Util.savePrinterConfiguration(this, null)
            val intent = Intent()
            setResult(resultCode, intent)
            finish()
        }
    }

    override fun onClick(view: View?) {
        mWifiManager?.startScan()
        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show()
    }

    private fun connectToWifi(position: Int) {
        try {
            val item = mScanResults[position]
            val Capabilities = item.capabilities

            //Then you could add some code to check for a specific security type.
            if (Capabilities.toString().contains("WPA")) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Password:")

                // Set up the input
                val input = EditText(this)
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                builder.setView(input)

                // Set up the buttons
                builder.setPositiveButton("OK") { dialog, which ->
                    val m_Text = input.text.toString()
                    if (!input.text.toString().trim().isEmpty()) {
                        val wifiConfiguration = WifiConfiguration()
                        wifiConfiguration.SSID = "\"" + item.SSID + "\""
                        wifiConfiguration.preSharedKey = "\"" + m_Text + "\""
                        wifiConfiguration.hiddenSSID = true
                        wifiConfiguration.status = WifiConfiguration.Status.ENABLED
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA) // For WPA
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN) // For WPA2
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                        val res = mWifiManager!!.addNetwork(wifiConfiguration)
                        Log.d("WifiPreference", "add Network returned $res")
                        val b = mWifiManager!!.enableNetwork(res, true)
                        Log.d("WifiPreference", "enableNetwork returned $b")
                        finishActivity(wifiConfiguration, res, Constants.RESULT_CODE_PRINTER)
                    } else {
                        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                builder.show()
            } else if (Capabilities.contains("WEP")) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Title")

                // Set up the input
                val input = EditText(this)
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                builder.setView(input)

                // Set up the buttons
                builder.setPositiveButton("OK") { dialog, which ->
                    val m_Text = input.text.toString()
                    if (!m_Text.isEmpty()) {
                        val wifiConfiguration = WifiConfiguration()
                        wifiConfiguration.SSID = "\"" + item.SSID + "\""
                        wifiConfiguration.wepKeys[0] = "\"" + m_Text + "\""
                        wifiConfiguration.wepTxKeyIndex = 0
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                        val res = mWifiManager!!.addNetwork(wifiConfiguration)
                        Log.d("WifiPreference", "add Network returned $res")
                        val b = mWifiManager!!.enableNetwork(res, true)
                        Log.d("WifiPreference", "enableNetwork returned $b")
                        finishActivity(wifiConfiguration, res, Constants.RESULT_CODE_PRINTER)
                    } else {
                        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                builder.show()
            } else {
                val wifiConfiguration = WifiConfiguration()
                wifiConfiguration.SSID = "\"" + item.SSID + "\""
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                val res = mWifiManager!!.addNetwork(wifiConfiguration)
                Log.d("WifiPreference", "add Network returned $res")
                val b = mWifiManager!!.enableNetwork(res, true)
                Log.d("WifiPreference", "enableNetwork returned $b")
                finishActivity(wifiConfiguration, res, Constants.RESULT_CODE_PRINTER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun update() {
        mObservable?.detach(this)
    }

    override fun updateObserver(bool: Boolean) {}
    override fun updateObserverProgress(percentage: Int) {}
    override fun onBackPressed() {
        try {
            val alert = AlertDialog.Builder(this)
            alert.setMessage("Do you want to cancel print?")
            alert.setPositiveButton("OK") { dialogInterface, i ->
                dialogInterface.dismiss()
                finishActivity(null, -1, Constants.RESULT_CODE_PRINTER_CONNECT_FAILED)
            }
            alert.setNegativeButton("Cancel") { dialogInterface, i -> dialogInterface.dismiss() }
            alert.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class WifiListener : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                mScanResults = mWifiManager?.scanResults ?: emptyList()
                Log.e("scan result size ", "" + mScanResults.size)
                setupAdapter(mScanResults)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupAdapter(mScanResults: List<ScanResult>) {
        mListWifi?.setUp(mScanResults.toMutableList(), R.layout.item_wifi_list, { item, pos ->
            txtWifiName.text = item.SSID
            txtWifiName.setOnClickListener {
                connectToWifi(pos)
            }
        })
    }

}
