package com.olam.warehouse.presentation.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
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
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.presentation.R
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.databinding.FragmentPrintPreviewBinding
import com.olam.warehouse.presentation.databinding.ItemPrintPreviewBinding
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.REQUEST_ENABLE_BT
import java.io.ByteArrayOutputStream


class PrintPreviewDialogFragment(bitmapKey: MutableList<String>) : AppCompatDialogFragment() {

    private var bitmapKey = mutableListOf<String>()
    private var mChatService: BluetoothService? = null

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bitmapList = ArrayList<Bitmap?>()
    private val progressBar = CustomProgressBar()
    private lateinit var binding: FragmentPrintPreviewBinding

    init {
        this.bitmapKey = bitmapKey
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPrintPreviewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(com.olam.warehouse.presentation.R.style.DialogAnimation)
        dialog?.setCanceledOnTouchOutside(false)
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.btPrint.setOnClickListener { printerModule() }
        setupAdapter()
    }

    private fun setupAdapter() {
        binding.rvPreviewList.setUpAdapter(
            bitmapKey,
            R.layout.item_print_preview,
            ItemPrintPreviewBinding::inflate,
            { item, pos, bindingItem ->
                bindingItem.ivPreview.setImageBitmap(setScaledBitmap(bitmapKey[pos]))
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



    fun setScaledBitmap(
        imagePath: String
    ): Bitmap {
        
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            val decodedString: ByteArray = Base64.decode(imagePath, Base64.DEFAULT)

            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, this)

             val imageViewWidth = 347
             val imageViewHeight = 413

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this,imageViewWidth,imageViewHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size, this)
        }
    }







  /*  private fun setScaledBitmap(imagePath: String): Bitmap? {
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
    }*/

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }




    private fun printerModule() {
        initBt()
    }

    private fun initBt() {

        // Initialize the BluetoothService to perform bluetooth connections
        for (i in bitmapKey) {
            bitmapList.add(setScaledBitmap(i))
        }

        if (checkBluetoothConnectPermission()) {
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
        if (checkBluetoothConnectPermission()) {
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
    }

    private fun showPairedDeviceDialog() {
        if (checkBluetoothConnectPermission()) {
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

    private fun checkBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permission = ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            )
            return if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    1
                )
                false
            } else {
                true
            }
        } else return true
    }
}
