package com.olam.warehouse.login.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.PalletLayoutBinding
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.PALLET_COUNT_EDIT
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.BT_MAC
import com.olam.warehouse.presentation.utils.extension.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Baskaran Kannan on 5/28/2020.
 */
class VegaCocoaAddPalletFragment : BaseFragment() {

    override val layoutResourceId = R.layout.pallet_layout
    private lateinit var binding: PalletLayoutBinding
    private var callBack: CallBackPallet? = null
    private var palletCount: Int = 0
    private var palletCount1: String = "0"
    private var palletWeight: String = "0"
    private var palletAvg: Double = 0.0

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private lateinit var mInputStream: InputStream
    private lateinit var mOutputStream: OutputStream
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var mWeight = ""
    private var palletCountEdit = false

    companion object {
        fun newInstance(bundle: Bundle?) = VegaCocoaAddPalletFragment().putArgs {
            putBundle("BUNDLE_DATA", bundle)
        }
    }

    interface CallBackPallet {
        fun updatePalletDetails(noOfPallet: String, palletWeight: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBackPallet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PalletLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        /*initBt()*/
    }

    private fun initUI() {
        if (arguments?.getBundle("BUNDLE_DATA") != null) {
            val bundle = arguments?.getBundle("BUNDLE_DATA")
            palletCountEdit = bundle?.getBoolean(PALLET_COUNT_EDIT) ?: false
            palletCount = bundle?.getInt(Constants.PALLET_ADDED) ?: 0
            palletCount1 = bundle?.getString(Constants.PALLET_COUNT) ?: "0"
            palletWeight = bundle?.getString(Constants.PALLET_WEIGHT) ?: "0"
            val uom = bundle?.getString(Constants.UOM)
            if(uom == "MT"){
                binding.tvWeightUOM.text = uom
                binding.ivAvgPallet.text = uom
            }
            if (palletCount1.isEmpty()) palletCount1 = "0"
            if (palletWeight.isEmpty()) palletWeight = "0"

            palletAvg = palletWeight.toDouble().div(palletCount1.toInt())
            binding.etWeight.setText(
                if (bundle?.getString(Constants.PALLET_WEIGHT)
                        .equals("0")
                ) "" else bundle?.getString(Constants.PALLET_WEIGHT)
            )
            binding.etNoPalletValue.setText(
                if (bundle?.getString(Constants.PALLET_COUNT)
                        .equals("0")
                ) "" else bundle?.getString(Constants.PALLET_COUNT)
            )
            if (binding.etWeight.text?.toString()?.isNotEmpty()!!) binding.tvAvgPalletWtValue.text =
                binding.etWeight.text?.toString()!!.toDouble().div(
                    if (binding.etNoPalletValue.text.toString()
                            .isNotEmpty()
                    ) binding.etNoPalletValue.text.toString().toInt()
                    else 1
                )
                    .formatThreeDigits()
            if (bundle?.getBoolean(Constants.PALLET_EDIT)!!) setEnableDiable(true) else setEnableDiable(false)
            binding.ivPalletWeight.gone()
            binding.ivNoPalletValue.gone()
        } else setEnableDiable(true)

        /* binding.etWeight.onChange {
             binding.etNoPalletValue.isEnabled = it.isNotEmpty()
             binding.tvAvgPalletWtValue.isEnabled = it.isNotEmpty()
         }*/
        binding.etNoPalletValue.onChange {

            when {
                it.isNotEmpty() && binding.etWeight.text?.toString()?.isNotEmpty()!! -> {
                    binding.ivNoPalletValue.visible()
                    if (palletCountEdit) {
                        if (palletCount1.toInt() <= it.toInt()) {
                            binding.etWeight.setText(it.toInt().times(palletAvg).formatThreeDigits())
                            /*binding.tvAvgPalletWtValue.text =
                                (it.toInt().plus(palletCount1.toInt()).times(palletAvg)).div(it.toInt()).formatThreeDigits()*/
                            callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                        } else {
                            binding.etWeight.setText(it.toInt().times(palletAvg).formatThreeDigits())
                            /*binding.tvAvgPalletWtValue.text =
                                (palletCount1.toInt().minus(it.toInt()).times(palletAvg)).div(it.toInt()).formatThreeDigits()*/
                            callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                        }

                    } else {
                        binding.tvAvgPalletWtValue.text =
                            binding.etWeight.text?.toString()!!.toDouble().div(it.toInt()).formatThreeDigits()
                        if (palletCount != 0) {
                            if (palletCount <= it.toInt())
                                callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                            else{
                                binding.etNoPalletValue.error = getString(R.string.enter_max_value)
                                callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                            }
                        } else
                            callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                    }
                }
                else -> {
                    if (!palletCountEdit) {
                        binding.ivNoPalletValue.gone()
                        binding.tvAvgPalletWtValue.text = ""
                        callBack?.updatePalletDetails(it, binding.etWeight.text?.toString()!!)
                    }
                }
            }
        }

        binding.etWeight.onChange {
            when {
                it.isNotEmpty() -> {
                    when {
                        binding.etNoPalletValue.text?.toString()?.isNotEmpty()==true -> {
                            binding.tvAvgPalletWtValue.text = it.toDouble().div(binding.etNoPalletValue.text?.toString()?.toInt()?:0)
                                    .formatThreeDigits()
                            callBack?.updatePalletDetails(binding.etNoPalletValue.text?.toString()?:"0", it)
                        }
                    }
                    binding.ivPalletWeight.visible()

                }
                else -> {
                    binding.ivPalletWeight.gone()
                    callBack?.updatePalletDetails(it, binding.etNoPalletValue.text?.toString()!!)
                }
            }
        }

        binding.ivPalletWeight.setOnClickListener {
            binding.etWeight.setText("")
            binding.ivPalletWeight.gone()
            binding.tvAvgPalletWtValue.text = ""
        }

        binding.ivNoPalletValue.setOnClickListener {
            binding.etNoPalletValue.setText("")
            binding.ivNoPalletValue.gone()
        }

        binding.tvPullWeight.setOnClickListener {
            binding.etWeight.error = null
            mWeight = PreferenceHelper.get("BTVALUE", "")
            if (mWeight.isNotEmpty())
                binding.etWeight.setText(mWeight)
            PreferenceHelper.save("BTVALUE", "")
            if (binding.etWeight.text.isNullOrEmpty())
                //context?.sendBroadcast(Intent(Constants.IS_BT_CONNECTED))
                context?.sendBroadcast(
                    Intent(Constants.IS_BT_CONNECTED).apply {
                        setPackage(context?.packageName)
                    }
                )

            //binding.etWeight.isEnabled = !mBTAdapter.isEnabled
        }
    }

