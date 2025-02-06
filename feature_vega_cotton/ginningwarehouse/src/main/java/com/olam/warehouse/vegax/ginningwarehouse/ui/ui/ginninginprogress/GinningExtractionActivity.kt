package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.Constants.IS_BT
import com.olam.warehouse.presentation.utils.DateUtils.isTimeBetween
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningExtractionBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.enums.Shift
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
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
    private lateinit var binding: ActivityGinningExtractionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningExtractionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initUI()
        initBt()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/ginninginprogress/GinningExtractionActivity")
            .title("Ginningwarehouse").with(tracker)

    }

    private fun initBt() {
        if (checkBluetoothConnectPermission()) {
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
            PreferenceHelper.save(IS_BT, false)
        }
    }


    private fun getPairedDevices() {
        if (checkBluetoothConnectPermission()) {
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
                                doAsync {
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
                                                                binding.etWeight.text
                                                            )
                                                        ) {
                                                            binding.etWeight.setText(mWeight.toString())
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
        if (checkBluetoothConnectPermission()) {
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
    }


    private fun initUI() {
        isBt = PreferenceHelper.get(IS_BT, true)
        binding.tvBaleId.text = intent.getStringExtra(BALE_ID) ?: ""
        binding.btnAdd.setOnClickListener { validateAndBale() }
        setPrefValueForShift()
        binding.rgShift.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbShiftA -> saveShiftValueInPref(Shift.A.value)
                R.id.rbShiftB -> saveShiftValueInPref(Shift.B.value)
                R.id.rbShiftC -> saveShiftValueInPref(Shift.C.value)
            }
        }
        binding.btnPullweight.setOnClickListener {
            binding.etWeight.error = null
            binding.etWeight.setText(mWeight.toString())
            if (binding.etWeight.text.isNullOrEmpty() && mBTAdapter.isEnabled) getPairedDevices()
        }
    }

    private fun saveShiftValueInPref(value: String) {
        PreferenceHelper.save(SHIFT_VALUE, value)
    }

    private fun validateAndBale() {
        when {
            binding.etWeight.text.isNullOrEmpty() -> setError()
            else -> {
                val weight = binding.etWeight.text.toString().trim().toDouble().formatThreeDigits()
                    .toDouble()
                if (weight in 100.0..350.0) {
                    binding.etWeight.error = null
                    setBaleDetail(weight)
                } else {
                    setError()
                }
            }
        }
    }

    private fun setError() {
        binding.etWeight.error = getString(R.string.enter_weight_between_100_350_kg)
        binding.etWeight.requestFocus()
    }

    private fun setBaleDetail(weight: Double) {
        val bale = Bale(
            baleID = binding.tvBaleId.text.toString(),
            grade = "",
            grossWeight = weight,
            isLastBaleOfShift = binding.cbLastbale.isChecked,
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
                    binding.rbShiftC.isChecked = true
                    saveShiftValueInPref(Shift.C.value)
                }
                isTimeBetween(AM_7, PM_3) -> {
                    binding.rbShiftA.isChecked = true
                    saveShiftValueInPref(Shift.A.value)
                }
                else -> {
                    binding.rbShiftB.isChecked = true
                    saveShiftValueInPref(Shift.B.value)
                }
            }
            else -> setShiftValue(shift)
        }
    }

    private fun setShiftValue(shift: String) {
        when (Shift.valueOf(shift)) {
            Shift.A -> binding.rbShiftA.isChecked = true
            Shift.B -> binding.rbShiftB.isChecked = true
            Shift.C -> binding.rbShiftC.isChecked = true
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

    private fun checkBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permission = ActivityCompat.checkSelfPermission(
                this@GinningExtractionActivity,
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

    private val REQUEST_CODE_ASK_PERMISSIONS = 1002


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
                                    this@GinningExtractionActivity,
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
