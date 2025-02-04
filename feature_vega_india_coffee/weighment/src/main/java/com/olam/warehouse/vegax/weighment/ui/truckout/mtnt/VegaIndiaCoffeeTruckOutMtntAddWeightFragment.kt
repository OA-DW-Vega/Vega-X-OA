package com.olam.warehouse.vegax.weighment.ui.truckout.mtnt

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
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighment.R
import com.olam.warehouse.vegax.weighment.databinding.FragmentVegaIndiaCoffeeTruckoutMtntAddWeightBinding
import com.olam.warehouse.vegax.weighment.ui.VegaIndiaCoffeeMtntViewModel
import com.olam.warehouse.vegax.weighment.utils.DIRECTIONOUT
import com.olam.warehouse.vegax.weighment.utils.MTNTDATA
import com.olam.warehouse.vegax.weighment.utils.MTNT_POST_DATA
import com.olam.warehouse.vegax.weighment.utils.TRUCKOUT_SUMMARYT_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException

class VegaIndiaCoffeeTruckOutMtntAddWeightFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var mtntData = VegaMtnt()
    private var mMtntList = mutableListOf<VegaMtnt>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private var bagWeightOne = ""
    private var bagWeightTwo = ""
    private var bagWeightThree = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101
    private var isRoundoff: Boolean = false

    interface CallBack {
        fun replaceMtntFragment(
            moveFrag: String,
            mtntData: VegaMtnt,
            mMtntList: MutableList<VegaMtnt>, isRoundoff: Boolean
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaIndiaCoffeeMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeeTruckoutMtntAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_truckout_mtnt_add_weight

    companion object {
        fun newInstance(mtntData: VegaMtnt, mMtntList: ArrayList<VegaMtnt>) =
            VegaIndiaCoffeeTruckOutMtntAddWeightFragment().putArgs {
                putParcelable(MTNTDATA, mtntData)
                putParcelableArrayList(MTNT_POST_DATA, mMtntList)
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
        binding = FragmentVegaIndiaCoffeeTruckoutMtntAddWeightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/truckout/mtnt/VegaTruckOutMtntAddWeightFragment").title("Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        mMtntList = arguments?.getParcelableArrayList<VegaMtnt>(MTNT_POST_DATA)!!
        mtntData = arguments?.getParcelable(MTNTDATA)!!

        binding.tvTruckOutLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.truck_tar_weight)) { mandatoryStars() } }
        binding.ivCamere.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.upload_ticket_photo)) { mandatoryStars() } }
        binding.tvUom.text = mtntData.unitsOfMeasure
        binding.tvTruckNo.text = mtntData.vehicleNumber
        binding.tvWeighBridgeId.text = mtntData.weighBridgeId
        binding.tvTruckID.text =
            getString(R.string.truck_id).plus(": ")
                .plus(mtntData.vehicleNumber ?: mtntData.weighBridgeId)
        binding.tvSupplierName.text = mtntData.supplierCode.plus("-").plus(mtntData.supplierName)
        binding.tvWeight.text = mtntData.tareWeight.plus(" ").plus(mtntData.unitsOfMeasure)
        binding.tvDate.text = mtntData.erdat
        val times = mtntData.erdat?.split('(', ')')
        binding.tvDate.text = times?.get(1)?.let { it1 ->
            DateUtils.getUTCDateTime(
                it1,
                App.getAppContext()
            )
        }
        imageFilePath = mtntData.imagePath.toString()
        if (imageFilePath.isNotEmpty()) updateCameraLayout()
        mMtntList.forEachIndexed { index, vegaMtnt ->
            when (index) {
                0 -> {
                    binding.tvBagType1.text = vegaMtnt.bagType
                    binding.etBagCount1.setText(vegaMtnt.bagCount)
                    binding.etBagWeight1.setText(vegaMtnt.bagTareWeight)
                }
                1 -> {
                    binding.tvBagType2.text = vegaMtnt.bagType
                    binding.etBagCount2.setText(vegaMtnt.bagCount)
                    binding.etBagWeight2.setText(vegaMtnt.bagTareWeight)
                }
                2 -> {
                    binding.tvBagType3.text = vegaMtnt.bagType
                    binding.etBagCount3.setText(vegaMtnt.bagCount)
                    binding.etBagWeight3.setText(vegaMtnt.bagTareWeight)
                }
            }
        }

        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

        binding.btnProceed.setOnClickListener { validateInputs() }
        binding.ivCamere.setOnClickListener { setupPermissions() }

        binding.etBagWeight1.onChange { bagWeightOne = it.toString() }
        binding.etBagWeight2.onChange { bagWeightTwo = it.toString() }
        binding.etBagWeight3.onChange { bagWeightThree = it.toString() }
        binding.tvRoundoff.setOnClickListener {
            isRoundoff = true
            showSnack(getString(R.string.rounded))
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
            UIUtils.showErrorDialog(
                requireContext(),
                requireContext().resources.getString(R.string.could_not_create_file)
            )
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
                UIUtils.showErrorDialog(
                    requireContext(),
                    requireContext().resources.getString(R.string.unrecognized_request_code)
                )
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
        binding.ivCamere.text = mtntData.vehicleNumber.plus(".jpg")
        ViewCompat.setBackgroundTintList(
            binding.ivCamere,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.dark_marun
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.ivCamere,
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
                if (bag == 1) {
                    binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                    var filteredBagTypeList =  bagTypeList.filter {  it.bagType==binding.tvBagType1.text.toString() }
                    if(filteredBagTypeList.size>0) {
                        binding.etBagWeight1.setText(filteredBagTypeList.get(0).tareWeight)
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
        //if (mtntData.weighBridgeType == PROCURE) {
        when {
            binding.etTruckTarWeight.text.isNullOrEmpty() -> showSnack(getString(R.string.error_valid_tar_weight))
            else -> {
                mtntData.grossWeight = binding.etTruckTarWeight.text.toString().toDouble().toString()
                if (binding.tvBagType1.text.isNotEmpty() || binding.etBagCount1.text.isNotEmpty()) {
                    val receiving = mtntData.copy()
                    mMtntList.clear()
                    receiving.bagType = binding.tvBagType1.text.toString()
                    receiving.bagCount = binding.etBagCount1.text.toString()
                    if(bagWeightOne.isNullOrEmpty()) {
                        receiving.bagTareWeight = binding.etBagWeight1.text.toString()
                    }else{
                        receiving.bagTareWeight = bagWeightOne.trim()
                    }
                    receiving.wsGate = "0002"
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
                    if(bagWeightOne.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving.bagTareWeight = bagWeightOne.trim()
                    }
                    mMtntList.add(receiving)
                }
                if (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty()) {
                    val receiving1 = mtntData.copy()
                    receiving1.bagType = binding.tvBagType2.text.toString()
                    receiving1.bagCount = binding.etBagCount2.text.toString()
                    if(bagWeightTwo.isNullOrEmpty()) {
                        receiving1.bagTareWeight = binding.etBagWeight2.text.toString()
                    }else {
                        receiving1.bagTareWeight = bagWeightTwo.trim()
                    }
                    receiving1.wsGate = "0002"
                    if(!(receiving1.grossWeight.equals("0"))) {
                        receiving1.grossWeight = "0"
                        receiving1.tareWeight = "0"
                        receiving1.netWeight = "0"
                    }
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                    if(bagWeightTwo.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving1.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving1.bagTareWeight = bagWeightTwo.trim()
                    }
                    mMtntList.add(receiving1)
                }
                if (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty()) {
                    val receiving2 = mtntData.copy()
                    receiving2.bagType = binding.tvBagType3.text.toString()
                    receiving2.bagCount = binding.etBagCount3.text.toString()
                    if(bagWeightThree.isNullOrEmpty()) {
                        receiving2.bagTareWeight = binding.etBagWeight3.text.toString()
                    }else {
                        receiving2.bagTareWeight = bagWeightThree.trim()
                    }
                    receiving2.wsGate = "0002"
                    if(!(receiving2.grossWeight.equals("0"))) {
                        receiving2.grossWeight = "0"
                        receiving2.tareWeight = "0"
                        receiving2.netWeight = "0"
                    }
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }
                    if(bagWeightThree.isNullOrEmpty()) {
                        if (data.isNotEmpty()) receiving2.bagTareWeight = data[0].tareWeight
                    }else {
                        receiving2.bagTareWeight = bagWeightThree.trim()
                    }
                    mMtntList.add(receiving2)
                }
                var isEmptyData = false
                mMtntList.forEach {
                    if (it.bagType!!.isEmpty() || it.bagCount!!.isEmpty() || it.bagCount.equals("0")) isEmptyData = true
                }
                when {
                    isEmptyData || mMtntList.size == 0 -> showSnack(getString(R.string.error_valid_bag_count_type))
                    imageFilePath.isEmpty() -> {
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
                    mtntData.tareWeight = binding.etTruckTarWeight.text.toString()
                    moveToSummary()
                }
            }
        }*/
    }

    private fun moveToSummary() {
        mtntData.imagePath = imageFilePath
        mtntData.wsGate = "0002"
//        mtntData.wsGate = WB01
        prepareSuccessData(mtntData.weighBridgeId, false)
        callBack?.replaceMtntFragment(TRUCKOUT_SUMMARYT_FRAG, mtntData, mMtntList, isRoundoff)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        mtntData.weighBridgeId = wbId.toString()
        mtntData.tmpWbId = wbId ?: ""
        mtntData.wsGate = "0002"
        mtntData.truckDirection = DIRECTIONOUT
        mtntData.status = Status.SYNC_PENDING
        mtntData.isSynced = syncStatus
        mtntData.syncStatusMsg = "Data cached offline"
        vm.saveMtnt(mtntData)
        if (!syncStatus) {
            mMtntList.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveMtntLineItems(mMtntList)
        }
    }


}
