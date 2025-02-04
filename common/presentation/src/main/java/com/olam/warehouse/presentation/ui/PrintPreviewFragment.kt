package com.olam.warehouse.login.ui.succes

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BluetoothService
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.REQUEST_ENABLE_BT
import kotlinx.android.synthetic.main.fragment_print_preview.*
import kotlinx.android.synthetic.main.item_print_preview.view.*
import java.io.ByteArrayOutputStream
import kotlin.math.min

class PrintPreviewFragment(bitmapKey: MutableList<String>, label: String) : AppCompatDialogFragment() {

    private var bitmapKey = mutableListOf<String>()
    private var label : String = ""
    private var mChatService: BluetoothService? = null

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bitmapList = ArrayList<Bitmap?>()
    private val progressBar = CustomProgressBar()

    init {
        this.bitmapKey = bitmapKey
        this.label = label
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return activity!!.layoutInflater.inflate(R.layout.fragment_print_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(label == "grn")
            btPrint.text = getString(R.string.print_grn_document)
        else if(label == "receipt")
            btPrint.text = getString(R.string.print_wh_receipt)
        dialog?.window?.setWindowAnimations(com.olam.warehouse.presentation.R.style.DialogAnimation)
        dialog?.setCanceledOnTouchOutside(false)
        ivClose.setOnClickListener {
            dismiss()
        }
        btPrint.setOnClickListener { printerModule() }
        setupAdapter()
    }

    private fun setupAdapter() {
        rvPreviewList.setUp(bitmapKey, R.layout.item_print_preview, { item, pos ->
            ivPreview.setImageBitmap(setScaledBitmap(bitmapKey[pos]))
        })
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onResume() {
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
        super.onResume()
    }

    private fun setScaledBitmap(imagePath: String): Bitmap? {
        try {
            val imageViewWidth = 347
            val imageViewHeight = 413

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = min(a = bitmapWidth / imageViewWidth, b = bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            val decodedString: ByteArray = Base64.decode(imagePath, Base64.DEFAULT)
            val decodedByte =
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, bmOptions)
            return decodedByte
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun printerModule() {
        initBt()
    }

    private fun initBt() {

        // Initialize the BluetoothService to perform bluetooth connections
        for (i in bitmapKey) {
            bitmapList.add(setScaledBitmap(i))
        }

        when {
            mBTAdapter.isEnabled -> {
                getPairedDevices()
                startChatService()
            }
            else -> startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT
            )
        }
    }

    fun startChatService() {
        mChatService = BluetoothService(mHandler!!)
        if (mChatService != null) {
            if (mChatService?.mState == Constants.STATE_NONE) {
                mChatService?.start()
            }
        }
    }

    private fun getPairedDevices() {
        val pairedMac = PreferenceHelper.get(UIUtils.BT_MAC, "")
        if (pairedMac.isBlank()) {
            showPairedDeviceDialog()
        } else {
            showLoading()
            DoAsync {
                try {
                    val device = mBTAdapter.getRemoteDevice(pairedMac)
                    mBTSocket = device.createRfcommSocketToServiceRecord(mUUID)
                    mBTSocket?.let {
                        if (it.isConnected) it.close()
                        mChatService!!.connect(device, true)
                    }
                } catch (e: Exception) {
                    Log.d("SuccessActivity", e.message ?: "")
                    hideLoading()
                    mBTSocket?.close()
                }
            }.execute()
        }
    }

    private fun showPairedDeviceDialog() {
        mPairedDevices.addAll(mBTAdapter.bondedDevices)
        val pairedDeviceName = mBTAdapter.bondedDevices.map { it.name }
        MaterialDialog(activity!!).show {
            title(text = getString(R.string.please_select_the_printer))
            listItemsSingleChoice(items = pairedDeviceName) { dialog, index, text ->
                if (mPairedDevices.isNotEmpty()) {
                    PreferenceHelper.save(UIUtils.BT_MAC, mPairedDevices[index].address)
                    getPairedDevices()
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok))) {
                dismiss()
            }
        }
    }


    private fun sendImage(bitmap: Bitmap) {
        if (mChatService!!.mState !== Constants.STATE_CONNECTED) {
            Toast.makeText(activity, getString(R.string.not_connected_with_device), Toast.LENGTH_SHORT).show()
            return
        }

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos) //bm is the bitmap object
        val b = baos.toByteArray()
        Toast.makeText(activity, b.size.toString(), Toast.LENGTH_SHORT).show()
        mChatService!!.write(b)
    }


    override fun onDestroy() {
        super.onDestroy()
        mChatService?.stop()
    }


    private var mHandler: Handler? = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    Constants.STATE_CONNECTED -> {
                        hideLoading()
                        for (i in bitmapList) {
                            sendImage(i!!)
                        }
                    }

                    Constants.STATE_NONE -> {

                    }
                }
                Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                }
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                }
                Constants.MESSAGE_DEVICE_NAME -> {
                    // save the connected device's name
                    hideLoading()
                    val mConnectedDeviceName = msg.data.getString(Settings.Global.DEVICE_NAME)
                    if (null != activity) {
                        Toast.makeText(
                            activity, getString(R.string.connected_to)
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT
                        ).show()
                        hideLoading()
                    }
                }
                Constants.MESSAGE_TOAST -> if (null != activity) {
                    Toast.makeText(
                        activity, msg.data.getString(Constants.TOAST),
                        Toast.LENGTH_SHORT
                    ).show()
                    hideLoading()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getPairedDevices()
            startChatService()
        }
    }

    fun showLoading() {
        when {
            progressBar.dialog == null -> progressBar.showLoading(requireContext())
            !progressBar.dialog!!.isShowing -> progressBar.showLoading(requireContext())
        }
    }

    fun hideLoading() = progressBar.dialog?.dismiss()
}
