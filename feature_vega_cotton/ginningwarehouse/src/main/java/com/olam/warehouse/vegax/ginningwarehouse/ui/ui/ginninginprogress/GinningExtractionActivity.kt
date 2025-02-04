package com.olam.warehouse.ginning.ui.ginninginprogress

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.ginning.data.model.enums.Shift
import com.olam.warehouse.ginning.utils.*
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.Constants.IS_BT
import com.olam.warehouse.presentation.utils.DateUtils.isTimeBetween
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.doAsync
import kotlinx.android.synthetic.main.activity_ginning_extraction.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.IOException
import java.io.InputStream

/**
 * Created by SangiliPandian C on 13-03-2020.
 */
class GinningExtractionActivity : HomeBaseActivity() {

    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private var mInputStream: InputStream? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight: String = ""
    private var isBt = true
    private var isBtDevice = true

    override val layoutResourceId = R.layout.activity_ginning_extraction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initUI()
        initBt()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/ginninginprogress/GinningExtractionActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    @SuppressLint("MissingPermission")
    private fun initBt() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getPairedDevices()
        } else {
            PreferenceHelper.save(IS_BT, false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevices() {
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
                            doAsync{
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
//                                            runOnUiThread { toast("WeightTest1: $data") }
                                        dataArr.forEach {
                                            data = Regex("[^0-9.]").replace(it, "")
                                            mWeight = data
                                            runOnUiThread {
                                                try {
                                                    if (mWeight.toDouble() > 0 && !mWeight.equals(
                                                            etWeight.text
                                                        )
                                                    ) {
                                                        etWeight.setText(mWeight.toString())
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


    @SuppressLint("MissingPermission")
    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(this).show {
            title(text = getString(R.string.please_select_the_weighing_scale))
            cancelOnTouchOutside(false)
            listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                if (mPairedDevices.isNotEmpty()) {
                    PreferenceHelper.save(BT_MAC, mPairedDevices[index].address)
                    getPairedDevices()
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                dismiss()
            }
        }
    }

    private fun initUI() {
        isBt = PreferenceHelper.get(IS_BT, true)
        tvBaleId.text = intent.getStringExtra(BALE_ID) ?: ""
        btnAdd.setOnClickListener { validateAndBale() }
        setPrefValueForShift()
        rgShift.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbShiftA -> saveShiftValueInPref(Shift.A.value)
                R.id.rbShiftB -> saveShiftValueInPref(Shift.B.value)
                R.id.rbShiftC -> saveShiftValueInPref(Shift.C.value)
            }
        }
        btn_pullweight.setOnClickListener {
            etWeight.error = null
            etWeight.setText(mWeight.toString())
        }
    }

    private fun saveShiftValueInPref(value: String) {
        PreferenceHelper.save(SHIFT_VALUE, value)
    }

    private fun validateAndBale() {
        when {
            etWeight.text.isNullOrEmpty() -> setError()
            else -> {
                val weight = etWeight.text.toString().trim().toDouble()
                if (weight in 100.0..350.0) {
                    etWeight.error = null
                    setBaleDetail(weight)
                } else {
                    setError()
                }
            }
        }
    }

    private fun setError() {
        etWeight.error = getString(R.string.enter_weight_between_100_350_kg)
        etWeight.requestFocus()
    }

    private fun setBaleDetail(weight: Double) {
        val bale = Bale(
            baleID = tvBaleId.text.toString(),
            grade = "",
            grossWeight = weight,
            isLastBaleOfShift = cbLastbale.isChecked,
            netWeight = weight,
            shift = PreferenceHelper.get(SHIFT_VALUE, "")
        )
        val intent = Intent(this, GinningInprogressActivity::class.java)
        intent.putExtra(BALE, bale)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setPrefValueForShift() {
        when (val shift = PreferenceHelper.get(SHIFT_VALUE, "")) {
            "" -> when {
                isTimeBetween(PM_11, AM_7) -> {
                    rbShiftC.isChecked = true
                    saveShiftValueInPref(Shift.C.value)
                }
                isTimeBetween(AM_7, PM_3) -> {
                    rbShiftA.isChecked = true
                    saveShiftValueInPref(Shift.A.value)
                }
                else -> {
                    rbShiftB.isChecked = true
                    saveShiftValueInPref(Shift.B.value)
                }
            }
            else -> setShiftValue(shift)
        }
    }

    private fun setShiftValue(shift: String) {
        when (Shift.valueOf(shift)) {
            Shift.A -> rbShiftA.isChecked = true
            Shift.B -> rbShiftB.isChecked = true
            Shift.C -> rbShiftC.isChecked = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mBTSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
