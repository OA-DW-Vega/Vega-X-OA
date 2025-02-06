package com.olam.warehouse.vegax.offloadingindo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.common.VegaCocoaAddPalletFragment
import com.olam.warehouse.login.ui.common.VegaSweepingWeightEntryFragment
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.BT_MAC
import com.olam.warehouse.presentation.utils.UIUtils.REQUEST_ENABLE_BT
import com.olam.warehouse.presentation.utils.extension.replaceFragment
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.offloadingindo.R
import com.olam.warehouse.vegax.offloadingindo.di.injectVegaIndoCoffeeOffloadingFeature
import com.olam.warehouse.vegax.offloadingindo.ui.callback.VegaIndoCoffeeOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingindo.ui.mtnr.VegaIndoCoffeeMtnrConsignmentFragment
import com.olam.warehouse.vegax.offloadingindo.ui.mtnr.VegaIndoCoffeeMtnrWeighScaleSummaryFragment
import com.olam.warehouse.vegax.offloadingindo.ui.supplier.VegaIndoCoffeeSupplierConsignmentFragment
import com.olam.warehouse.vegax.offloadingindo.ui.transaction.VegaIndoCoffeeTransactionDetailsFragment
import com.olam.warehouse.vegax.offloadingindo.utils.*
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeOffloadingActivity : HomeBaseActivity(), VegaIndoCoffeeOffloadReplaceFragmentCallback,
    VegaSweepingWeightEntryFragment.CallBackAddBags, VegaIndoCoffeeSupplierConsignmentFragment.Callback,
    VegaCocoaAddPalletFragment.CallBackPallet, VegaIndoCoffeeTransactionDetailsFragment.CallBack {
    private val mTAG = VegaIndoCoffeeOffloadingActivity::class.java.canonicalName
    override val layoutResourceId = R.layout.activity_indo_coffee_offloading
    private val REQUEST_CODE_ASK_PERMISSIONS = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectVegaIndoCoffeeOffloadingFeature()
        initNavigationView()
        initUI()
    }

    private fun initUI() {
        isBt = PreferenceHelper.get(Constants.IS_BT, true)
        if (intent.hasExtra(UIUtils.TRANS_OFFLOADING))
            displayFragment(VegaIndoCoffeeTransactionDetailsFragment(), false)
        else
            displayFragment(VegaIndoCoffeeOffloadTypeSelectFragment.newInstance(), false)
    }

    private fun displayFragment(fragment: Fragment, flag: Boolean) {
        replaceFragment(
            fragment,
            mTAG,
            allowStateLoss = true,
            containerViewId = R.id.flContainer,
            allowBackStack = flag
        )
    }

    override fun replaceFragment(receivingType: String, data: Any) {
        when (receivingType) {
            /*WEIGHSCALE_LIST -> displayFragment(
                VegaCoffeeOffloadWeighscaleListFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )*/
            MTNR -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(
                    VegaCoffeeReceiving(
                        weighBridgeType = data as String
                    )
                ), true
            )
            SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(
                    VegaCoffeeReceiving(
                        weighBridgeType = data as String
                    )
                ), true
            )
            WEIGHSCALE -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            WEIGHSCALE_SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )

            ADD_WEIGHT -> {
                displayFragment(
                    VegaIndoCoffeeMtnrWeighScalePalletFragment.newInstance(data as VegaCoffeeReceiveLots),
                    true
                )
                initBt()
                /*initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                        when (fragment) {
                            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })*/
            }
            UPDATE_WEIGHT -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaIndoCoffeeMtnrConsignmentFragment -> fragment.updateAddWeight(data as String)
                    //is VegaCoffeeSupplierConsignmentFragment -> fragment.updateAddWeight(data as String)
                }
            }
            FRAG_ADD_BAG_WEIGHT -> {
                val bundle = data as Bundle
                val isLotHide = bundle.getBoolean("isLotHide", false)
                displayFragment(
                    VegaSweepingWeightEntryFragment.newInstance(data, false, isLotHide),
                    true
                )
            }
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaIndoCoffeeMtnrWeighScaleSummaryFragment.newInstance(data as VegaCoffeeReceiving),
                true
            )
            EDIT_LOT -> {
                supportFragmentManager.popBackStackImmediate()
                val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                when (fragment) {
                    is VegaIndoCoffeeMtnrConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                    is VegaIndoCoffeeSupplierConsignmentFragment -> fragment.editLot(data as VegaCoffeeReceiveLots)
                }
            }
        }
    }

    override fun onBackPressed() {
        backNavigation()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    backNavigation()
                    return true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backNavigation() {
        val fragment1 = supportFragmentManager.findFragmentById(R.id.flContainer)
        supportFragmentManager.popBackStackImmediate()
        when (val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)) {
            is VegaIndoCoffeeMtnrWeighScaleSummaryFragment -> {
            }
            is VegaIndoCoffeeTransactionDetailsFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeTransactionDetailsFragment -> super.onBackPressed()
                }
            }
            is VegaIndoCoffeeSupplierConsignmentFragment -> {
                //closeSocket()
                closeBTSocket()
            }
            is VegaIndoCoffeeOffloadTypeSelectFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeOffloadTypeSelectFragment -> super.onBackPressed()
                }

            }
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
            }
            is VegaIndoCoffeeMtnrConsignmentFragment -> {
                when (fragment1) {
                    is VegaIndoCoffeeMtnrWeighScaleSummaryFragment -> fragment.getBack()
                }
            }
            else -> super.onBackPressed()
        }
    }

    /*override fun addedLots(lots: ArrayList<VegaCocoaDispatchLots>) {
        supportFragmentManager.popBackStackImmediate()
        val lotAddFragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (lotAddFragment) {
            is VegaCoffeeMtntWeighBridgeAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
            is VegaCoffeeMtntWeighScaleAddLotFragment -> {
                lotAddFragment.updateLotList(lots)
            }
        }
    }

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaCoffeeMtntWeighScalePalletFragment -> fragment.updateBagWeight(bagMaterial)
        }
    }*/

    override fun updateBagWeight(bagMaterial: VegaCocoaSweepingBagMaterial) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> fragment.updateBagWeight(prepareItem((bagMaterial)))
        }
    }

    override fun updatePalletDetails(noOfPallet: String, palletWeight: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
        when (fragment) {
            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
                fragment.updatePalletDetails(noOfPallet, palletWeight)
            }
        }
    }

    override fun callBack(type: String, receiveLots: VegaCoffeeReceiveLots, receiving: VegaCoffeeReceiving) {
        when (type) {
            ADD_WEIGHT -> {
                displayFragment(
                    VegaIndoCoffeeMtnrWeighScalePalletFragment.newInstance(receiveLots, receiving),
                    true
                )
                initBt()
                /*initBt(object : BtObserve {
                    override fun valueObserve(btValue: String) {
                        val fragment = supportFragmentManager.findFragmentById(R.id.flContainer)
                        when (fragment) {
                            is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
                                val fragment1 = VegaCocoaAddPalletFragment()
                                fragment1.updateBtWeight(btValue)
                            }
                            is VegaSweepingWeightEntryFragment -> {
                                fragment.updateBtWeight(btValue)
                            }
                        }
                    }

                })*/
            }
        }
    }

    override fun replaceFragment(receivingType: String, data: Any, vegaCoffeeReceiving: Any) {
        when (receivingType) {
            MTNR -> displayFragment(
                VegaIndoCoffeeMtnrConsignmentFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving),
                true
            )
            SUPPLIER -> displayFragment(
                VegaIndoCoffeeSupplierConsignmentFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving),
                true
            )
            MTNR_WEIGHSCALE_SUMMARY -> displayFragment(
                VegaIndoCoffeeMtnrWeighScaleSummaryFragment.newInstance(vegaCoffeeReceiving as VegaCoffeeReceiving),
                true
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //closeSocket()
        closeBTSocket()
    }

    fun closeBTSocket() {
        try {
            unregisterReceiver(btConnectReceiver)
            mBTSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Bluetooth

    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight: String = ""
    private var isBt = true
    private var isBtDevice = true

    private fun initBt() {
        registerBtListener1()
        if(checkBluetoothConnectPermission()) {
            mBTAdapter = BluetoothAdapter.getDefaultAdapter()
            when {
                mBTAdapter.isEnabled -> getPairedDevices()
                else -> {
                    if (isBt)
                        startActivityForResult(
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                            REQUEST_ENABLE_BT
                        )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getPairedDevices()
        } else {
            PreferenceHelper.save(Constants.IS_BT, false)
        }
    }


    private fun getPairedDevices() {
        if(checkBluetoothConnectPermission()) {
            val pairedMac = PreferenceHelper.get(BT_MAC, "")
            if (pairedMac.isBlank()) {
                showPairedDeviceDialog()
            } else {
                showLoading()
                // doAsync {
                try {
                    val device = mBTAdapter.getRemoteDevice(pairedMac)
                    mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                    mBTSocket?.let {

                        if (it.isConnected) it.close()
                        it.connect()
                        if (it.isConnected) {
                            //  runOnUiThread {
                            hideLoading()
                            toast("Weighscale connected successfully")
                            //}
                            try {
                                DoAsync {
                                    mInputStream = it.inputStream
                                    var data = ""
                                    var bytes: Int
                                    while (true) {
                                        try {
//                                        val re = Regex("\\d{4}\\.\\d{2}")
//                                        val strArr = arrayOf("\r\n", "\r", "\n", " ")
                                            val buffer = ByteArray(1024)
                                            /*bytes = */mInputStream?.read(buffer, 0, buffer.size)
                                            data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                            val dataArr = data.split("\r\n", "\r", "\n", " ")
                                            var decodeStr = URLDecoder.decode(data, "UTF-8")
                                            runOnUiThread { toast("WeightTest1: $data \n $decodeStr") }
                                            dataArr.forEach {
                                                data = Regex("[^0-9.]").replace(it, "")
                                                mWeight = data
                                                runOnUiThread {
                                                    //toast("WeightTest: $mWeight")
                                                    try {
                                                        if (mWeight.toDouble() > 0) {
                                                            val fragment =
                                                                supportFragmentManager.findFragmentById(
                                                                    R.id.flContainer
                                                                )
                                                            when (fragment) {
                                                                is VegaIndoCoffeeMtnrWeighScalePalletFragment -> {
                                                                    val fragment1 =
                                                                        VegaCocoaAddPalletFragment()
                                                                    fragment1.updateBtWeight(mWeight)
                                                                }
                                                                is VegaSweepingWeightEntryFragment -> {
                                                                    fragment.updateBtWeight(mWeight)
                                                                }
                                                            }
                                                        }

                                                    } catch (e: java.lang.Exception) {
                                                        e.printStackTrace()
                                                    }

                                                }
                                                try {
                                                    if (mWeight.toDouble() > 0) data = ""
                                                } catch (e: java.lang.Exception) {
                                                    e.printStackTrace()
                                                }
                                                //runOnUiThread { toast("Weight1: $mWeight") }
                                            }
                                        } catch (e: IOException) {
                                            Log.d("GinngBluetoothReadData", e.message ?: "")
                                            /*runOnUiThread {
                                            toast("Error1: " + e.message.toString())
                                        }*/
                                            break
                                        }
                                    }
                                }.execute()
                            } catch (e: Exception) {
                                Log.d("GinngBluetoothReadData", e.message ?: "")
                                /*runOnUiThread {
                                toast("Error2: " + e.message.toString())
                            }*/
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.d("GinngBluetoothConnect", e.message ?: "")
                    /*runOnUiThread {
                    toast("Error3: " + e.message.toString())
                }*/
                    mBTSocket?.close()
                    hideLoading()
                }
                //}.execute()
            }
        }
    }

    private fun showPairedDeviceDialog() {
        if(checkBluetoothConnectPermission()) {
            mPairedDevices.addAll(mBTAdapter.bondedDevices)
            val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
            MaterialDialog(this).show {
                title(text = getString(com.olam.warehouse.presentation.R.string.please_select_the_weighing_scale))
                cancelOnTouchOutside(false)
                listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                    if (mPairedDevices.isNotEmpty()) {
                        PreferenceHelper.save(BT_MAC, mPairedDevices[index].address)
                        getPairedDevices()
                    }
                }
                positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok))) {
                    dismiss()
                }
            }
        }
    }

    fun registerBtListener1() {
        if (!btConnectReceiver.isOrderedBroadcast) {
            val intentFilterBt = IntentFilter(Constants.IS_BT_CONNECTED)
            val intentFilterBtSwitch = IntentFilter(Constants.SWITCH_BT_DEVICE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(btConnectReceiver, intentFilterBt, RECEIVER_NOT_EXPORTED )
                registerReceiver(btConnectReceiver, intentFilterBtSwitch, RECEIVER_NOT_EXPORTED)
            }else{
                registerReceiver(btConnectReceiver, intentFilterBt )
                registerReceiver(btConnectReceiver, intentFilterBtSwitch)
            }

        }
    }


    //Bluetooth Connect Receiver
    private val btConnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.IS_BT_CONNECTED -> {
                    try {
                        //if (mBTSocket?.isConnected == true) {
                        mBTSocket?.close()
                        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
                        when {
                            mBTAdapter.isEnabled -> getPairedDevices()
                            else -> {
                                if (checkBluetoothConnectPermission()) {
                                    startActivityForResult(
                                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                        REQUEST_ENABLE_BT
                                    )
                                }
                            }
                        }
                        //}
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
                Constants.SWITCH_BT_DEVICE -> {
                    showPairedDeviceDialog()
                }
            }
        }
    }

    private fun checkBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permission = ActivityCompat.checkSelfPermission(
                this@VegaIndoCoffeeOffloadingActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            )
            return if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
                false
            } else {
                true
            }
        } else return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        "PERMISSION_STATE Denied",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                when (requestCode) {
                    1 -> {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            if ((ContextCompat.checkSelfPermission(
                                    this@VegaIndoCoffeeOffloadingActivity,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED)
                            ) {
                                initBt()
                            }
                        } else {
                            Toast.makeText(this, "PERMISSION_STATE Denied", Toast.LENGTH_LONG)
                                .show()
                        }
                        return
                    }
                }
            }
        }
    }
}
