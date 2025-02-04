package com.olam.warehouse.vegax.gateentrynigeria.ui

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.itextpdf.text.Image
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentrynigeria.R
import com.olam.warehouse.vegax.gateentrynigeria.data.domain.model.VegaNigeriaGateEntryPostData
import com.olam.warehouse.vegax.gateentrynigeria.databinding.FragmentVegaNigeriaAddNewTruckBinding
import com.olam.warehouse.vegax.gateentrynigeria.utils.*
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VegaNigeriaAddNewTruckFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntry()
    private var mtnsList = mutableListOf<VegaReceivingMtn>()
    private var productList = mutableListOf<VegaReceivingMtnLots>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var plantList = mutableListOf<Plant>()
    private var warehouselist = mutableListOf<VegaSupplyStorageLocation>()
    private var selectedStorageLoc: String? = ""
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
    private val RECORD_REQUEST_CODE_READ_WRITE = 102
    private var vegaWbIds = listOf<VegaGateEntry>()
    private var imageList = ArrayList<String>()
    private var jsonData = mutableListOf<String>()
    private var procurementTypeList = ArrayList<String>()
    private var openGrntTypeList = ArrayList<String>()
    private var gateEntryOrderList = mutableListOf<VegaNigeriaGateEntryPostData>()
    private var grntOrderList = ArrayList<VegaNigeriaGateEntryPostData>()

    //private var gateEntryOrderList = ArrayList<VegaNigeriaGateEntryPostData>()
    private var procurementType: String? = ""
    private var selectedPlantId = ""
    var image: Image? = null
    private var pdfFile: File? = null
    private var isSummaryEnabled: Boolean? = false

    interface CallBack {
        fun replaceFragment(
            paramsListFrag: String,
            item: VegaGateEntry,
            plantDetails: Plant
        )
    }

    private val vm: VegaGateEntryNigeriaViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaAddNewTruckBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_add_new_truck

    companion object {
        fun newInstance(gateEntryData: VegaGateEntry) = VegaNigeriaAddNewTruckFragment().putArgs {
            putParcelable(GATE_ENTRY_DATA, gateEntryData)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaAddNewTruckBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("gateentryNigeria/ui/VegaAddNewTruckFragment").title("Gate Entry").with(tracker)
        initUI()
    }

    private fun initUI() {

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())



        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))

        vm.waitingTrucks1.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                if (AppUtils.isOnline()) vm.getWaitingTruckList(selectedPlantId)
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)



        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        updateMandatory()
        if (gateEntryData.weighBridgeType == PROCURE) {
            binding.llSupplier.visible()
            binding.llMtnr.gone()
        } else {
            binding.llSupplier.gone()
            binding.llMtnr.visible()
        }
        
        binding.tvDate.text = getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
