package com.olam.warehouse.vegax.receiving.ui.truckout.mtnt

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaTruckoutMtntAddWeightBinding
import com.olam.warehouse.vegax.receiving.ui.VegaMtntViewModel
import com.olam.warehouse.vegax.receiving.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException

/**
 * Created by Baskaran Kannan on 2/14/2020.
 */
class VegaTruckOutMtntAddWeightFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var mtntData = VegaMtnt()
    private var mMtntList = mutableListOf<VegaMtnt>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101

    interface CallBack {
        fun replaceMtntFragment(
            moveFrag: String,
            mtntData: VegaMtnt,
            mMtntList: MutableList<VegaMtnt>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    private val vm: VegaMtntViewModel by viewModel()
    private lateinit var binding: FragmentVegaTruckoutMtntAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_truckout_mtnt_add_weight

    companion object {
        fun newInstance(mtntData: VegaMtnt, mMtntList: ArrayList<VegaMtnt>) =
            VegaTruckOutMtntAddWeightFragment().putArgs {
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
        binding = FragmentVegaTruckoutMtntAddWeightBinding.inflate(layoutInflater)
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
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
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
            getString(R.string.truck_id).plus(": ").plus(mtntData.vehicleNumber ?: mtntData.weighBridgeId)
        binding.tvSupplierName.text = mtntData.supplierName ?: mtntData.supplierCode
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
                }
                1 -> {
                    binding.tvBagType2.text = vegaMtnt.bagType
                    binding.etBagCount2.setText(vegaMtnt.bagCount)
                }
                2 -> {
                    binding.tvBagType3.text = vegaMtnt.bagType
                    binding.etBagCount3.setText(vegaMtnt.bagCount)
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
            showErrorDialogWithFAQLink(
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
                showErrorDialogWithFAQLink(
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
                when (bag) {
                    1 -> binding.tvBagType1.setText(text, TextView.BufferType.EDITABLE)
                    2 -> binding.tvBagType2.setText(text, TextView.BufferType.EDITABLE)
                    3 -> binding.tvBagType3.setText(text, TextView.BufferType.EDITABLE)
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
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType1.text.toString()) }
                    if (data.isNotEmpty()) receiving.bagTareWeight = data[0].tareWeight
                    mMtntList.add(receiving)
                }
                if (binding.tvBagType2.text.isNotEmpty() || binding.etBagCount2.text.isNotEmpty()) {
                    val receiving1 = mtntData.copy()
                    receiving1.bagType = binding.tvBagType2.text.toString()
                    receiving1.bagCount = binding.etBagCount2.text.toString()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType2.text.toString()) }
                    if (data.isNotEmpty()) receiving1.bagTareWeight = data[0].tareWeight
                    mMtntList.add(receiving1)
                }
                if (binding.tvBagType3.text.isNotEmpty() || binding.etBagCount3.text.isNotEmpty()) {
                    val receiving2 = mtntData.copy()
                    receiving2.bagType = binding.tvBagType3.text.toString()
                    receiving2.bagCount = binding.etBagCount3.text.toString()
                    val data = bagTypeList.filter { it.bagType.equals(binding.tvBagType3.text.toString()) }
                    if (data.isNotEmpty()) receiving2.bagTareWeight = data[0].tareWeight
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
        mtntData.wsGate = WB01
        prepareSuccessData(mtntData.weighBridgeId, false)
        callBack?.replaceMtntFragment(TRUCKOUT_SUMMARYT_FRAG, mtntData, mMtntList)
    }

    private fun prepareSuccessData(wbId: String?, syncStatus: Boolean) {
        mtntData.weighBridgeId = wbId.toString()
        mtntData.tmpWbId = wbId ?: ""
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
