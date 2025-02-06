package com.olam.warehouse.login.ui.succes

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentPrintPreviewBinding
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BluetoothService
import com.olam.warehouse.presentation.ui.widget.CustomProgressBar
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DoAsync
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.REQUEST_ENABLE_BT
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.visible
import java.io.ByteArrayOutputStream
import kotlin.math.min

class PrintLoCardPreviewDialogFragment(lots: MutableList<VegaCoffeeSalesLots>) : AppCompatDialogFragment() {

    private var lotList = mutableListOf<VegaCoffeeSalesLots>()
    private var mChatService: BluetoothService? = null

    //Bluetooth
    private val mPairedDevices = arrayListOf<BluetoothDevice>()
    private var mBTAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mBTSocket: BluetoothSocket? = null
    private val mUUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bitmapList = ArrayList<Bitmap?>()
    private var bitmapValue = HashMap<Int, Bitmap?>()
    private val progressBar = CustomProgressBar()

    init {
        this.lotList = lots
    }

    private lateinit var binding: FragmentPrintPreviewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPrintPreviewBinding.inflate(layoutInflater)
        dialog?.setCanceledOnTouchOutside(false)
        return binding.root
        // return activity!!.layoutInflater.inflate(R.layout.fragment_print_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(com.olam.warehouse.presentation.R.style.DialogAnimation)
        dialog?.setCanceledOnTouchOutside(true)
        binding.ivClose.setOnClickListener {
            if (dialog != null) dialog?.dismiss()
        }
        binding.btPrint.setOnClickListener { printerModule() }
        binding.btPrint.text = getString(R.string.print_lot_card)
        createBitMap()
        setupAdapter()
    }

    private fun createBitMap() {
        lotList.forEachIndexed { index, item ->
            val parent = LinearLayout(activity)
            parent.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            parent.orientation = LinearLayout.VERTICAL
            parent.background = ContextCompat.getDrawable(parent.context, R.color.white)

            //children of parent linearlayout
            val iv = ImageView(activity)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(10, 16, 10, 0)
            val lpText = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lpText.setMargins(10, 0, 10, 5)
            lpText.gravity = Gravity.CENTER
            iv.layoutParams = lp
            iv.setImageBitmap(getBitmap(item.batchNumber))
            iv.layoutParams.height = 512
            iv.layoutParams.width = 512

            val tv1 = TextView(activity)
            tv1.text = getString(R.string.lot_id).plus(" : ").plus(item.batchNumber)
            val tv2 = TextView(activity)
            tv2.text = getString(R.string.material).plus(" : ").plus(item.materialName)
            val tv4 = TextView(activity)
            if (item.grade!!.isNotEmpty()) {
                tv4.text = getString(R.string.grade).plus(" : ").plus(item.grade)
            }
            val tv5 = TextView(activity)
            if (item.certificate!!.isNotEmpty()) {
                tv5.text = getString(R.string.certificate).plus(" : ").plus(item.certificate)
            }
            val tv3 = TextView(activity)
            if (!item.editedWeight.isNullOrEmpty())
                tv3.text =
                    getString(R.string.weight).plus(" : ").plus(item.editedWeight?.toDouble()?.formatThreeDigits())
                        .plus(" ").plus(item.unitOfMeasure)
            tv1.layoutParams = lpText
            tv2.layoutParams = lpText
            if (item.grade!!.isNotEmpty()) tv4.layoutParams = lpText
            tv3.layoutParams = lpText
            parent.addView(iv) // lo agregamos al layout
            parent.addView(tv1)
            parent.addView(tv2)
            if (item.grade!!.isNotEmpty()) parent.addView(tv4)
            if (item.certificate!!.isNotEmpty()) parent.addView(tv5)
            parent.addView(tv3)
            bitmapValue.put(index, getBitmapFromView(parent))
        }
    }

    private fun setupAdapter() {
        binding.rvPreviewList.setUpAdapter(
            lotList,
            R.layout.item_print_lot_card_preview,
            ItemPrintLotCardPreviewBinding::inflate,
            { item, pos, binding ->
                binding.tvLotValue.text = item.batchNumber
                binding.tvMaterialValue.text = item.materialName
                if (item.grade!!.isNotEmpty()) {
                    binding.tvGradeValue.text = item.grade
                    binding.tvGrade.visible()
                    binding.tvGradeValue.visible()
                }
                if (item.certificate!!.isNotEmpty()) {
                    binding.tvCertificateValue.text = item.certificate
                    binding.tvCertificate.visible()
                    binding.tvCertificateValue.visible()
                }
                binding.tvWeightValue.text = item.editedWeight.plus(" ").plus(item.unitOfMeasure)
                binding.ivPreview.setImageBitmap(getBitmap(item.batchNumber))
                //bitmapValue.put(pos, getBitmapFromView(binding.rvPreviewList))
            })
    }

    fun getBitmap(lot: String): Bitmap? {
        val content = lot
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun getBitmapFromView(view: View): Bitmap? {
        /* val bitmap = Bitmap.createBitmap(512, 720, Bitmap.Config.ARGB_8888)
         val canvas = Canvas(bitmap)
         view.draw(canvas)
         return bitmap*/
        if (view.measuredHeight <= 0) {
            view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val b = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.draw(c)
            return b
        }
        return null
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
        lotList.forEachIndexed { index, lot ->
            bitmapList.add(bitmapValue[index])
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
        MaterialDialog(requireActivity()).show {
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
        if (mChatService != null) {
            mChatService?.stop()
            mBTSocket?.close()
        }
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