//        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.btnConfirm.setOnClickListener { validateInputs() }

        vm.product.observe(viewLifecycleOwner, Observer {
            val products = it.map { data -> data.materialName }
            it.forEach { it1 ->
                if (it1.materialName.equals("Ivory Coast Raw Cashew Nut")) {
                    gateEntryData.materialCode =
                            //MATERIAL_CODE.plus(it1.materialCode.toString())
                        (it1.materialCode.toString())
                    gateEntryData.materialName = it1.materialName.toString()
                    binding.tvProduct.setText(it1.materialName, TextView.BufferType.EDITABLE)
                    binding.tvUom.text = it1.unitsOfMeasure.toString()
                    gateEntryData.unitsOfMeasure = it1.unitsOfMeasure.toString()
                }
            }
            val productAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, products)
            binding.tvProduct.threshold = 1
            binding.tvProduct.setAdapter(productAdapter)
            binding.tvProduct.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { material ->
                    if (material.materialName == binding.tvProduct.text.toString()) {
                        gateEntryData.materialCode =
                                // MATERIAL_CODE.plus(material.materialCode.toString())
                            (material.materialCode.toString())
                        gateEntryData.materialName = material.materialName.toString()
                        binding.tvUom.text = material.unitsOfMeasure.toString()
                        gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }
            }
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            val suppliers = it
//                .filter { data -> data.bcApprover?.isNotEmpty()!! }
                .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            val supplierAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, suppliers)
            binding.tvSupplier.threshold = 1
            binding.tvSupplier.setAdapter(supplierAdapter)
            binding.tvSupplier.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
                it.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        gateEntryData.supplierCode = vendor.vendorCode
                        gateEntryData.supplierName = vendor.vendorName
                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                    }
                }
            }
        })
        vm.getSuppliers()

        vm.suppplierZone.observe(viewLifecycleOwner, Observer {
            it?.let { item ->
                if (it.size > 0) {
                    binding.tvSupplierZone.setText(item[0].bczone.toString())
                    binding.tvSupplierZone.isEnabled = false
                }
            }

        })

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            custonLocationList = it.toMutableList()
            var receivingLocationList = custonLocationList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            }
            if (receivingLocationList.size == 1) {
                binding.tvReceivingLocation.text =
                    custonLocationList[0].procureLocationCode.plus(" - ")
                        .plus(custonLocationList[0].procureLocationName)
                gateEntryData.storageLocationCode = custonLocationList[0].procureLocationCode
                gateEntryData.storageLocationName = custonLocationList[0].procureLocationName
            }

        })
        vm.getCustomLocations()

        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer { updateProcessType(it) })
        vm.getProcessTypeList(getCurrentKey())

        vm.gateEntryGrnt.observe(viewLifecycleOwner, Observer { updateGRNTType(it) })

        plantList = getMultiPlantList() as MutableList<Plant>
        if (plantList.size == 1) {
            binding.tvPlant.text = plantList[0].plantId.plus(" - ")
                .plus(plantList[0].plantName)
            gateEntryData.plantId = plantList[0].plantId
            gateEntryData.plantName = plantList[0].plantName
        }

        /*vm.multiPlant.observe(viewLifecycleOwner, Observer {
            plantList = it.toMutableList()

            if (plantList.size == 1) {
                binding.tvReceivingLocation.text = plantList[0].plantId.plus(" - ")
                    .plus(plantList[0].plantName)
            }
        })
        vm.getMultiPlantList()*/

        binding.tvPlant.setOnClickListener {
            if (plantList.size >= 1) {
                showPlantSelectionDialog(plantList)
                binding.tvReceivingLocation.text = ""
            }
        }

        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
            fetchMtnDetails()
        }
        binding.tvObdNumber.setOnClickListener { showStoDialog(mtnsList) }
        binding.tvProcurementValue.setOnClickListener {
            showProcurementDialogDialog(
                procurementTypeList
            )
        }
        binding.tvOpenGrntValue.setOnClickListener {
            if (binding.tvProduct.text.isNotEmpty() && binding.tvSupplier.text.isNotEmpty()) {
                vm.openGrntDetails(
                    getCurrentKey(),
                    MATERIAL_CODE.plus(gateEntryData.materialCode.toString()),
                    SUPPLIER_CODE.plus(
                        gateEntryData.supplierCode.toString()
                    ),
                    selectedPlantId
                )
            } else {
                showSnack(getString(R.string.error_valid_reason))
            }
        }
        binding.tvDispatchWarehouse.setOnClickListener { showDispatchWHDialog(warehouselist) }
        binding.tvReceivingLocation.setOnClickListener {
            if (binding.tvPlant.text != "") {
                2
                var plantid = binding.tvPlant.text
                var receivingLocationList = custonLocationList.filter {
                    !it.storageLocationType.equals(
                        "B"
                    )
                }.filter { it.plant == gateEntryData.plantId }
                if (receivingLocationList.size >= 1) {
                    showReceivingLocationDialog(receivingLocationList)
                }
            }
        }

        imageFilePath = gateEntryData.imagePath.toString()
        if (imageFilePath.isNotEmpty()) updateCameraLayout()
        //  binding.ivCamera.setOnClickListener { setupPermissions() }

        binding.llCamera.setOnClickListener {
            if (imageList.size > 2 && imageList.size == 3) {
                Toast.makeText(context, "Only Three Images can be uploaded ", Toast.LENGTH_SHORT)
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

    private fun setupPermissions() {
        val permission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
        if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()
    }
    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            RECORD_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

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
                        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                            val dialogBuilder = AlertDialog.Builder(context)
                            dialogBuilder.setTitle("Permissions")
                            dialogBuilder.setMessage("You need to allow access to both the permissions")
                            dialogBuilder.setPositiveButton("Done",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
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
                }
            }
        }
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<List<VegaGateEntry>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (isSummaryEnabled == true) {
                        gateEntryData.contactNumber = binding.tvPhoneNo.text.toString()
                        gateEntryData.driverNumber = binding.tvPhoneNo.text.toString()
                        gateEntryData.erdat = getUTCDateTimeMillis(
                            binding.tvDate.text.toString(),
                            App.getAppContext()
                        )
                        gateEntryData.vehicleNumber = binding.tvTruckNo.text.toString()
                        gateEntryData.driverName = binding.tvDriverName.text.toString()
                        gateEntryData.truckDriverName = binding.tvDriverName.text.toString()
//        gateEntryData.supplierZone = binding.tvSupplierZone.text.toString()
                        gateEntryData.mtnCode = binding.tvMtntNoValue.text.toString()

                        //Modify below to add Tentative no of bags
                        gateEntryData.tempBagCount = binding.tvNoOfBagsValue.text.toString()

                        gateEntryData.delivery = binding.tvObdNumber.text.toString()
                        gateEntryData.approximateWeight = binding.etWeight.text.toString()
                        gateEntryData.wsGate = WS01
                        //var mergedImage = createSingleImageFromMultipleImages(imageList)
                        //var file = bitmapToFile(mergedImage,"megedimage_1")
                        //gateEntryData.imagePath = file?.absolutePath
                        gateEntryData.imagePath = imageFilePath
                        gateEntryData.imagesList = imageList
                        var plantDetails = getPlantDetails(selectedPlantId).single()
                        createPdf()
                    } else {
                        hideLoading()
                        // it.data?.data?.let { it1 ->
                        // // if (it1.isNotEmpty()) {
                        vegaWbIds = listOf()
                        binding.btnConfirm.isEnabled = true
                        binding.btnConfirm.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))
                        binding.linearOne.visibility = View.VISIBLE
                    }
                    // }
                    //}
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), response.error.toString())
                }
            }
        }

    }


    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_vega_nigeria_gateentry_plant_select,
                plantIdList
            )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        selectedPlantId = plantIdList[position]
