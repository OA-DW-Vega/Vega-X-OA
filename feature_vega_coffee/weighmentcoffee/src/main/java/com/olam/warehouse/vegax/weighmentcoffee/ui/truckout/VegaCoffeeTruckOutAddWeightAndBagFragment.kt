package com.olam.warehouse.vegax.weighmentcoffee.ui.truckout

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.master.vega.model.VegaCoffeeTruckOutReceivingWithBags
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCoffeeSingleSelectListener
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_DATA
import com.olam.warehouse.presentation.utils.UIUtils.RECEIVING_POST_DATA
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeTruckoutAddWeightBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeMtnrSupplierViewModel
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException
import kotlin.random.Random

class VegaCoffeeTruckOutAddWeightAndBagFragment : BaseFragment(), VegaSingleSelectListener,
    VegaCoffeeSingleSelectListener {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var currentImagePath: String? = ""
    private val CAMERA_REQUEST_CODE = 0
    private var imageFilePath: String = ""
    private val RECORD_REQUEST_CODE = 101
    private var wareHouseList: MutableList<VegaSupplyStorageLocation> = mutableListOf()
    private var localDBData: VegaCoffeeTruckOutReceivingWithBags? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var OriginList = mutableListOf<VegaQualitative>()
    private var Departmentlist = mutableListOf<VegaQualitative>()
    private var materialCode: String = ""

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaCoffeeMtnrSupplierViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffeeTruckoutAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_coffee_truckout_add_weight

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            receiving: ArrayList<VegaReceiving>
        ) = VegaCoffeeTruckOutAddWeightAndBagFragment().putArgs {
            putParcelable(RECEIVING_DATA, receivingData)
            putParcelableArrayList(RECEIVING_POST_DATA, receiving)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeTruckoutAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("weighmentcoffee/ui/truckout/VegaCoffeeTruckOutAddWeightAndBagFragment")
            .title("IVC/Coffee/Weighment/Truck Out Add Weight and Bag")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(RECEIVING_POST_DATA)!!
        /*vm.getTruckOutWithBags(receivingData.weighBridgeId)
        vm.truckOutModel.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)
            } else {
                localDBData = it
                updateLocalDBData(it)
            }
        })*/
        vm.getLocations()
        binding.tvTruckOutLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_tar_weight)) { mandatoryStars() } }
        binding.ivCamere.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.upload_ticket_photo)) { mandatoryStars() } }
        binding.tvBagDetails.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_details)) { mandatoryStars() } }
