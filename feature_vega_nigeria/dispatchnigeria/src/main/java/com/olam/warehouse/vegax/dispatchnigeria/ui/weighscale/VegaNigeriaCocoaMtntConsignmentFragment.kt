package com.olam.warehouse.vegax.dispatchnigeria.ui.weighscale

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingWarehouseWithMtns
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.work.convertKgToMT
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaCocoaMtntPurchaseOrder
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaSesameMtntPurchaseOrders
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentNigeriaCocoaWeighscaleConsignmentLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.databinding.ItemNigeriaCocoaMaterialLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaCustomSingleSelectDialog
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaSingleSelectListener
import com.olam.warehouse.vegax.dispatchnigeria.utils.MTNT_WEIGHSCALE
import com.olam.warehouse.vegax.dispatchnigeria.utils.WEIGHSCALE_ADD_LOT
import com.olam.warehouse.vegax.dispatchnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaNigeriaCocoaMtntConsignmentFragment : BaseFragment(),
    VegaNigeriaCocoaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_nigeria_cocoa_weighscale_consignment_layout
    private lateinit var binding: FragmentNigeriaCocoaWeighscaleConsignmentLayoutBinding

    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var customDialog: VegaNigeriaCocoaCustomSingleSelectDialog? = null
    private var purchaseOrderList = mutableListOf<VegaNigeriaCocoaMtntPurchaseOrder>()
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()
    private var STONumbers = ArrayList<String>()
    private var wareHouseId = ""
    private var selectedPurchaseOrder: VegaNigeriaSesameMtntPurchaseOrders? = null
    private var selectedMaterial: String = ""
    private var materials: ArrayList<VegaNigeriaSesameMtntPurchaseOrders>? = null
    private var purchaseOrder = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private var filteredPurchaseOrderList = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private lateinit var callback: VegaNigeriaCocoaReplaceFragmentCallback
    private var materialModelList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var productList = emptyList<VegaMaterial>()
    private var warehouseWithMtn = VegaReceivingWarehouseWithMtns()
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var sendingWareHouseList = mutableListOf<VegaCustomStLocation>()
    private var selectedMaterialList: ArrayList<VegaCoffeePurchaseOrderMaterialModel> = ArrayList()
    private var selectedSendingWH = ""
    private val RECORD_REQUEST_CODE_READ_WRITE = 102
    val CAMERA_REQUEST_CODE = 0
    private val RECORD_REQUEST_CODE_WRITE = 103
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
    private var imageList = ArrayList<String>()
    private var pMaterialList= arrayListOf<VegaPackageMaterial>()
    private var isCompliantMaterial: Boolean = false
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaNigeriaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaNigeriaCocoaMtntConsignmentFragment()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaCocoaWeighscaleConsignmentLayoutBinding.inflate(layoutInflater)
        initUI()
        vm.getSuppliers()
        return binding.root
    }

    private fun initUI() {
            showTTDialog()
//        binding.tvWhValue.setOnClickListener {
//            showReceivingLocationDialog(customLocationList.filter {
//                !it.storageLocationType.equals(
//                    "B"
//                )
//            })
//        }
//        binding.tvSendingWHValue.setOnClickListener { showDispatchWHDialog(wareHouseList) } // Sending
//        binding.tvstoValue.setOnClickListener { showStoDialog(warehouseWithMtn) }
//        binding.tvmaterialValue.setOnClickListener { showProductDialog(productList.filter { it.mtnNumber == binding.tvSto.text.toString() }) }
//
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        vm.material.observe(viewLifecycleOwner,Observer{
           it?.let {
               pMaterialList=it as ArrayList<VegaPackageMaterial> /* = java.util.ArrayList<com.olam.warehouse.master.vega.entity.VegaPackageMaterial> */
           }
        })
        vm.getMaterials()

        vm.waitingTrucks1.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })

        binding.tvWhValue.setOnClickListener {
            showSingleSelectDialog(true, getString(R.string.select_dest_wh), false, false)
        }
        binding.tvSendingWHValue.setOnClickListener {
            if (sendingWareHouseList.size > 1) {
//                binding.tvSendingWHValue.setCompoundDrawablesWithIntrinsicBounds(0, 0,R.drawable.ic_add_icon, 0)
                showSingleSelectDialog(
                    false,
                    getString(R.string.select_sent_wh), true, false
                )
            }
        }
        binding.tvstoValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_sto), false, false
            )
        }
        binding.tvmaterialValue.setOnClickListener {
            if (binding.tvWhValue.text.toString().isNotEmpty()) showSingleSelectDialog(
                false,
                getString(R.string.select_material), false, true
            )
        }

        //vm.getPurchaseOrder("")
        binding.btnProceed.setOnClickListener { validateProceed() }
        enableProceed()
        vm.purchaseOrder.observe(viewLifecycleOwner, Observer { updatePurchaseOrder(it) })
        binding.tvDriverNameValue.onChange { enableProceed() }
        binding.tvTruckNoValue.onChange { enableProceed() }
        binding.tvDriverNoValue.onChange { enableProceed() }


        vm.suppplier.observe(viewLifecycleOwner, Observer {
            if (it != null)
                supplierList = it as MutableList
        })
        //vm.getAllProduct()
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it.filter { if(isCompliantMaterial) it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                }
            })
        // vm.location.observe(viewLifecycleOwner, Observer { sendingWareHouseList = it.toMutableList() })
        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            sendingWareHouseList = it.toMutableList()
            if (sendingWareHouseList.size == 1) {
                sendingWareHouseList.forEach {
                    binding.tvSendingWHValue.text =
                        sendingWareHouseList[0].procureLocationCode.plus(" - ").plus(
                            sendingWareHouseList[0].procureLocationName
                        )
                    selectedSendingWH = sendingWareHouseList[0].procureLocationCode
                }
            }
        })
        vm.getCustomLocations()
        //vm.getSendingWHLocations()

        binding.llCamera.setOnClickListener {
            if (imageList.size > 5 && imageList.size == 6) {
                Toast.makeText(context, "Only Six Images can be uploaded ", Toast.LENGTH_SHORT)
                    .show()
            } else {
                setupPermissions()
            }
        }

        binding.closeDialog.setOnClickListener {
            binding.photoImageView.setImageBitmap(null)
            binding.photoImageView.visibility = View.GONE
            binding.closeDialog.visibility = View.GONE
            if (imageList.size > 0) {
                // imageList.removeAt(0)
                imageList.remove(imageList[0])
            }
        }

        binding.closeDialog1.setOnClickListener {
            binding.photoImageView1.setImageBitmap(null)
            binding.photoImageView1.visibility = View.GONE
            binding.closeDialog1.visibility = View.GONE
            if (imageList.size > 1) {
                //imageList.removeAt(1)
                imageList.remove(imageList[1])
            }
        }

        binding.closeDialog2.setOnClickListener {
            binding.photoImageView2.setImageBitmap(null)
            binding.photoImageView2.visibility = View.GONE
            binding.closeDialog2.visibility = View.GONE
            if (imageList.size > 2) {
                //imageList.removeAt(2)
                imageList.remove(imageList[2])
            }
        }

        binding.closeDialog3.setOnClickListener {
            binding.photoImageView3.setImageBitmap(null)
            binding.photoImageView3.visibility = View.GONE
            binding.closeDialog3.visibility = View.GONE
            if (imageList.size > 3) {
                //imageList.removeAt(2)
                imageList.remove(imageList[3])
            }
        }

        binding.closeDialog4.setOnClickListener {
            binding.photoImageView4.setImageBitmap(null)
            binding.photoImageView4.visibility = View.GONE
            binding.closeDialog4.visibility = View.GONE
            if (imageList.size > 4) {
                //imageList.removeAt(2)
                imageList.remove(imageList[4])
            }
        }

        binding.closeDialog5.setOnClickListener {
            binding.photoImageView5.setImageBitmap(null)
            binding.photoImageView5.visibility = View.GONE
            binding.closeDialog5.visibility = View.GONE
            if (imageList.size > 5) {
                //imageList.removeAt(2)
                imageList.remove(imageList[5])
            }
        }


        binding.photoImageView.setOnClickListener {
            val image = ImageView(context)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(10, 16, 10, 0)
            lp.gravity = Gravity.CENTER
            image.layoutParams = lp
            image.setImageBitmap(setScaledBitmap(imageList[0]))
            /*iv.layoutParams.height = 512
            iv.layoutParams.width = 512*/
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(context).setMessage("Message above the image")
                    .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                        }
                    }).setView(image)
            builder.create().show()
        }

    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaGateEntry>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    createPdf()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                }
            }
        }

    }

    private fun setupPermissions() {
//        val permission =
//            activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
//        if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()
        if(!hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) setupAllPermissions(
            REQUIRED_CAMERA_PERMISSIONS,
            object: PermissionObserve {
                override fun grandValue(grandValue: Boolean) {
                    if(grandValue) moveToCameraView()
                }
            }) else moveToCameraView()
    }

    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            RECORD_REQUEST_CODE_READ_WRITE -> if (grantResults.size > 0) {
                val readAccepted = grantResults[0] === PackageManager.PERMISSION_GRANTED
                val writeAccepted = grantResults[1] === PackageManager.PERMISSION_GRANTED
                if (readAccepted && writeAccepted) Toast.makeText(
                    context,
                    "Permission Granted, Now you can read and write data.",
                    Toast.LENGTH_LONG
                ).show() else {
                    Toast.makeText(
                        context,
                        "Permission Denied, You cannot read and write data.",
                        Toast.LENGTH_LONG
                    ).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            val dialogBuilder = AlertDialog.Builder(context)
                            dialogBuilder.setTitle("Permissions")
                            dialogBuilder.setMessage("You need to allow access to both the permissions")
                            dialogBuilder.setPositiveButton("Done",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            ),
                                            RECORD_REQUEST_CODE_READ_WRITE
                                        )
                                    }
                                })
                            dialogBuilder.show()
                            return
                        }
                    }
                }
            }
        }
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        permissions[0]
                    )
                }!!

                when (granted) {
                    true -> moveToCameraView()
                    else -> {}
                }
            }
        }

        when (requestCode) {
            RECORD_REQUEST_CODE_WRITE -> {
                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        permissions[0]
                    )
                }!!

                when (granted) {
                    true -> createPdf()
                    else -> {}
                }
            }
        }
    }*/

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = requireActivity().packageName + ".fileprovider"
                val imageUri =
                    activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            showErrorDialogWithFAQLink(requireContext(), "Could not create file!")
            //activity?.toast("Could not create file!")
        }catch (e:IllegalStateException){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateCameraLayout()
                } else {
                    imageFilePath = ""
                }
            }
            else -> {
                showErrorDialogWithFAQLink(requireContext(), "Unrecognized request code")
                //activity?.toast("Unrecognized request code")
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PNG_" + timeStamp + "_"
        //val imageFileName: String = "JPEG_".plus(binding.tvTruckNo.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".png", storageDir)
        imageFilePath = imageFile.absolutePath
        imageList.add(imageFilePath)
        return imageFile
    }

    fun updateCameraLayout() {
        // Toast.makeText(context, "Image taken  ", Toast.LENGTH_SHORT).show()

        // binding.photoImageView.visible()
        // binding.closeDialog.visible()
        //val ThumbImage1: Bitmap =
        // ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageList[0]), 64, 64)

        //  binding.photoImageView.setImageBitmap(setScaledBitmap(imageList[0]))
        //binding.photoImageView.setImageBitmap(ThumbImage1)


        if (imageList.size > 0) {
            binding.photoImageView.visible()
            binding.closeDialog.visible()
            binding.photoImageView.setImageBitmap(setScaledBitmap(imageList[0]))
            if (imageList.size > 1 && imageList.size >= 2) {
                binding.photoImageView1.visible()
                binding.closeDialog1.visible()
                binding.photoImageView1.setImageBitmap(setScaledBitmap(imageList[1]))
            }
            if (imageList.size > 2 && imageList.size == 3) {
                binding.photoImageView2.visible()
                binding.closeDialog2.visible()
                binding.photoImageView2.setImageBitmap(setScaledBitmap(imageList[2]))
            }
            if (imageList.size > 3 && imageList.size == 4) {
                binding.photoImageView3.visible()
                binding.closeDialog3.visible()
                binding.photoImageView3.setImageBitmap(setScaledBitmap(imageList[3]))
            }
            if (imageList.size > 4 && imageList.size == 5) {
                binding.photoImageView4.visible()
                binding.closeDialog4.visible()
                binding.photoImageView4.setImageBitmap(setScaledBitmap(imageList[4]))
            }
            if (imageList.size > 5 && imageList.size == 6) {
                binding.photoImageView5.visible()
                binding.closeDialog5.visible()
                binding.photoImageView5.setImageBitmap(setScaledBitmap(imageList[5]))
            }
        }
//            val ThumbImage2: Bitmap =
//                ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageList[0]), 64, 64)

        //binding.photoImageView.setImageBitmap(ThumbImage2)
        // }

/*        if(imageList[1].isNotEmpty()){
            binding.photoImageView1.visible()
            binding.photoImageView1.setImageBitmap(setScaledBitmap(imageList[1]))
        }*/

//            binding.photoImageView.setImageBitmap(setScaledBitmap(imageList[0]))
//            binding.photoImageView1.setImageBitmap(setScaledBitmap(imageList[1]))

        //binding.ivCamere.text = gateEntryData.vehicleNumber.plus(".jpg")
        /*ViewCompat.setBackgroundTintList(
            binding.ivCamere,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.blue_light
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.llCamera,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.blue_light
            )
        )*/
    }


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


    private fun checkPermission(): Boolean {
        val writeStoragepermission = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val readStoragepermission = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        return writeStoragepermission == PackageManager.PERMISSION_GRANTED && readStoragepermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            RECORD_REQUEST_CODE_READ_WRITE
        )
    }

    fun isStoragePermissionGranted(): Boolean {
        val TAG = "Storage Permission"
        return if (Build.VERSION.SDK_INT >= 23) {
            val permission =
                activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) }
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                true
            } else {
                Log.v(TAG, "Permission is revoked")
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RECORD_REQUEST_CODE_WRITE
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }
    }

    private fun setScaledBitmap(image: String): Bitmap? {
        try {
            val imageViewWidth = 200
            val imageViewHeight = 200

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true

            BitmapFactory.decodeFile(image, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = calculateInSampleSize(bmOptions, imageViewWidth, imageViewHeight)
            //Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inSampleSize = scaleFactor
            bmOptions.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(image, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    private fun createPdf() {

        if (hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) {

            val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (!storageDir!!.exists()) storageDir.mkdirs()
            val imageFile = File.createTempFile("UniqueFileName", ".png", storageDir)


            try {
                if (imageFile.exists()) {
                } else {
                    imageFile.createNewFile()
                }
                val fileOutputStream = FileOutputStream(imageFile)
                val pdfDocument = PdfDocument()
                for (i in 0 until imageList.size) {

                    val bmOptions = BitmapFactory.Options()
                    bmOptions.inJustDecodeBounds = true

                    BitmapFactory.decodeFile(imageList.get(i), bmOptions)
                    val scaleFactor = calculateInSampleSize(bmOptions, 200, 200)
                    bmOptions.inSampleSize = scaleFactor
                    bmOptions.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeFile(imageList.get(i), bmOptions)
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap, 200, 200,
                        false
                    )
                    /*  val Options = BitmapFactory.Options()
                      Options.inSampleSize = 4
                      Options.inJustDecodeBounds = false
                      val bitmap = BitmapFactory.decodeFile(imageList.get(i), Options)
                      val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200,
                          false) // filter=false if downscaling, true if upscaling*/

                    val pageInfo = PdfDocument.PageInfo.Builder(200, 200, i + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = Paint()
                    paint.color = Color.BLUE
                    canvas.drawPaint(paint)


                    canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    scaledBitmap.recycle()
                }
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()


                try {
                    var encodeFileToBase64Binary = encodeFileToBase64Binary(imageFile)
                    vm.dispatchWh.encodedImageContent = encodeFileToBase64Binary
                    var encodeFileToBase64BinaryNew = encodeFileToBase64Binary(imageFile)
                    Handler().postDelayed(Runnable {
                        hideLoading()
                        callback.replaceFragment(WEIGHSCALE_ADD_LOT, vm.dispatchWh)
                    }, 5000)
                    // startActivity(pdfIntent)
                } catch (e: ActivityNotFoundException) {
                    hideLoading()
                    Toast.makeText(context, "Can't read pdf file", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IOException) {
                hideLoading()
                e.printStackTrace()
            }
        }
    }

    private fun encodeFileToBase64Binary(yourFile: File): String {
        val size = yourFile.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(yourFile))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun validateProceed() =
        if (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvDriverNameValue.text.toString()
                .isNotEmpty()
            && binding.tvTruckNoValue.text.toString()
                .isNotEmpty() && binding.tvSendingWHValue.text.toString().isNotEmpty()
            && binding.tvmaterialValue.text.toString().isNotEmpty()
        ) {
            when {
                imageFilePath.isNullOrEmpty() -> {
                    showSnack(getString(R.string.error_valid_ticket_photo))
                }
                (imageList.size == 0) -> {
                    showSnack(getString(R.string.error_valid_ticket_photo))
                }
                else ->
                    if (hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) {
                        prepareTruckWb()
                        vm.saveWeighBridgeDetails()
                        selectedMaterialList.forEach {
                            it.weighBridgeId = vm.dispatchWh.weighBridgeId
                        }
                        saveMaterialDetails(selectedMaterialList)
                        vm.dispatchWh.imagePath = imageFilePath
                        vm.dispatchWh.imagesList = imageList
                        vm.getWaitingTruckList(vm.dispatchWh.plantId.toString())
                        // createPdf()
                    } else {
                        //Toast.makeText(context, "Please request permission.", Toast.LENGTH_LONG).show();
                        //requestPermission()
                        setupAllPermissions(REQUIRED_CAMERA_PERMISSIONS, object: PermissionObserve {
                            override fun grandValue(grandValue: Boolean) {
                                if(grandValue) moveToCameraView()
                            }
                        })
                    }
            }

        } else {
            showSnack(getString(R.string.mandatory))
        }

    private fun prepareTruckWb() {
        if (vm.dispatchWh.weighBridgeId.isEmpty()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.dispatchWh.weighBridgeId = randomDouble
            vm.dispatchWh.weighBridgeId = randomDouble
        }
        vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
        vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
        vm.dispatchWh.weighBridgeType = MTNT_WEIGHSCALE
        vm.dispatchWh.netWeight = selectedPurchaseOrder?.openQuantity
        vm.dispatchWh.unitsOfMeasure = selectedPurchaseOrder?.meins
        vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId ?: ""
        vm.dispatchWh.storageLocationCode = binding.tvSendingWHValue.text.toString()
        vm.dispatchWh.recStorageLocationCode = selectedSendingWH
        vm.dispatchWh.recPlantId = selectedPurchaseOrder?.warehouseId
        vm.dispatchWh.materialName = binding.tvmaterialValue.text.toString()
        vm.dispatchWh.vehicleNumber = binding.tvTruckNoValue.text.toString()
        vm.dispatchWh.driverName = binding.tvDriverNameValue.text.toString()
        vm.dispatchWh.driverPhoneNumber = binding.tvDriverNoValue.text.toString()
        vm.dispatchWh.erdat = getCurrentDate()
    }

    private fun showSingleSelectDialog(
        isWh: Boolean,
        title: String,
        isSendingWH: Boolean,
        isMaterial: Boolean
    ) {
        var list: ArrayList<String> = ArrayList()
        if (isSendingWH) {
            list = sendingWareHouseList.map { data ->
                data.procureLocationCode.plus("-").plus(data.procureLocationName)
            } as ArrayList<String>
        } else if (isWh) {
            list = storageLocation
        } else if (isMaterial) {
            if (materialModelList.size > 1)
                list = materialModelList.map { it.materialName }.distinct() as ArrayList<String>
            else
                Toast.makeText(context, "Materials are unavailable", Toast.LENGTH_SHORT).show()
        } else {
            list = STONumbers
        }

        if (list.isNotEmpty()) {
            customDialog =
                VegaNigeriaCocoaCustomSingleSelectDialog(
                    title,
                    isWh, isSendingWH, isMaterial,
                    list,
                    requireActivity(),
                    this
                )
            customDialog?.show()
            customDialog?.setCanceledOnTouchOutside(false)
        }

    }

    private fun enableProceed() {
        val enable =
            (binding.tvWhValue.text.isNotEmpty() && binding.tvstoValue.text.isNotEmpty()
                    && binding.tvTruckNoValue.text.toString()
                .isNotEmpty() && binding.tvmaterialValue.text.toString().isNotEmpty()
                    )
        if (enable) {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enable
    }

    override fun clickOnItem(
        data: String,
        isWh: Boolean,
        isSendingWH: Boolean,
        isMaterial: Boolean
    ) {
        customDialog?.dismiss()
        if (isWh) {
            binding.tvWhValue.text = data
            binding.tvstoValue.text = ""
            binding.tvmaterialValue.text = ""
            materialModelList.clear()
            selectedMaterialList.clear()
            binding.rvMaterialList.adapter?.notifyDataSetChanged()
            wareHouseId = data
            val da = data.split("-").toTypedArray()
            vm.dispatchWh.recPlantId = da[0]
            vm.dispatchWh.plantName = da[1]
            getPurchaseOrderByStorageLocation(da[0])
        } else if (isMaterial) {

            binding.tvmaterialValue.text = data
            selectedMaterial = data
            selectedMaterialList =
                materialModelList.filter { it.materialName.toString() == data } as ArrayList<VegaCoffeePurchaseOrderMaterialModel>
            setUpMaterialAdapter()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
            }

        } else if (!isSendingWH) {
            vm.removeLotList()

            vm.deleteMaterialData()
            if (materialModelList.isNotEmpty()) {
                val material = materialModelList
                vm.deleteMaterialData(material[0].weighBridgeId)
            }
            binding.tvmaterialValue.text = ""

            selectedMaterialList.clear()
            setUpMaterialAdapter()

            getWeight(data)

            selectedPurchaseOrder?.storageLocationName = vm.dispatchWh.plantName
            binding.tvstoValue.text = data
            vm.dispatchWh.purchaseDocNum = selectedPurchaseOrder?.purchaseDocNum
            vm.dispatchWh.purchaseDocDesc = selectedPurchaseOrder?.purchaseDocDesc
            vm.dispatchWh.plantId = selectedPurchaseOrder?.plantId
            vm.weighScaleWithLotMaterial.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    vm.dispatchWh = it.dispatch
                } else {
                    binding.tvTruckNoValue.setText("")
                    binding.tvDriverNameValue.setText("")
                    binding.tvDriverNoValue.setText("")
                }
            })
            vm.getWeighScaleWithLotAndMaterial(
                vm.dispatchWh.plantId ?: "",
                vm.dispatchWh.purchaseDocNum ?: ""
            )
            enableProceed()
        } else if (isSendingWH) {
            binding.tvSendingWHValue.text = data
            val da = data.split("-").toTypedArray()

            selectedSendingWH = da[0]
            enableProceed()
        }
    }

    private fun getListforSelectedMaterial(materialName: String) {
        val orderList = purchaseOrder.filter { it.materialName == materialName.trim() }
        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())

    }

    private fun getWeight(purchaseId: String) {
        materialModelList.clear()
        materials?.clear()
        selectedMaterialList.clear()

        materials =
            filteredPurchaseOrderList.filter { s ->
                s.purchaseDocNum == purchaseId
            } as ArrayList<VegaNigeriaSesameMtntPurchaseOrders>

        materials?.forEach {
            val materialModel = VegaCoffeePurchaseOrderMaterialModel()
            materialModel.materialCode = it.materialCode
            materialModel.soNumber = it.purchaseDocNum
            var tareWeightBag = if ((it.meins).equals("KG")) convertKgToMT(
                (it.openQuantity).toString().trim()
            ) else (it.openQuantity).toString().trim()
            //materialModel.soWeight = convertMtToKg(it.openQuantity!!, it.meins.toString())
            materialModel.soWeight = tareWeightBag
            materialModel.uom = it.meins
            materialModel.purchaseOrderNum = it.purchaseDocNum ?: ""
            materialModel.purchaseOrderDesc = it.purchaseDocDesc ?: ""
            materialModelList.add(materialModel)
        }

        materialModelList.forEach { item ->
            productList.forEach { item1 ->
                if (item.materialCode.contains(item1.materialCode)) {
                    val materialName =
                        productList.single { item.materialCode.contains(it.materialCode) }
                    if (materialName.materialName!!.isNotEmpty())
                        item.materialName = materialName.materialName
                }
            }
        }
       //
        val bagMaterialCode= pMaterialList.filter { it.bagMaterialCode.isNotEmpty() }.map { it.bagMaterialCode }
        val bagMaterialList= materialModelList.filter { bagMaterialCode.contains(it.materialCode) }
        if(bagMaterialList.isEmpty())materialModelList = materialModelList.filter { it.materialName!!.isNotEmpty() } as ArrayList<VegaCoffeePurchaseOrderMaterialModel>
       /* if (materialModelList.size == 1) {


        }*/
        val materialNameItem = materialModelList.filter { it.materialName?.isNotEmpty() == true }
        if(materialNameItem.isNotEmpty())binding.tvmaterialValue.text = materialNameItem[0].materialName
        selectedMaterialList = materialModelList
        setUpMaterialAdapter()
        selectedPurchaseOrder = materials?.get(0)

    }

    private fun updateUIWithDbData() {
        binding.tvTruckNoValue.setText(vm.dispatchWh.vehicleNumber)
        binding.tvDriverNameValue.setText(vm.dispatchWh.driverName)
        binding.tvDriverNoValue.setText(vm.dispatchWh.driverPhoneNumber)
    }

    private fun saveMaterialDetails(list: ArrayList<VegaCoffeePurchaseOrderMaterialModel>) {
        vm.saveMaterialDetails(list)
    }

    private fun setUpMaterialAdapter() {

        binding.rvMaterialList.setUpAdapter(
            selectedMaterialList,
            R.layout.item_nigeria_cocoa_material_layout,
            ItemNigeriaCocoaMaterialLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvMaterialName.text = if(item.materialName?.isNotEmpty() == true)item.materialName else item.materialCode
                bindItem.tvStoWeightValue.text =
                    item.soWeight?.toDouble()?.formatThreeDigits()?.replace(",", "").plus(" ")
                        .plus(item.uom)
                bindItem.tvDispatchWeight.visibility = View.GONE
                bindItem.tvDispatchWeightValue.visibility = View.GONE
                bindItem.llTotalWeightLoss.visibility = View.GONE
            })
    }

    private fun updatePurchaseOrder(response: Resource<GenericReqAndResp<List<VegaNigeriaCocoaMtntPurchaseOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        purchaseOrderList = it1 as MutableList<VegaNigeriaCocoaMtntPurchaseOrder>
                    }
                    filterSTONumber()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun getPurchaseOrderByStorageLocation(warehouseId: String) {
        STONumbers.clear()
        val orderList = purchaseOrder.filter { it.warehouseId == warehouseId.trim() }
        filteredPurchaseOrderList = orderList as ArrayList<VegaNigeriaSesameMtntPurchaseOrders>
        STONumbers.addAll(orderList.map { it.purchaseDocNum ?: "" }.toSet().toList())
    }

    private fun getMTNTPlantName(plantId: String): String {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.MTNT_PLANT_LIST, ""))
        val plant = plants.singleOrNull { it.plantId == plantId }
        return plant?.plantName ?: ""
    }

    private fun filterSTONumber() {
        var metrials = productList.map { it.materialCode }
        for (item in purchaseOrderList) {
            if (metrials.contains(item.materialCode?.takeLast(12))|| item.materialCode?.takeLast(12)?.startsWith("2")==true)
                purchaseOrder.addAll(item.purchaseOrders)
        }
        val location =
            purchaseOrder.map { it.warehouseId.plus("-").plus(getMTNTPlantName(it.warehouseId ?: "")) }.toSet()
        storageLocation.addAll(location)
    }


    private fun getCurrentDate(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(Date())
    }

    private fun showTTDialog() {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(com.olam.warehouse.login.R.string.select_procurement_type)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",

                {
                        isComplaint, isThirdParty, isFarmerLessTransaction ->  if(isComplaint) isCompliantMaterial = true else false
                        vm.getAllProduct()
                        vm.getPurchaseOrder("")


                },
                { dismiss() },
                false)
        }

    }

}