//                    validateLot(selectedPlantId)
                    }
//                binding.spPlantSelection.setSelection(0)
                }
            }
    }

    private fun updateGRNTType(data: Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    openGrntTypeList.clear()
                    hideLoading()
                    data.data?.data?.let { it1 ->
                        gateEntryOrderList = it1 as MutableList<VegaNigeriaGateEntryPostData>
                    }
                    gateEntryOrderList.forEach { item ->
                        openGrntTypeList.add(item.grnNumber.toString())
                    }
                    showOpenGrntDialog(openGrntTypeList)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        jsonData.forEach {
            if (it.contains(JSON_PROCUREMENT_TYPE)) {
                var procurementTypeSelection =
                    (JSONObject(it).getJSONArray(JSON_PROCUREMENT_TYPE)).toString().split(",")
                //  procurementType = (JSONObject(it).getJSONArray(JSON_PROCUREMENT_TYPE).get(0)).toString()
                procurementTypeSelection.forEach { it1 ->
                    procurementTypeList.add(
                        (it1.split(":")[0]).replace("[", "").replace("]", "").replace("\"", "")
                    )
                }
            }
        }
    }

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = activity!!.packageName + ".fileproviders"
                val imageUri =
                    activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            UIUtils.showErrorDialog(requireContext(), "Could not create file!")
            //activity?.toast("Could not create file!")
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
                UIUtils.showErrorDialog(requireContext(), "Unrecognized request code")
                //activity?.toast("Unrecognized request code")
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        //val imageFileName: String = "JPEG_".plus(binding.tvTruckNo.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
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

    private fun setScaledBitmap(image: String): Bitmap? {
        try {
            val imageViewWidth = 200
            val imageViewHeight = 200

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true

            BitmapFactory.decodeFile(image, bmOptions)
            val bitmapWidth = bmOptions.outWidth
            val bitmapHeight = bmOptions.outHeight

            val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor

            return BitmapFactory.decodeFile(image, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getMultiPlantList(): List<Plant> {
        val plants = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, ""))
        return plants
    }

    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> UIUtils.showErrorDialog(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        hideLoading()
        data?.data?.let {
            mtnsList = it.mtns as MutableList<VegaReceivingMtn>
            productList = it.batchDetails as MutableList<VegaReceivingMtnLots>
            warehouselist = it.storageLocationLst as MutableList<VegaSupplyStorageLocation>
        }
    }

    private fun showProcurementDialogDialog(procurementTypeList: ArrayList<String>) {
        //procurementTypeList as MutableList<String>
        MaterialDialog(requireContext()).show {
            title(R.string.tittle_procurement_type)
            listItemsSingleChoice(items = procurementTypeList) { _, index, text ->
                binding.tvProcurementValue.text = text
                gateEntryData.procurementType = binding.tvProcurementValue.text.toString().trim()
                if (gateEntryData.procurementType.equals(PR) || gateEntryData.procurementType.equals(
                        PX
                    ) || gateEntryData.procurementType.equals(
                        DR
                    )
                ) {
                    gateEntryData.grnModel = GRN_VALUE
                } else {
                    gateEntryData.grnModel = Z01
                }
                if (gateEntryData.procurementType.equals(DD) || gateEntryData.procurementType.equals(
                        DX
                    )
                ) {
                    binding.tvOpenGrntLabel.visibility = View.VISIBLE
                    binding.tvOpenGrntValue.visibility = View.VISIBLE
                } else {
                    binding.tvOpenGrntLabel.visibility = View.GONE
                    binding.tvOpenGrntValue.visibility = View.GONE
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showOpenGrntDialog(openGrntTypeList: ArrayList<String>) {
        //openGrntTypeList as MutableList<String>
        MaterialDialog(requireContext()).show {
            title(R.string.tittle_open_grnt_type)
            listItemsSingleChoice(items = openGrntTypeList) { _, index, text ->
                binding.tvOpenGrntValue.text = text
                gateEntryData.grntNumber = binding.tvOpenGrntValue.text.toString().trim()
                /* gateEntryData.procurementType = binding.tvProcurementValue.text.toString().trim()
                 if(gateEntryData.procurementType.equals(PR)){
                     gateEntryData.grnModel = GRN_VALUE
                 }else {
                     gateEntryData.grnModel = Z01
                 }*/
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showStoDialog(mtnsList: MutableList<VegaReceivingMtn>) {
        val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
        val mtnNumbers = data.map { item -> item.mtnNumber }
        MaterialDialog(requireContext()).show {
            title(R.string.tittle_odb_popup)
            listItemsSingleChoice(items = mtnNumbers) { _, index, text ->
                binding.tvObdNumber.text = text
                val mtn = data[index]
                gateEntryData.mtnCode = mtn.mtnNumber
                gateEntryData.delivery = mtn.mtnNumber
                gateEntryData.deliveryItem = mtn.posnr

                val batchList = productList.filter { it.mtnNumber == mtn.mtnNumber }
                if (batchList.size > 0) {
                    gateEntryData.batchNumber = batchList[0].batch
                    gateEntryData.purchaseDocNum = batchList[0].purchaseOrder
                    gateEntryData.purchaseDocDesc = batchList[0].ebelp
                }


            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showReceivingLocationDialog(it: List<VegaCustomStLocation>) {
        val location = it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.receiving_location_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvReceivingLocation.text = text
                gateEntryData.storageLocationCode = it[index].procureLocationCode
                gateEntryData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showPlantSelectionDialog(it: List<Plant>) {
        val location = it.map { data -> data.plantId.plus(" - ").plus(data.plantName) }
        MaterialDialog(requireContext()).show {
            title(R.string.plant_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                binding.tvPlant.text = text
                gateEntryData.plantId = (((text.toString()).split("-"))[0]).trim()
                gateEntryData.plantName = (((text.toString()).split("-"))[1]).trim()
//                gateEntryData.storageLocationName = it[index].procureLocationName
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun showDispatchWHDialog(it: List<VegaSupplyStorageLocation>) {
        val warehouses = it.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

        MaterialDialog(requireContext()).show {
            title(R.string.trans_Warehouse_popup)
            listItemsSingleChoice(items = warehouses) { _, index, text ->
                binding.tvDispatchWarehouse.text = text
                binding.tvObdNumber.text = ""
                val item = it[index].storageLocationCode
                selectedStorageLoc = item
                gateEntryData.plantId = UIUtils.getWarehouseId().toString()
                gateEntryData.supplierCode = it[index].plant
                gateEntryData.supplierName = text.toString()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }


    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "MM/dd/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    binding.tvDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

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
    private fun validateInputs() {
        if (checkPermission()) {
            //  Toast.makeText(context, "Permission already granted.", Toast.LENGTH_LONG).show();

            when {
                gateEntryData.materialCode.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
                binding.tvProduct.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_product))
                binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_date))
                binding.tvReceivingLocation.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_receiving_location))
                binding.tvTruckNo.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_truck_no))
                binding.tvDriverName.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_driver_name))
                binding.tvProcurementValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_procurement_type))
                binding.tvNoOfBagsValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_no_of_bags))
                //binding.tvOpenGrntValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_open_grnt_type))
                binding.tvOpenGrntValue.isVisible && binding.tvOpenGrntValue.text.isNullOrEmpty() -> showSnack(
                    getString(R.string.error_open_grnt_type)
                )

                // binding.tvMtntNoValue.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_mtnt_no))
                //binding.etWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_weight))
                else -> {
                    if (gateEntryData.weighBridgeType == PROCURE) {
                        when {
                            binding.tvSupplier.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier))
//                        binding.tvSupplierZone.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_supplier_zone))
                            imageFilePath.isNullOrEmpty() -> {
                                showSnack(getString(R.string.error_valid_ticket_photo))
                            }
                            (imageList.size == 0) -> {
                                showSnack(getString(R.string.error_valid_ticket_photo))
                            }
                            else -> moveToSummary()
                        }

                    } else if (gateEntryData.weighBridgeType == STO) {
                        when {
                            binding.tvDispatchWarehouse.text.isNullOrEmpty() -> showSnack(
                                getString(
                                    R.string.error_valid_dispatch_wh
                                )
                            )
                            binding.tvObdNumber.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_obd_no))
                            imageFilePath.isNullOrEmpty() -> {
                                showSnack(getString(R.string.error_valid_ticket_photo))
                            }
                            (imageList.size == 0) -> {
                                showSnack(getString(R.string.error_valid_ticket_photo))
                            }
                            else -> moveToSummary()
                        }
                    } else moveToSummary()

                }
            }
        } else {
            //Toast.makeText(context, "Please request permission.", Toast.LENGTH_LONG).show();
            requestPermission()
        }
    }

    private fun moveToSummary() {
        isSummaryEnabled = true
        if (AppUtils.isOnline()) vm.getWaitingTruckList(selectedPlantId)
    }

    private fun createPdf() {

        val cw = ContextWrapper(context)
        val directory_path = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val file1 = File(directory_path, "UniqueFileName" + ".jpg")

        /* val directory_path =
             Environment.getExternalStorageDirectory().getAbsolutePath() + '/';
 */
        //val file1 = File(directory_path)
        /* if (!file1.exists()) {
             file1.mkdirs()
         }*/
        // val targetPdf = directory_path + "receipt.pdf"
        //val filePath = File(targetPdf)
        try {
            if (file1.exists()) {
            } else {
                file1.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file1)
            val pdfDocument = PdfDocument()
            for (i in 0 until imageList.size) {
                val bitmap = BitmapFactory.decodeFile(imageList.get(i))
                //  binding.ivSortDownUp.visibility = View.VISIBLE
                // binding.ivSortDownUp.setImageBitmap(bitmap)


                val pageInfo = PageInfo.Builder(bitmap.width, bitmap.height, i + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.color = Color.BLUE
                canvas.drawPaint(paint)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
                bitmap.recycle()
            }
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()

            /* val path: Uri = Uri.fromFile(filePath)
        // Setting the intent for pdf reader
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(path, "application/pdf")
        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP*/
            try {
                var encodeFileToBase64Binary = encodeFileToBase64Binary(file1)
                gateEntryData.imageString = encodeFileToBase64Binary
                var plantDetails = getPlantDetails(selectedPlantId).single()
                hideLoading()

                /* val imageBytes = Base64.decode(encodeFileToBase64Binary, Base64.NO_WRAP)
                 val decodedImage: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                 binding.ivSortDownUpNew.visible()
                 binding.ivSortDownUpNew.setImageBitmap(decodedImage)*/
                callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData, plantDetails)
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

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    private fun updateMandatory() {
        binding.tvProductLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.product)) { mandatoryStars() } }
        binding.tvSupplierLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.supplier)) { mandatoryStars() } }
        binding.tvPlantLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.plant)) { mandatoryStars() } }
        binding.tvNoOfBagsLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.tentative_no_of_bags)) { mandatoryStars() } }
        binding.tvDispatchWHLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.dispatch_wh)) { mandatoryStars() } }
        binding.tvObdLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.mtn_obd_number)) { mandatoryStars() } }
        binding.tvLocationLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.receiving_location)) { mandatoryStars() } }
        binding.tvDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.arrival_date)) { mandatoryStars() } }
        binding.tvProcurementLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.procurement_type)) { mandatoryStars() } }
        binding.tvDriverNameLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.driver_name)) { mandatoryStars() } }
        binding.tvTruckNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_no)) { mandatoryStars() } }
    }

    private fun createSingleImageFromMultipleImages(imageList: ArrayList<String>): Bitmap? {

        var newBitmap: Bitmap? = null
        try {
            var firstimage = File(imageList[0])
            val bmOptions = BitmapFactory.Options()

            var firstbitmap = BitmapFactory.decodeFile(firstimage.absolutePath, bmOptions)

            var mergedheight = 0
            var width = firstbitmap.width

            for(i in imageList){

                val image = File(i)
                val bmOptions = BitmapFactory.Options()
                var bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)

                var imgFile = File(i)
                if(imgFile.exists()) {
                    var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    mergedheight = mergedheight + myBitmap.height
                }
            }

            var config: Bitmap.Config = firstbitmap.config
            if (config == null) {
                config = Bitmap.Config.ARGB_8888
            }

            var newBitmap = Bitmap.createBitmap(200, 200, config)
            var newCanvas = Canvas(newBitmap as Bitmap)

            for (i in imageList) {
                var myBitmap = BitmapFactory.decodeFile(File(i).absolutePath)
                newCanvas.drawBitmap(myBitmap, 0f, myBitmap.height.toFloat(), null)
            }

        } catch (e: FileNotFoundException) {

            e.printStackTrace()
        }

        return newBitmap


//        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
//        val canvas = Canvas(result)
//        canvas.drawBitmap(firstImage, 0f, 0f, null)
//        canvas.drawBitmap(secondImage, 10f, 10f, null)
//        return result
    }

    fun bitmapToFile(bitmap: Bitmap?, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(fileNameToSave, ".jpg", storageDir)

            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(imageFile)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }
}