    private fun setEnableDiable(flag: Boolean) {
        binding.etWeight.isEnabled = flag
        binding.etNoPalletValue.isEnabled = flag
        binding.tvPullWeight.isEnabled = flag
        if (flag) {
            ViewCompat.setBackgroundTintList(
                binding.tvPullWeight,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.dark_marun
                    )
                }
            )
            binding.tvPullWeight.setTextColor(
                ContextCompat.getColor(
                    binding.tvPullWeight.context,
                    com.olam.warehouse.presentation.R.color.white
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvPullWeight.compoundDrawableTintList = context?.let { it1 ->
                    ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.white)
                }?.let { it2 -> ColorStateList.valueOf(it2) }
            }
        } else {
            if (palletCountEdit)
                binding.etNoPalletValue.isEnabled = palletCountEdit

            ViewCompat.setBackgroundTintList(
                binding.tvPullWeight,
                context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
            )

            binding.tvPullWeight.setTextColor(
                ContextCompat.getColor(
                    binding.tvPullWeight.context,
                    com.olam.warehouse.presentation.R.color.lightGrey
                )
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvPullWeight.compoundDrawableTintList = context?.let { it1 ->
                    ContextCompat.getColor(it1, com.olam.warehouse.presentation.R.color.lightGrey)
                }?.let { it2 -> ColorStateList.valueOf(it2) }
            }
        }
    }

    /*private fun initBt() {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        when {
            mBTAdapter.isEnabled -> getPairedDevices()
            else -> startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //getPairedDevices()
        }
    }

  /*  @SuppressLint("MissingPermission")
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
                        activity?.toast("Weighscale connected successfully")
                        //}
                        try {
                            DoAsync {
                                mInputStream = it.inputStream
                                var data = ""
                                var bytes: Int
                                while (true) {
                                    try {
                                        val buffer = ByteArray(1024)
                                        *//*bytes = *//*mInputStream.read(buffer, 0, buffer.size)
                                        data = String(buffer, 0, buffer.size, Charsets.UTF_8)
                                        val dataArr = data.split("\r\n", "\r", "\n", " ")
//                                            runOnUiThread { toast("WeightTest1: $data") }
                                        dataArr.forEach {
                                            data = Regex("[^0-9.]").replace(it, "")
                                            mWeight = data
                                            try {
                                                if (mWeight.toDouble() > 0) data = ""
                                            } catch (e: java.lang.Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    } catch (e: IOException) {
                                        Log.d("GinngBluetoothReadData", e.message ?: "")
                                        break
                                    }
                                }
                            }.execute()
                        } catch (e: Exception) {
                            Log.d("GinngBluetoothReadData", e.message ?: "")
                        }
                    }

                }
            } catch (e: Exception) {
                Log.d("GinngBluetoothConnect", e.message ?: "")
                mBTSocket?.close()
            }
            //}.execute()
        }
    }

    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(requireContext()).show {
            title(text = getString(R.string.please_select_the_weighing_scale))
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
*/
    override fun onDestroy() {
        super.onDestroy()
        if (mBTSocket != null && mBTSocket?.isConnected == true) {
            mBTSocket?.close()
        }
    }

    fun updateBtWeight(btValue: String) {
        mWeight = btValue
        try {
            if (btValue.toDouble() > 0 && !btValue.equals(binding.etWeight.text)) {
                binding.etWeight.setText(btValue)
                //binding.etWeight.isEnabled = !mBTAdapter.isEnabled
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        PreferenceHelper.save("BTVALUE", btValue)

    }

}