binding.tvTruckInBagLabel.text=
        with(UIUtils) { with(requireContext().resources.getString(R.string.declared_bag_count)) { mandatoryStars() } }
        binding.tvTruckDeclaredweightLabel.text=
        with(UIUtils) { with(requireContext().resources.getString(R.string.declared_weight)) { mandatoryStars() } }
        binding.tvOriginLabel.text=with(UIUtils) { with(requireContext().resources.getString(R.string.origin)) { mandatoryStars() } }
        binding.tvDestinationLabel.text=with(UIUtils) { with(requireContext().resources.getString(R.string.destination)) { mandatoryStars() } }
        binding.tvConnaissementLabel.text=with(UIUtils) { with(requireContext().resources.getString(R.string.connaissement)) { mandatoryStars() } }
        binding.tvUom.text = receivingData.unitsOfMeasure
        binding.tvTruckNo.text = receivingData.vehicleNumber
        binding.tvWeighBridgeId.text = receivingData.weighBridgeId
        binding.etTruckTarWeight.setText(
            if (receivingData.tareWeight.equals("0.000") || receivingData.tareWeight.equals("0")) "" else receivingData.tareWeight,
            TextView.BufferType.EDITABLE
        )
        binding.tvDispatchWh.text = receivingData.dstorageLocationName
        binding.tvDriverName.text = receivingData.truckDriverName
        binding.tvDriverPhone.text = receivingData.phoneNo
        binding.tvReceiveWh.text = receivingData.receivingWH

        imageFilePath = receivingData.imagePath.toString()
        if (imageFilePath.isNotEmpty()) updateCameraLayout()
        mReceiving.forEachIndexed { index, vegaReceiving ->
            when (index) {
                0 -> {
                    binding.tvBagType1.text = vegaReceiving.bagType
                    binding.etBagCount1.setText(vegaReceiving.bagCount)
                }
                1 -> {
                    binding.tvBagType2.text = vegaReceiving.bagType
                    binding.etBagCount2.setText(vegaReceiving.bagCount)
                }
                2 -> {
                    binding.tvBagType3.text = vegaReceiving.bagType
                    binding.etBagCount3.setText(vegaReceiving.bagCount)
                }
            }
        }
        binding.tvOrigin.setOnClickListener {
            showSingleSelectDialog(
                    false,
                    getString(R.string.select_origin),
                    false, false,true,false
            )
        }
        binding.tvDestinationLocation.setOnClickListener {
            showSingleSelectDialog(
                    false,
                    getString(R.string.select_Destination),
                    false, false,false,true
            )
        }
        if (receivingData.materialCode?.length != 18)
            materialCode=  "000000".plus(receivingData.materialCode)
        else
            materialCode= receivingData.materialCode!!
        vm.getQualityParams(materialCode, false,receivingData.weighBridgeId )
        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateoriginlist(it) })
        vm.location.observe(viewLifecycleOwner, Observer { wareHouseList = it.toMutableList() })
        when (receivingData.weighBridgeType) {
            PROCURE -> {
                binding.tvdifference.text = SUPPLIER
                binding.llOriginLayout.visibility=View.VISIBLE
                binding.llDepartmentLayout.visibility=View.VISIBLE
                binding.tvTruckInBagLabel.visibility=View.VISIBLE
                binding.llTruckbagcontainerID.visibility=View.VISIBLE
                binding.tvTruckDeclaredweightLabel.visibility=View.VISIBLE
                binding.llTruckDeclaredweight.visibility=View.VISIBLE
                binding.tvTransportLabel.visibility=View.VISIBLE
            }
            else -> {
                receivingData.supplierCode = receivingData.customerNum
                binding.tvdifference.visibility = View.GONE
                binding.tvSupplierName.visibility = View.GONE
                binding.tvdifference.text = WAREHOUSE
                binding.llOriginLayout.visibility=View.GONE
                binding.llDepartmentLayout.visibility=View.GONE
                binding.tvTruckInBagLabel.visibility=View.GONE
                binding.llTruckbagcontainerID.visibility=View.GONE
                binding.tvTruckDeclaredweightLabel.visibility=View.GONE
                binding.llTruckDeclaredweight.visibility=View.GONE
                binding.tvTransportLabel.visibility=View.GONE
            }
        }
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ").plus(receivingData.vehicleNumber ?: receivingData.weighBridgeId)
        binding.tvSupplierName.text = receivingData.supplierName ?: receivingData.supplierCode
        /*if (receivingData.weighBridgeType == PROCURE) {*/
            binding.tvWeight.text = receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        /*} else {
            binding.tvWeight.text = receivingData.tareWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        }*/
        binding.tvDate.text = receivingData.erdat
        binding.etTruckDeclaredweight.text=receivingData.vendorDeclaredWeight
        binding.etConnaissement.text=receivingData.challan
        binding.etCooperative.setText(receivingData.remarks.toString())
        binding.etTruckBagcount.text=receivingData.bagCount?.trim()
        if(binding.etTruckDeclaredweight.text.isNotEmpty())
            binding.etTruckDeclaredweight.isEnabled = false
        if( binding.etTruckBagcount.text.isNotEmpty())
            binding.etTruckBagcount.isEnabled = false
        if( binding.etConnaissement.text.isNotEmpty())
            binding.etConnaissement.isEnabled = false
        if( binding.etCooperative.text.isNotEmpty())
            binding.etCooperative.isEnabled = false
        val times = receivingData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }

        /*if (receivingData.weighBridgeType == PROCURE) {
            binding.llBagDetails.visible()
        } else {
            binding.llBagDetails.gone()
        }*/
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

        binding.btnProceed.setOnClickListener { validateInputs() }
        binding.ivCamere.setOnClickListener { setupPermissions() }
        binding.llCamera.setOnClickListener { setupPermissions() }

        vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateValueUI(it) })
        vm.getWeighBridgeIdDetail(receivingData.weighBridgeId)

    }
    private fun showSingleSelectDialog(
            isWh: Boolean,
            title: String,
            isVendor: Boolean,
            isPoList: Boolean,
            isOrgin:Boolean,isDepartment:Boolean,
            isYear: Boolean = false
    ) {
        var list = ArrayList<String>()
        if(isOrgin)
        {
            list = OriginList.map {
                it.charValue
            } as ArrayList<String>


        }
        else if (isDepartment)
        {
            list = Departmentlist.map {
                it.charValue
            } as ArrayList<String>


        }

        customDialog =
                VegaCustomSingleSelectDialogWithSearch(
                    title,
                    isWh,
                    isVendor,
                    isPoList,
                    list,
                    requireActivity(),
                    this,
                    isSupplier = isYear,
                    supplierListener = this,
                    isOrigin = isOrgin,
                    isDepartment = isDepartment
                )

        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }
    private fun updateLocalDBData(item: VegaCoffeeTruckOutReceivingWithBags) {
        binding.tvDriverName.text = item.receiving.truckDriverName
        binding.tvDriverPhone.text = item.receiving.contactNumber
        binding.tvTruckNo.text = item.receiving.vehicleNumber
        binding.etTruckBagcount.text = item.receiving.bagCount?.trim()
        binding.etTruckDeclaredweight.text = item.receiving.vendorDeclaredWeight
        binding.etCooperative.setText(item.receiving.remarks.toString())
        binding.etConnaissement.text = item.receiving.challan
        receivingData.transportVendorCode = item.receiving.transportVendorCode
        receivingData.transportVendorName = item.receiving.transportVendorName
        receivingData.dstorageLocationCode = item.receiving.dstorageLocationCode
        receivingData.dstorageLocationName = item.receiving.dstorageLocationName
        receivingData.materialCode = item.receiving.materialCode
        receivingData.materialName = item.receiving.materialName
        receivingData.driverName = item.receiving.driverName
        receivingData.contactNumber = item.receiving.contactNumber
        receivingData.truckNo = item.receiving.truckNo
        receivingData.storageLocationCode = item.receiving.storageLocationCode
        receivingData.storageLocationName = item.receiving.storageLocationName
        binding.tvReceiveWh.text =
            item.receiving.storageLocationCode.plus("-")
                .plus(getLocationName(item.receiving.storageLocationCode))
        binding.tvDispatchWh.text =
            item.receiving.dstorageLocationCode.plus("-").plus(item.receiving.dstorageLocationName)
        mReceiving.clear()
        item.bags.forEach {
            val receiving = receivingData.copy()
            receiving.bagType = binding.tvBagType1.text.toString()
            receiving.bagCount = binding.etBagCount1.text.toString()
            val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
            if (data.isNotEmpty()) receiving.bagTareWeight = data[0].tareWeight
            mReceiving.add(receiving)
        }
        if(binding.etTruckDeclaredweight.text.isNotEmpty())
            binding.etTruckDeclaredweight.isEnabled = false
        if(binding.etCooperative.text.isNotEmpty())
            binding.etCooperative.isEnabled = false
        if(binding.etConnaissement.text.isNotEmpty())
            binding.etConnaissement.isEnabled = false
        if( binding.etTruckBagcount.text.isNotEmpty())
            binding.etTruckBagcount.isEnabled = false

    }


    private fun setupPermissions() {
        /*val permission = activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) }
        if (permission != PackageManager.PERMISSION_GRANTED) makeRequest() else moveToCameraView()*/
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

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissions[0]) }!!

                when (granted) {
                    true -> moveToCameraView()
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
                val imageUri = activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
                callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: IOException) {
            showErrorDialogWithFAQLink(requireContext(), "Could not create file!")
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
                showErrorDialogWithFAQLink(requireContext(), "Unrecognized request code")
                //activity?.toast("Unrecognized request code")
            }
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val imageFileName: String = "JPEG_".plus(binding.tvTruckNo.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    fun updateCameraLayout() {
        binding.ivCamere.text = receivingData.vehicleNumber.plus(".jpg")
        ViewCompat.setBackgroundTintList(
            binding.ivCamere,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.llCamera,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
    }

    fun setScaledBitmap(): Bitmap {
        val imageViewWidth = 347
        val imageViewHeight = 413

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = Math.min(bitmapWidth / imageViewWidth, bitmapHeight / imageViewHeight)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)

    }

    private fun showBagTypeDiaog(bag: Int, it: List<VegaPackageMaterial>) {
        val bagTypes = it.map { data -> data.bagType }
        MaterialDialog(requireContext()).show {
            title(R.string.select_bag_type)
            listItemsSingleChoice(items = bagTypes) { _, index, text ->
                when (bag) {
                    1 -> binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                    2 -> binding.tvBagType2.setText(text, TextView.BufferType.EDITABLE)
                    3 -> binding.tvBagType3.setText(text, TextView.BufferType.EDITABLE)
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.presentation.R.string.ok), true))
        }
    }

    private fun validateInputs() {
        //if (receivingData.weighBridgeType == PROCURE) {
        when {
            binding.etTruckTarWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_tar_weight))
            else -> {
                receivingData.tareWeight = binding.etTruckTarWeight.text.toString()
                if (binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) {
                    val receiving = receivingData.copy()
                    mReceiving.clear()
                    receiving.bagType = binding.tvBagType1.text.toString()
                    receiving.bagCount = binding.etBagCount1.text.toString()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
                    if (data.isNotEmpty()) receiving.bagTareWeight = data[0].tareWeight
                    mReceiving.add(receiving)
                }
                if (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty()) {
                    val receiving1 = receivingData.copy()
                    receiving1.bagType = binding.tvBagType2.text.toString()
                    receiving1.bagCount = binding.etBagCount2.text.toString()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                    if (data.isNotEmpty()) receiving1.bagTareWeight = data[0].tareWeight
                    mReceiving.add(receiving1)
                }
                if (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty()) {
                    val receiving2 = receivingData.copy()
                    receiving2.bagType = binding.tvBagType3.text.toString()
                    receiving2.bagCount = binding.etBagCount3.text.toString()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }
                    if (data.isNotEmpty()) receiving2.bagTareWeight = data[0].tareWeight
                    mReceiving.add(receiving2)
                }
                var isEmptyData = false
                mReceiving.forEach {
                    if (it.bagType!!.isEmpty() || it.bagCount!!.isEmpty() || it.bagCount.equals("0")) isEmptyData = true
                }
                when {
                    isEmptyData || mReceiving.size == 0 -> showSnack(getString(R.string.error_valid_bag_count_type))
                    receivingData.weighBridgeType == PROCURE && binding.tvOrigin.text.isNullOrEmpty() -> showSnack(
                        getString(R.string.error_valid_origin)
                    )
                    receivingData.weighBridgeType == PROCURE && binding.tvDestinationLocation.text.isNullOrEmpty() -> showSnack(
                        getString(R.string.error_valid_department)
                    )
                    imageFilePath.isEmpty() -> {
                        moveToSummary()
                        // showSnack(getString(R.string.error_valid_ticket_photo))
                    }
                    else -> {
                        moveToSummary()
                    }
                }

            }
        }
        /*} else {
            when {
                binding.etTruckTarWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_tar_weight))
                else -> {
                    receivingData.tareWeight = binding.etTruckTarWeight.text.toString()
                    moveToSummary()
                }
            }
        }*/
    }

    private fun moveToSummary() {
        //saveTruckOutInfo()
        receivingData.imagePath = imageFilePath
        receivingData.wsGate = WB01
        prepareSuccessData(receivingData.weighBridgeId, false)
        callBack?.replaceFragment(TRUCKOUT_SUMMARYT_FRAG, receivingData, mReceiving)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        receivingData.weighBridgeId = wbId.toString()
        receivingData.tmpWbId = wbId ?: ""
        receivingData.truckDirection = DIRECTIONOUT
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = "Data cached offline"
        receivingData.vendorDeclaredWeight= binding.etTruckDeclaredweight.text.toString()
        receivingData.challan= binding.etConnaissement.text.toString()
        receivingData.remarks= binding.etCooperative.text.toString()
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }
    }

    private fun updateValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val driverName =
                    if (response.data?.data?.truckDriverName.isNullOrEmpty()) response.data?.data?.driverName else response.data?.data?.truckDriverName
                binding.tvDriverName.text = response.data?.data?.truckDriverName
                binding.etTruckDeclaredweight.text = response.data?.data?.vendorDeclaredWeight
                binding.etConnaissement.text = response.data?.data?.challan
                binding.etCooperative.setText(response.data?.data?.remarks)
                binding.etTruckBagcount.text = response.data?.data?.bagCount?.trim()
                binding.tvDriverPhone.text = response.data?.data?.contactNumber
                binding.tvTruckNo.text = response.data?.data?.vehicleNumber
                receivingData.transportVendorCode = response.data?.data?.transportVendorCode
                receivingData.transportVendorName = response.data?.data?.transportVendorName
                receivingData.dstorageLocationCode = response.data?.data?.dstorageLocationCode
                receivingData.dstorageLocationName = response.data?.data?.dstorageLocationName
                receivingData.materialCode = response.data?.data?.materialCode
                receivingData.materialName = response.data?.data?.materialName
                receivingData.driverName = response.data?.data?.truckDriverName
                receivingData.truckDriverName = response.data?.data?.truckDriverName
                receivingData.contactNumber = response.data?.data?.contactNumber
                receivingData.truckNo = response.data?.data?.truckNo
                receivingData.storageLocationCode = response.data?.data?.storageLocationCode
                receivingData.storageLocationName = response.data?.data?.storageLocationName
                binding.tvReceiveWh.text =
                    response.data?.data?.storageLocationCode.plus("-")
                        .plus(getLocationName(response.data?.data?.storageLocationCode))
                binding.tvDispatchWh.text =
                    response.data?.data?.dstorageLocationCode.plus("-").plus(response.data?.data?.dstorageLocationName)
                if(binding.etTruckDeclaredweight.text.isNotEmpty())
                    binding.etTruckDeclaredweight.isEnabled = false
                if(binding.etConnaissement.text.isNotEmpty())
                    binding.etConnaissement.isEnabled = false
                if(binding.etCooperative.text.isNotEmpty())
                    binding.etCooperative.isEnabled = false
                if( binding.etTruckBagcount.text.isNotEmpty())
                    binding.etTruckBagcount.isEnabled = false
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }

    private fun getLocationName(storageLocationName: String?): String {
        val location = ""
        if (storageLocationName != null) {
            val item = wareHouseList.singleOrNull { it.storageLocationCode == storageLocationName }
            return item?.storageLocationName ?: ""
        }
        return location
    }

    fun saveTruckOutInfo() {
        if ((binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) || (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty())
            || (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty())
        ) {
            val list = ArrayList<VegaCoffeeOffloadingBagMaterial>()

            mReceiving.forEachIndexed { index, vegaReceiving ->
                val bag = VegaCoffeeOffloadingBagMaterial()
                if (localDBData != null) {
                    if (index <= localDBData?.bags?.size ?: 0) {
                        Log.i("djhgfdshjcvjds ", "dshjfdsjhvcds $index  fdsdsfds ${localDBData?.bags?.size}")
                    }
                }
                /*bag.bagCount = vegaReceiving.bagCount ?: "0"
            bag.bagType = vegaReceiving.bagType ?: ""
            bag.mtnNumber = vegaReceiving.weighBridgeId.plus(vegaReceiving.bagType)
            bag.batchNumber = vegaReceiving.weighBridgeId.plus(vegaReceiving.bagType)
            bag.id = Random.nextInt()
            bag.tareWeight = vegaReceiving.bagTareWeight
            bag.baseMaterial = vegaReceiving.weighBridgeId
            list.add(bag)*/
            }

            mReceiving.forEach {
                val bag = VegaCoffeeOffloadingBagMaterial()
                bag.bagCount = it.bagCount ?: "0"
                bag.bagType = it.bagType ?: ""
                bag.mtnNumber = it.weighBridgeId.plus(it.bagType)
                bag.batchNumber = it.weighBridgeId.plus(it.bagType)
                bag.id = Random.nextInt()
                bag.tareWeight = it.bagTareWeight
                bag.baseMaterial = it.weighBridgeId
                list.add(bag)
            }
            vm.saveTruckoutDetails(receivingData, list)
        }
    }
    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean, isSupplier: Boolean,isOrigin:Boolean,isDepartment:Boolean) {
        customDialog?.dismiss()
        if(isOrigin)
        {
            binding.tvOrigin.text=data
            receivingData.origin=data
        }
        else if(isDepartment)
        {
            binding.tvDestinationLocation.text=data
            receivingData.department=data
        }
        else {
        }
    }


    private fun updateoriginlist(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    OriginList.clear()
                    Departmentlist.clear()
                    it.forEach { item ->
                        if(item.qualityParameter.materialCode==materialCode&&item.qualityParameter.nameChar=="CI_COFFEE_TRANS_ORIGIN")
                        {
                            item.qualitative?.forEach {
                                item1->
                                OriginList.add(item1)
                            }
                        }
                        if(item.qualityParameter.materialCode==materialCode&&item.qualityParameter.nameChar=="CI_COFFEE_TRANS_DEPT")
                        {
                            item.qualitative?.forEach {
                                item1->
                                Departmentlist.add(item1)
                            }
                        }
                    }
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()

                    value.addAll(it)

                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }
    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {

    }
}
