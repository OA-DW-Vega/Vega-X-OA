package com.olam.warehouse.vegax.gateentrynigeria.ui

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.veganigeria.model.VegaNigeriaCocoPortPlantIdModel
import com.olam.warehouse.master.veganigeria.utils.PORT_PLANTLIST
import com.olam.warehouse.master.veganigeria.utils.portPlantIdList
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeMillis
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
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


class VegaNigeriaAddNewTruckFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private var productNameList: List<VegaMaterial> = emptyList()
    private var suppliersList: List<VegaVendor> = emptyList()
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
    private val RECORD_REQUEST_CODE_WRITE = 103
    private val RECORD_REQUEST_CODE_READ_WRITE = 102
    private var vegaWbIds = listOf<VegaGateEntry>()
    private var imageList = ArrayList<String>()
    private var imageListForDriverTruckDetails = ArrayList<String>()
    private var imagePathDriverLicenseNo = ""
    private var imagePathWayBillNo = ""
    private var fromClick = "0"
    private var jsonData = mutableListOf<String>()
    private var procurementTypeList = ArrayList<String>()
    private var openGrntTypeList = ArrayList<String>()
    private var gateEntryOrderList = mutableListOf<VegaNigeriaGateEntryPostData>()
    private var grntOrderList = ArrayList<VegaNigeriaGateEntryPostData>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null


    //private var gateEntryOrderList = ArrayList<VegaNigeriaGateEntryPostData>()
    private var procurementType: String? = ""
    private var selectedPlantId = ""
    var image: Image? = null
    private var pdfFile: File? = null
    private var isSummaryEnabled: Boolean? = false
    var portPlantsList = mutableListOf<String>()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    private fun initUI() {

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())



        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))

        //vm.waitingTrucks1.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                constructData()
               // if (AppUtils.isOnline()) vm.getWaitingTruckList(selectedPlantId)
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)


       try {
           gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
           arguments?.clear()
       }catch (e:Exception){
           e.printStackTrace()
       }


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
            productNameList= it
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
        })
        vm.getProducts()

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            suppliersList=it
        })

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
            /*commented below things due to logic change in loading receiving location*/
            /*if (receivingLocationList.size == 1) {
                binding.tvReceivingLocation.text =
                        custonLocationList[0].procureLocationCode.plus(" - ")
                                .plus(custonLocationList[0].procureLocationName)
                gateEntryData.storageLocationCode = custonLocationList[0].procureLocationCode
                gateEntryData.storageLocationName = custonLocationList[0].procureLocationName
            }*/

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


        binding.tvProduct.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_product), PRODUCT)
        }

        binding.tvSupplier.setOnClickListener {
            showSingleSelectDialog(getString(R.string.select_supplier), SUPPLIER)
        }

        binding.tvPlant.setOnClickListener {
            if (plantList.size >= 1) {
                showSingleSelectDialog(getString(R.string.plant_popup), PLANT)
//                binding.tvReceivingLocation.text = ""
            }
        }

        if (AppUtils.isOnline() && gateEntryData.weighBridgeType == STO) {
            fetchMtnDetails()
        }
        binding.tvObdNumber.setOnClickListener {
            showSingleSelectDialog(getString(R.string.tittle_odb_popup),STO)
        }

        binding.tvProcurementValue.setOnClickListener {
            showSingleSelectDialog(getString(R.string.tittle_procurement_type),"")
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
        binding.tvDispatchWarehouse.setOnClickListener {
            showSingleSelectDialog(getString(R.string.trans_Warehouse_popup), WAREHOUSE)
        }
        binding.tvReceivingLocation.setOnClickListener {
            if(selectedPlantId.isNotEmpty()) {
                showSingleSelectDialog(getString(R.string.receiving_location_popup), RECEIVING_LOCATION)
            } else {
                context?.toast(getString(R.string.plant_id_warning_msg))
            }
            /*if (binding.tvPlant.text != "") {
                var receivingLocationList = custonLocationList.filter {
                    !it.storageLocationType.equals(
                            "B"
                    )
                }.filter { it.plant == gateEntryData.plantId }
                if (receivingLocationList.isNotEmpty()) {
                    showSingleSelectDialog(getString(R.string.receiving_location_popup), RECEIVING_LOCATION)
                }
            }*/
        }

        imageFilePath = gateEntryData.imagePath.toString()
        if (imageFilePath.isNotEmpty()) updateCameraLayout()
        //  binding.ivCamera.setOnClickListener { setupPermissions() }

        binding.llCamera.setOnClickListener {
            if (imageList.size > 5 && imageList.size == 6) {
                Toast.makeText(context, "Only Six Images can be uploaded ", Toast.LENGTH_SHORT)
                    .show()
            } else {
                setupPermissions()
            }
        }
        if(isNGCashewEnabled()){
            setupCashew()
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

    private fun setupCashew() {
            binding.llParentCameraCashew.visibility=View.VISIBLE
            binding.llCamera.visibility=View.GONE
            binding.tvPhoneNo.filters=arrayOf(InputFilter.LengthFilter(11))

        binding.llCameraCashPictureOne.setOnClickListener {
            if (imageListForDriverTruckDetails.size > 2 && imageList.size == 3) {
                Toast.makeText(context, "Only Three Images can be uploaded ", Toast.LENGTH_SHORT)
                    .show()
            } else {
                fromClick="1"
                setupPermissions()
            }

        }
        binding.llCameraCashPictureTwo.setOnClickListener {
            if (imagePathDriverLicenseNo.isNotEmpty()) {
                Toast.makeText(context, "Only one Image can be uploaded ", Toast.LENGTH_SHORT)
                    .show()
            }else{
                fromClick="2"
                setupPermissions()
            }

        }
        binding.llCameraCashPictureThree.setOnClickListener {
            if (imagePathWayBillNo.isNotEmpty()) {
                Toast.makeText(context, "Only one Image can be uploaded ", Toast.LENGTH_SHORT)
                    .show()
            }else{
                fromClick="3"
                setupPermissions()
            }

        }
        binding.closeDialogCash.setOnClickListener {
            binding.photoImageViewCash.setImageBitmap(null)
            binding.photoImageViewCash.visibility = View.GONE
            binding.closeDialogCash.visibility = View.GONE
            if (imageListForDriverTruckDetails.size > 0) {
                imageListForDriverTruckDetails.remove(imageListForDriverTruckDetails[0])
            }
        }
        binding.closeDialogCash1.setOnClickListener {
            binding.photoImageViewCash1.setImageBitmap(null)
            binding.photoImageViewCash1.visibility = View.GONE
            binding.closeDialogCash1.visibility = View.GONE
            if (imageListForDriverTruckDetails.size > 1) {
                imageListForDriverTruckDetails.remove(imageListForDriverTruckDetails[1])
            }
        }
        binding.closeDialogCash2.setOnClickListener {
            binding.photoImageViewCash2.setImageBitmap(null)
            binding.photoImageViewCash2.visibility = View.GONE
            binding.closeDialogCash2.visibility = View.GONE
            if (imageListForDriverTruckDetails.size > 2) {
                imageListForDriverTruckDetails.remove(imageListForDriverTruckDetails[2])
            }
        }
        binding.closeDialogWaybillNOCash.setOnClickListener {
            binding.photoImageViewWaybillNOCash.setImageBitmap(null)
            binding.photoImageViewWaybillNOCash.visibility = View.GONE
            binding.closeDialogWaybillNOCash.visibility = View.GONE
            if (imagePathWayBillNo.isNotEmpty()) {
                imagePathWayBillNo=""
            }
        }
        binding.closeDialogLicNOCash.setOnClickListener {
            binding.photoImageViewLicNOCash.setImageBitmap(null)
            binding.photoImageViewLicNOCash.visibility = View.GONE
            binding.closeDialogLicNOCash.visibility = View.GONE
            if (imagePathWayBillNo.isNotEmpty()) {
                imagePathDriverLicenseNo=""
            }
        }

    }

    private fun setupPermissions() {
        //val permission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
       // if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()
        if(!hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) setupAllPermissions(REQUIRED_CAMERA_PERMISSIONS, object:PermissionObserve{
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
   /* override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

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
                        // gateEntryData.wsGate = WS01
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
                        binding.btnConfirm.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
                        binding.linearOne.visibility = View.VISIBLE
                        vm.getSuppliers(selectedPlantId)
                    }
                    // }
                    //}
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), response.error.toString())
                }
            }
        }

    }

    private fun constructData(){
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
            // gateEntryData.wsGate = WS01
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
            binding.btnConfirm.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            binding.linearOne.visibility = View.VISIBLE
            vm.getSuppliers(selectedPlantId)
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
                        binding.tvReceivingLocation.text = ""
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
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun updateProcessType(miscellaneous: List<VegaCocoaMiscellaneous>) {
        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
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
            } else if (it.contains(PORT_PLANTLIST)) {
                portPlantsList.clear()
                val plantId = gson.fromJson(it, VegaNigeriaCocoPortPlantIdModel::class.java)
                portPlantsList.addAll(plantId.PORT_PLANTLIST)
                portPlantIdList = portPlantsList as ArrayList<String>
            }
        }
    }

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val fileSize = imageFile.length()
            println("fileSize:"+fileSize)

            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = requireActivity().packageName + ".fileprovider"
                try {
                    val imageUri =
                        activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                    callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
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
       try {
           when (requestCode) {
               CAMERA_REQUEST_CODE -> {
                   if (resultCode == Activity.RESULT_OK) {
                       updateCameraLayout()
                   } else {
                       imageList.removeAt(imageList.size - 1)
                       imageFilePath = ""
                   }
               }
               else -> {
                   showErrorDialogWithFAQLink(requireContext(), "Unrecognized request code")
                   //activity?.toast("Unrecognized request code")
               }
           }
       }catch (e:Exception){
           e.printStackTrace()
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
        if(fromClick.equals("1")){
            imageListForDriverTruckDetails.add(imageFilePath)
        }else if(fromClick.equals("2")){
            imagePathDriverLicenseNo=imageFilePath
        }else if(fromClick.equals("3")){
            imagePathWayBillNo=imageFilePath
        }

        return imageFile
    }

    private fun updateCameraLayout() {
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
            if(imageList.size>3 && imageList.size ==4){
                binding.photoImageView3.visible()
                binding.closeDialog3.visible()
                binding.photoImageView3.setImageBitmap(setScaledBitmap(imageList[3]))
            }
            if(imageList.size>4 && imageList.size ==5){
                binding.photoImageView4.visible()
                binding.closeDialog4.visible()
                binding.photoImageView4.setImageBitmap(setScaledBitmap(imageList[4]))
            }
            if(imageList.size>5 && imageList.size ==6){
                binding.photoImageView5.visible()
                binding.closeDialog5.visible()
                binding.photoImageView5.setImageBitmap(setScaledBitmap(imageList[5]))
            }
        }
        if(imageListForDriverTruckDetails.size>0){
            binding.photoImageViewCash.visible()
            binding.closeDialogCash.visible()
            binding.photoImageViewCash.setImageBitmap(setScaledBitmap(imageListForDriverTruckDetails[0]))
            if (imageListForDriverTruckDetails.size > 1 && imageListForDriverTruckDetails.size >= 2) {
                binding.photoImageViewCash1.visible()
                binding.closeDialogCash1.visible()
                binding.photoImageViewCash1.setImageBitmap(setScaledBitmap(imageListForDriverTruckDetails[1]))
            }
            if (imageListForDriverTruckDetails.size > 2 && imageListForDriverTruckDetails.size == 3) {
                binding.photoImageViewCash2.visible()
                binding.closeDialogCash2.visible()
                binding.photoImageViewCash2.setImageBitmap(setScaledBitmap(imageListForDriverTruckDetails[2]))
            }
        }
        if(imagePathDriverLicenseNo.isNotEmpty()){
            binding.photoImageViewLicNOCash.visible()
            binding.closeDialogLicNOCash.visible()
            binding.photoImageViewLicNOCash.setImageBitmap(setScaledBitmap(imagePathDriverLicenseNo))

        }
        if(imagePathWayBillNo.isNotEmpty()){
            binding.photoImageViewWaybillNOCash.visible()
            binding.closeDialogWaybillNOCash.visible()
            binding.photoImageViewWaybillNOCash.setImageBitmap(setScaledBitmap(imagePathWayBillNo))

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

            val scaleFactor =calculateInSampleSize(bmOptions,imageViewWidth,imageViewHeight)
            //Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

            bmOptions.inSampleSize = scaleFactor
            bmOptions.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(image, bmOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

   private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
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
                    WRITE_EXTERNAL_STORAGE
            )
        }
        val readStoragepermission = activity?.let {
            ContextCompat.checkSelfPermission(
                    it,
                    READ_EXTERNAL_STORAGE
            )
        }
        return writeStoragepermission == PackageManager.PERMISSION_GRANTED && readStoragepermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
                arrayOf(
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE
                ),
                RECORD_REQUEST_CODE_READ_WRITE
        )
    }
    private fun validateInputs() {
        if (hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) {
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
                isNGCashewEnabled() && imageListForDriverTruckDetails.size==0 -> showSnack("Upload valid Photo")
                isNGCashewEnabled() && imagePathDriverLicenseNo.isNullOrEmpty() -> showSnack("Upload valid Driver License Photo")

                isNGCashewEnabled() && imagePathWayBillNo.isNullOrEmpty() -> showSnack("Upload valid Waybill Photo")
                isNGCashewEnabled() && binding.tvPhoneNo.text.isNullOrEmpty() -> showSnack("Enter Driver Mobile Number")


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
            setupAllPermissions(REQUIRED_CAMERA_PERMISSIONS, object: PermissionObserve {
                override fun grandValue(grandValue: Boolean) {
                    if(grandValue) moveToCameraView()
                }
            })
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        val TAG = "Storage Permission"
        return if (Build.VERSION.SDK_INT >= 23) {
            val permission = activity?.let { ContextCompat.checkSelfPermission(it, WRITE_EXTERNAL_STORAGE) }
            if (permission == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                true
            } else {
                Log.v(TAG, "Permission is revoked")
                requestPermissions(
                        arrayOf(WRITE_EXTERNAL_STORAGE),
                        RECORD_REQUEST_CODE_WRITE
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }
    }

    private fun moveToSummary() {
        isSummaryEnabled = true
        constructData()
       // if (AppUtils.isOnline()) vm.getWaitingTruckList(selectedPlantId)
    }


    private fun createPdf() {
        try {
            if (hasPermissions(this.requireContext(), *REQUIRED_CAMERA_PERMISSIONS)) {
                val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                if (!storageDir!!.exists()) storageDir.mkdirs()
                val imageFile = File.createTempFile("UniqueFileName", ".png", storageDir)
                if (imageFile.exists()) {
                } else {
                    imageFile.createNewFile()
                }
                val fileOutputStream = FileOutputStream(imageFile)
                val pdfDocument = PdfDocument()
                for (i in 0 until imageList.size) {
                    val Options = BitmapFactory.Options()
                    Options.inJustDecodeBounds = true

                    BitmapFactory.decodeFile(imageList.get(i), Options)
                    val scaleFactor = calculateInSampleSize(Options, 200, 200)
                    Options.inSampleSize = scaleFactor
                    Options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeFile(imageList.get(i), Options)
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        bitmap, 200, 200,
                        false
                    )
                    val pageInfo = PageInfo.Builder(200, 200, i + 1).create()
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
                    gateEntryData.imageString = encodeFileToBase64Binary
                    var plantDetails = getPlantDetails(selectedPlantId).single()
                    hideLoading()

                    callBack?.replaceFragment(SUMMARY_FRAG, gateEntryData, plantDetails)

                } catch (e: ActivityNotFoundException) {
                    hideLoading()
                    Toast.makeText(context, "Can't read pdf file", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            hideLoading()
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun encodeFileToBase64Binary(yourFile: File): String {
        val size = yourFile.length().toInt()
        println("baseSize:${size}")
        val bytes = ByteArray(size)
        println("baseSize1:${bytes.size}")
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
        if(isNGCashewEnabled()){
            binding.tvDriverNoLabel.text =
                with(UIUtils) { with(requireContext().resources.getString(R.string.driver_no)) { mandatoryStars() } }
        }
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
            val imageFile = File.createTempFile(fileNameToSave, ".png", storageDir)

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

    private fun showSingleSelectDialog(title: String,currentFalg: String) {
        val list: List<String>

        when(currentFalg) {
            PRODUCT -> {
                list=productNameList.map { data ->data.materialCode.plus("&&").plus(data.materialName!!) }
            }
            SUPPLIER->{
                list = suppliersList.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
            }
            PLANT->{
                list = plantList.map { data -> data.plantId.plus(" - ").plus(data.plantName) }
            }
            RECEIVING_LOCATION-> {
                val plant = plantList.filter { data -> data.plantId.equals(selectedPlantId)}.single()
                list = plant.storageLocation.map { data -> data.storageLocationCode.plus(" - ").plus(data.storageLocationName) }
                /*val receivingLocationList = custonLocationList.filter {
                    !it.storageLocationType.equals(
                        "B"
                    )
                }.filter { it.plant == gateEntryData.plantId }

                list = receivingLocationList.map { data ->
                    data.procureLocationCode.plus(" - ").plus(data.procureLocationName)
                }*/
            }
            WAREHOUSE->{
                list = warehouselist.map { data -> data.storageLocationCode.plus("-").plus(data.storageLocationName) }

            }STO->{
                val data = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }
                list = data.map { item -> item.mtnNumber }

           }
            else->{

              list= procurementTypeList
            }
        }
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when(currentFlag) {
            PRODUCT -> {
                binding.tvProduct.text= data
                productNameList.forEach { material ->
                    if (material.materialCode == binding.tvProduct.text.toString().split("&&")[0]) {
                        gateEntryData.materialCode =material.materialCode.toString()
                                // MATERIAL_CODE.plus(material.materialCode.toString())
                        gateEntryData.materialName = material.materialName.toString()
                        binding.tvUom.text = material.unitsOfMeasure.toString()
                        gateEntryData.unitsOfMeasure = material.unitsOfMeasure.toString()
                    }
                }

            }
            SUPPLIER->{
                binding.tvSupplier.text= data
                suppliersList.forEach { vendor ->
                    if (vendor.vendorCode == binding.tvSupplier.text.toString().split(" - ")[0]) {
                        gateEntryData.supplierCode = vendor.vendorCode
                        gateEntryData.supplierName = vendor.vendorName
                        vendor.bcApprover?.let { it1 -> vm.getSupplierZone(it1) }
                    }
                }

            }
            PLANT->{
                binding.tvPlant.text = data
                gateEntryData.plantId = data.split("-")[0].trim()
                gateEntryData.plantName = data.split("-")[1].trim()

            }
            RECEIVING_LOCATION->{
                binding.tvReceivingLocation.text = data
                gateEntryData.storageLocationCode = data.split("-")[0].trim()
                gateEntryData.storageLocationName = data.split("-")[1].trim()
            }
            WAREHOUSE->{
                binding.tvDispatchWarehouse.text = data
                warehouselist.forEach {
                    if(it.storageLocationCode== data.split("-")[0].trim()){
                        binding.tvObdNumber.text = ""
                        val item = it.storageLocationCode
                        selectedStorageLoc = item
                        gateEntryData.plantId = UIUtils.getWarehouseId()
                        gateEntryData.supplierCode = it.plant
                        gateEntryData.supplierName = data
                        return
                    }
                }
            }
            STO->{
                binding.tvObdNumber.text = data

                val mtList = mtnsList.distinct().filter { it.storageLocationCode == selectedStorageLoc }

                mtList.forEach {
                    if(it.mtnNumber == data.trim()){
                        val mtn = it
                        gateEntryData.mtnCode = mtn.mtnNumber
                        gateEntryData.delivery = mtn.mtnNumber
                        gateEntryData.deliveryItem = mtn.posnr

                        val batchList = productList.filter { it.mtnNumber == mtn.mtnNumber }
                        if (batchList.isNotEmpty()) {
                            gateEntryData.batchNumber = batchList[0].batch
                            gateEntryData.purchaseDocNum = batchList[0].purchaseOrder
                            gateEntryData.purchaseDocDesc = batchList[0].ebelp
                        }
                        return
                    }
                }
            }
            else->{
            binding.tvProcurementValue.text = data
            gateEntryData.procurementType = binding.tvProcurementValue.text.toString().trim()
            if (gateEntryData.procurementType.equals(PR) || gateEntryData.procurementType.equals(
                    PX
                ) || gateEntryData.procurementType.equals(
                    DIS
                )
            ) {
                gateEntryData.grnModel = GRN_VALUE
            } else {
                gateEntryData.grnModel = GRN_VALUE
            }
            if (gateEntryData.procurementType.equals(DD) || gateEntryData.procurementType.equals(
                    DX
                )
            ) {
                binding.tvOpenGrntLabel.visibility = View.VISIBLE
                binding.tvOpenGrntValue.visibility = View.VISIBLE
                /*TODO-NG-CASH once GRN-T scope sprint came it need to be remove*/
                if(isNGCashewEnabled()){
                    binding.tvOpenGrntLabel.visibility = View.GONE
                    binding.tvOpenGrntValue.visibility = View.GONE
                }
            } else {
                binding.tvOpenGrntLabel.visibility = View.GONE
                binding.tvOpenGrntValue.visibility = View.GONE
            }
            }
        }
    }
}
