package com.olam.warehouse.vegax.weighment.ui.truckout

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
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeTruckoutAddWeightBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeReceivingViewModel
import com.olam.warehouse.vegax.weighment.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException

class VegaIndiaCoffeeTruckOutAddWeightAndBagFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
    private var seivingSelection = 0
    private var bagWeightOne = ""
    private var bagWeightTwo = ""
    private var bagWeightThree = ""
    private var approveQualityList = ArrayList<VegaQualityParamsWithQualitative>()
    private var isRoundoff: Boolean = false
   // private var qualitylist = arrayListOf<VegaQualitative>()

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            receivingData: VegaReceiving,
            mReceiving: MutableList<VegaReceiving>, isRoundoff: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaIndiaCoffeeReceivingViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeTruckoutAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_truckout_add_weight

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            receiving: ArrayList<VegaReceiving>
        ) = VegaIndiaCoffeeTruckOutAddWeightAndBagFragment().putArgs {
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
        binding = FragmentVegaIndiaCoffeeTruckoutAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/VegaTruckOutAddWeightAndBagFragment").title("Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        mReceiving = arguments?.getParcelableArrayList<VegaReceiving>(RECEIVING_POST_DATA)!!

        binding.tvTruckOutLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_tar_weight)) { mandatoryStars() } }
        binding.ivCamere.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.upload_ticket_photo)) { mandatoryStars() } }
        binding.tvBagDetails.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.bag_details)) { mandatoryStars() } }

        binding.tvUom.text = receivingData.unitsOfMeasure
        binding.tvTruckNo.text = receivingData.vehicleNumber
        binding.tvWeighBridgeId.text = receivingData.weighBridgeId
        binding.etTruckTarWeight.setText(
            if (receivingData.tareWeight.equals("0.000") || receivingData.tareWeight.equals("0")) "" else receivingData.tareWeight,
            TextView.BufferType.EDITABLE
        )
        imageFilePath = receivingData.imagePath.toString()
        if (imageFilePath.isNotEmpty()) updateCameraLayout()
        mReceiving.forEachIndexed { index, vegaReceiving ->
            when (index) {
                0 -> {
                    binding.tvBagType1.text = vegaReceiving.bagType
                    binding.etBagCount1.setText(vegaReceiving.bagCount)
                    binding.etBagWeight1.setText(vegaReceiving.bagTareWeight)
                }
                1 -> {
                    binding.tvBagType2.text = vegaReceiving.bagType
                    binding.etBagCount2.setText(vegaReceiving.bagCount)
                    binding.etBagWeight2.setText(vegaReceiving.bagTareWeight)
                }
                2 -> {
                    binding.tvBagType3.text = vegaReceiving.bagType
                    binding.etBagCount3.setText(vegaReceiving.bagCount)
                    binding.etBagWeight3.setText(vegaReceiving.bagTareWeight)
                }
            }
        }

        when (receivingData.weighBridgeType) {
            PROCURE -> {
                binding.tvdifference.text = SUPPLIER
            }
            else -> {
                receivingData.supplierCode = receivingData.customerNum
                binding.tvdifference.visibility = View.GONE
                binding.tvSupplierName.visibility = View.GONE
                binding.tvdifference.text = WAREHOUSE
            }
        }
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ")
                .plus(receivingData.vehicleNumber ?: receivingData.weighBridgeId)
        binding.tvSupplierName.text =
            receivingData.supplierCode.plus("-").plus(receivingData.supplierName)
        binding.tvWeight.text =
            receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
        binding.tvDate.text = receivingData.erdat
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
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateQuality(it) })
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        vm.material.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getMaterials()
        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

        binding.btnProceed.setOnClickListener { validateInputs() }
        binding.ivCamere.setOnClickListener { setupPermissions() }
        binding.llCamera.setOnClickListener { setupPermissions() }

        binding.etBagWeight1.onChange { bagWeightOne = it.toString() }
        binding.etBagWeight2.onChange { bagWeightTwo = it.toString() }
        binding.etBagWeight3.onChange { bagWeightThree = it.toString() }


        binding.cbPo.setOnCheckedChangeListener { it, isChecked -> updatePoSelection(isChecked, 1) }

        binding.tvRoundoff.setOnClickListener {
            isRoundoff = true
            showSnack(getString(R.string.rounded))

        }

    }

    private fun updateUI(data: List<VegaPackageMaterial>) {
        bagTypeList = data.toMutableList()

        /*  var filteredBagTypeList =  bagTypeList.filter {  it.bagType==receivingData.bagType }
          if(filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG")) {
              binding.tvBagType1.isClickable = false
              binding.tvBagType1.setText("CROP GUNNY BAG")
          }else{
              binding.tvBagType1.isClickable = true
          }*/
        vm.getPreSamplingQualitydata(
            receivingData.batchNumber.toString().trim(),
            receivingData.materialCode?.trim()!!
        )
        //vm.getQualityParams( receivingData.materialCode.toString(), false, receivingData.weighBridgeId)
    }

    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    var totalbags: Int = 0
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            preQualityList.forEach {
                                if (it.sapQCName.equals("Z_ACCEPTED_BAGS") || it.sapQCName.equals("Z_REJECTED_BAGS")) {
                                    val intAcceptedBags: Int = (it.satNam.toString()).toInt()
                                    totalbags = totalbags + intAcceptedBags
                                }
                            }


                            var filteredBagTypeList =  bagTypeList.filter {  it.bagType==receivingData.bagType }
                            //var filteredBagTypeList =  bagTypeList.filter {  it.bagType=="CROP GUNNY BAG"}
                            if(filteredBagTypeList.size>0) {
                                if (filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG")) {
                                    binding.tvBagType1.isClickable = false
                                    binding.etBagCount1.isClickable = false
                                    binding.etBagCount1.isFocusable = false
                                    binding.tvBagType1.text = filteredBagTypeList.get(0).bagType
                                    binding.etBagCount1.setText(totalbags.toString())
                                    binding.etBagWeight1.setText(filteredBagTypeList.get(0).tareWeight)
                                } else {
                                    binding.tvBagType1.isClickable = true
                                    binding.etBagCount1.isClickable = true
                                    binding.etBagCount1.isFocusable = true
                                    binding.tvBagType1.text = filteredBagTypeList.get(0).bagType
                                    binding.etBagCount1.setText(totalbags.toString())
                                    binding.etBagWeight1.setText(filteredBagTypeList.get(0).tareWeight)
                                }
                            }

                            //receivingData.materialCode?.let { vm.getQualityParams(receivingData.materialCode!!, false, receivingData.weighBridgeId) }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateQuality(data: List<VegaQualityParamsWithQualitative>?) {
        data?.let {
            when (it.isNotEmpty()) {
                true -> {
                    approveQualityList = it as ArrayList<VegaQualityParamsWithQualitative>
                    it.forEach {item ->
                        if ((item.qualityParameter.nameChar == "Z_ACCEPTED_BAGS") || (item.qualityParameter.nameChar == "Z_REJECTED_BAGS")){
                            //val intAcceptedBags: Int = (it.satNam.toString()).toInt();
                            //totalbags = totalbags +intAcceptedBags;
                        }
                    }




                  /*  it.forEach {
                        it.qualitative?.forEach{
                            val item = VegaQualitative()
                            item.nameChar = it.nameChar
                            item.charValue = it.charValue
                            item.descValue = it.descValue
                            qualitylist.add(item)
                        }

                    }



                    var totalbags: Int = 0;
                    approveQualityList.forEach {
                        it.qualitative?.forEach {
                            if (it.nameChar.equals("Z_ACCEPTED_BAGS")|| it.nameChar.equals("Z_REJECTED_BAGS")){
                                val intAcceptedBags: Int = (it.descValue.toString()).toInt();
                                totalbags = totalbags +intAcceptedBags;
                            }
                        }
                    }*/

                   /* var filteredBagTypeList =  bagTypeList.filter {  it.bagType==receivingData.bagType }
                    if(filteredBagTypeList.get(0).bagType.equals("CROP GUNNY BAG")) {
                        binding.tvBagType1.isClickable = false
                        binding.etBagCount1.isClickable = false
                        binding.tvBagType1.setText("CROP GUNNY BAG")
                        binding.etBagCount1.setText(totalbags)
                    }else{
                        binding.tvBagType1.isClickable = true
                    }*/



                }
                else -> {
                    //nothing to implement
                }
            }
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
            RECORD_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissions[0]) }!!

                when (granted) {
                    true -> moveToCameraView()
                }
            }
        }
    }

    private fun moveToCameraView() {
        try {
            val imageFile = createImageFile()
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (activity?.packageManager?.let { callCameraIntent.resolveActivity(it) } != null) {
                val authorities = activity!!.packageName + ".fileprovider"
                val imageUri = activity?.let { FileProvider.getUriForFile(it, authorities, imageFile) }
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

    private fun updatePoSelection(selected: Boolean, position: Int) {
     if (selected) {
            when (position) {
                1 -> {
                    seivingSelection = 1
                }
            }
        }else{
         seivingSelection = 0
     }

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
                if (bag == 1) {
                    if(receivingData.weighBridgeType==STO){
                        binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                        var filteredBagTypeList =  bagTypeList.filter {  it.bagType==binding.tvBagType1.text.toString() }
                        if(filteredBagTypeList.size>0) {
                            binding.etBagWeight1.setText(filteredBagTypeList.get(0).tareWeight)
                        }
                    }else {
                        binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                    }
                }else if (bag == 2) {
                    binding.tvBagType2.setText(text, TextView.BufferType.EDITABLE)
                    var filteredBagTypeList =  bagTypeList.filter {  it.bagType==binding.tvBagType2.text.toString() }
                    if(filteredBagTypeList.size>0) {
                            binding.etBagWeight2.setText(filteredBagTypeList.get(0).tareWeight)
                    }
                }else if (bag == 3){
                    binding.tvBagType3.setText(text, TextView.BufferType.EDITABLE)
                    var filteredBagTypeList =  bagTypeList.filter {  it.bagType==binding.tvBagType3.text.toString() }
                    if(filteredBagTypeList.size>0) {
                        binding.etBagWeight3.setText(filteredBagTypeList.get(0).tareWeight)
                    }
                }
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun validateInputs() {
        //if (receivingData.weighBridgeType == PROCURE) {
        when {
            binding.etTruckTarWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_tar_weight))
            binding.etTruckTarWeight.text.toString().equals("0") -> showSnack(getString(R.string.error_valid_tar_weight_more_than_zero))
            else -> {
                receivingData.tareWeight = binding.etTruckTarWeight.text.toString()
                if (binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) {
                    val receiving = receivingData.copy()
                    mReceiving.clear()
                    receiving.bagType = binding.tvBagType1.text.toString()
                    receiving.bagCount = binding.etBagCount1.text.toString()
                    if(bagWeightOne.isNullOrEmpty()) {
                        receiving.bagTareWeight = binding.etBagWeight1.text.toString()
                    }else{
                        receiving.bagTareWeight = bagWeightOne.trim()
                    }
                    receiving.wsGate = "0002"
                    if(receiving.bagType.equals("CROP GUNNY BAG")){
                        if(seivingSelection==1){
                            receiving.challan = "1"
                            receiving.item = "0001"
                        }else{
                            receiving.challan = ""
                            receiving.item = "0002"
                            receiving.grossWeight = "0"
                            receiving.tareWeight = "0"
                            var filteredBagTypeList =  bagTypeList.filter {  it.bagType==receivingData.bagType }
                            if(filteredBagTypeList.size>0) {
                                receiving.materialCode = filteredBagTypeList.get(0).bagMaterialCode
                                receiving.materialName = filteredBagTypeList.get(0).bagType
                                receiving.unitsOfMeasure = "EA"
                            }
                        }
                    }else{
                        receiving.item = "0001"
                    }
                    /*
                    if(seivingSelection==1){
                        receiving.challan = "1"
                    }else{
                        receiving.challan = ""}*/
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
                    if(bagWeightOne.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving.bagTareWeight = bagWeightOne.trim()
                    }
                    mReceiving.add(receiving)
                }
                if (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty()) {
                    val receiving1 = receivingData.copy()
                    receiving1.bagType = binding.tvBagType2.text.toString()
                    receiving1.bagCount = binding.etBagCount2.text.toString()
                    if(bagWeightTwo.isNullOrEmpty()) {
                        receiving1.bagTareWeight = binding.etBagWeight2.text.toString()
                    }else {
                        receiving1.bagTareWeight = bagWeightTwo.trim()
                    }
                    if(seivingSelection==1){
                        receiving1.challan = "1"
                    }else {
                        receiving1.challan = ""
                    }
                    receiving1.item = "0001"
                    receiving1.wsGate = "0002"
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                    if(bagWeightTwo.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving1.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving1.bagTareWeight = bagWeightTwo.trim()
                    }
                    mReceiving.add(receiving1)
                }
                if (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty()) {
                    val receiving2 = receivingData.copy()
                    receiving2.bagType = binding.tvBagType3.text.toString()
                    receiving2.bagCount = binding.etBagCount3.text.toString()
                    if(bagWeightThree.isNullOrEmpty()) {
                        receiving2.bagTareWeight = binding.etBagWeight3.text.toString()
                    }else {
                        receiving2.bagTareWeight = bagWeightThree.trim()
                    }
                    if(seivingSelection==1){
                        receiving2.challan = "1"
                    }else {
                        receiving2.challan = ""
                    }
                    receiving2.item = "0001"
                    receiving2.wsGate = "0002"
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }
                    if(bagWeightThree.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving2.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving2.bagTareWeight = bagWeightThree.trim()
                    }
                    mReceiving.add(receiving2)
                }
                if (((binding.tvBagType2.text.isNullOrEmpty() || binding.etBagCount2.text.isNullOrEmpty()) && (binding.tvBagType3.text.isNullOrEmpty() || binding.etBagCount3.text.isNullOrEmpty()))) {
                    val receiving3 = receivingData.copy()
                    //receiving3.bagType = ""
                    //receiving3.bagCount = ""
                    if(seivingSelection==1){
                        receiving3.challan = "1"
                    }else {
                        receiving3.challan = ""
                    }
                    receiving3.item = "0001"
                    receiving3.wsGate = "0002"
                 //   val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                  //  if (data.isNotEmpty()) receiving3.bagTareWeight = data[0].tareWeight
                    receiving3.bagTareWeight="0"
                    mReceiving.add(receiving3)
                }
                /*var isEmptyData = false
                mReceiving.forEach {
                    if (it.bagType!!.isEmpty() || it.bagCount!!.isEmpty() || it.bagCount.equals("0")) isEmptyData = true
                }*/
                when {
                    //isEmptyData || mReceiving.size == 0 -> showSnack(getString(R.string.error_valid_bag_count_type))
                    mReceiving.size == 0 -> showSnack(getString(R.string.error_valid_bag_count_type))
                    imageFilePath.isNullOrEmpty() -> {
                        showSnack(getString(R.string.error_valid_ticket_photo))
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
        receivingData.imagePath = imageFilePath
        receivingData.wsGate =  "0002"
        prepareSuccessData(receivingData.weighBridgeId, false)
        callBack?.replaceFragment(TRUCKOUT_SUMMARYT_FRAG, receivingData, mReceiving, isRoundoff)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        receivingData.weighBridgeId = wbId.toString()
        receivingData.tmpWbId = wbId ?: ""
        receivingData.truckDirection = DIRECTIONOUT
        receivingData.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_PENDING
        receivingData.isSynced = syncStatus
        receivingData.syncStatusMsg = "Data cached offline"
        receivingData.item = "0001"
        receivingData.wsGate = "0002"
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }
    }


}
