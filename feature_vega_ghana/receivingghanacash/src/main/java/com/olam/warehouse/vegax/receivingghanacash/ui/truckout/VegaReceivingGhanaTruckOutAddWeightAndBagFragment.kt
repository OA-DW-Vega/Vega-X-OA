package com.olam.warehouse.vegax.receivingghanacash.ui.truckout

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
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receivingghanacash.R
import com.olam.warehouse.vegax.receivingghanacash.databinding.FragmentVegaReceivingGhanaTruckoutAddWeightBinding
import com.olam.warehouse.vegax.receivingghanacash.ui.VegaReceivingGhanaViewModel
import com.olam.warehouse.vegax.receivingghanacash.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.File
import java.io.IOException

/**
 * Created by Baskaran Kannan on 1/28/2020.
 */
class VegaReceivingGhanaTruckOutAddWeightAndBagFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mReceiving = mutableListOf<VegaReceiving>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var currentImagePath: String? = ""
    val CAMERA_REQUEST_CODE = 0
    var imageFilePath: String = ""
    private val TAG = "PermissionDemo"
    private val RECORD_REQUEST_CODE = 101

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

    private val vm: VegaReceivingGhanaViewModel by viewModel()
    private lateinit var binding: FragmentVegaReceivingGhanaTruckoutAddWeightBinding

    override val layoutResourceId = R.layout.fragment_vega_receiving_ghana_truckout_add_weight

    companion object {
        fun newInstance(
            receivingData: VegaReceiving,
            receiving: ArrayList<VegaReceiving>
        ) = VegaReceivingGhanaTruckOutAddWeightAndBagFragment().putArgs {
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
        binding = FragmentVegaReceivingGhanaTruckoutAddWeightBinding.inflate(layoutInflater)
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
            getString(R.string.truck_id).plus(": ").plus(receivingData.vehicleNumber ?: receivingData.weighBridgeId)
        binding.tvSupplierName.text = receivingData.supplierName ?: receivingData.supplierCode
        binding.tvWeight.text = receivingData.grossWeight.plus(" ").plus(receivingData.unitsOfMeasure)
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
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        binding.tvBagType1.setOnClickListener { showBagTypeDiaog(1, bagTypeList) }
        binding.tvBagType2.setOnClickListener { showBagTypeDiaog(2, bagTypeList) }
        binding.tvBagType3.setOnClickListener { showBagTypeDiaog(3, bagTypeList) }

        binding.btnProceed.setOnClickListener { validateInputs() }
        binding.ivCamere.setOnClickListener { setupPermissions() }
        binding.llCamera.setOnClickListener { setupPermissions() }
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
        vm.saveReceiving(receivingData)
        if (!syncStatus) {
            mReceiving.forEach { it.tmpWbId = wbId ?: "" }
            vm.saveReceivingLineItems(mReceiving)
        }
    }


}
