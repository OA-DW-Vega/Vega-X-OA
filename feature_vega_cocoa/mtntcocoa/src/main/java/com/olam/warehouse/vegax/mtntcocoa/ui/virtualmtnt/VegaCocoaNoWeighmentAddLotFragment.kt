package com.olam.warehouse.vegax.mtntcocoa.ui.virtualmtnt

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
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentLot
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaNoWeighmentModel
import com.olam.warehouse.master.vegacocoa.model.VegaCocoaPurchaseOrders
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaLotListModel
import com.olam.warehouse.vegax.mtntcocoa.data.domain.model.VegaCocoaNWBagModel
import com.olam.warehouse.vegax.mtntcocoa.databinding.FragmentCocoaMtntNoWeighmentAddLotLayoutBinding
import com.olam.warehouse.vegax.mtntcocoa.ui.CallBack
import com.olam.warehouse.vegax.mtntcocoa.ui.VegaCocoaMtntViewModel
import com.olam.warehouse.vegax.mtntcocoa.utils.*
import kotlinx.android.synthetic.main.item_vega_cocoa_no_weighment_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class VegaCocoaNoWeighmentAddLotFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_cocoa_mtnt_no_weighment_add_lot_layout
    private lateinit var binding: FragmentCocoaMtntNoWeighmentAddLotLayoutBinding

    private val vm: VegaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaNoWeighmentModel? = null
    private var isMultipleLot: Boolean = false
    private var selectedPurchaseOrder: VegaCocoaPurchaseOrders? = null
    private lateinit var callback: CallBack
    private var moreWeightBatches = ""
    private var palletCountNotMatchBatches = ""
    private var addWeightPosition = 0
    private val RECORD_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 0
    private var imageFilePath: String = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaNoWeighmentModel) =
            VegaCocoaNoWeighmentAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCocoaMtntNoWeighmentAddLotLayoutBinding.inflate(layoutInflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        vm.dispatchNoWeighmentWh = model ?: VegaCocoaNoWeighmentModel()
        vm.getConfigItems(UserRoles.MTNT.role)
        vm.weighScaleWithLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchNoWeighmentWh = it.dispatch
                vm.noWeighmentlots.clear()
                vm.noWeighmentlots.addAll(it.lineItems)
                updateUIWithLocalData()
            }
        })
        if (AppUtils.isOnline() || model?.isEditData == true) {
            vm.getWeighScaleWithLotAndMaterial(
                vm.dispatchNoWeighmentWh.warehouseId ?: "",
                vm.dispatchNoWeighmentWh.purchaseDocNum ?: "",
                vm.dispatchNoWeighmentWh.purchaseDocDesc ?: ""
            )
        }
    }

    private fun updateUIWithLocalData() {
        enableLotAddFeatures()
        selectedPurchaseOrder?.materialName = vm.dispatchNoWeighmentWh.materialName ?: ""
        selectedPurchaseOrder?.materialCode = vm.dispatchNoWeighmentWh.materialCode ?: ""
        selectedPurchaseOrder?.plantId = vm.dispatchNoWeighmentWh.plantId
        refreshAdapter(vm.noWeighmentlots)
        enableProceed(vm.noWeighmentlots.isNotEmpty())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("/mtntcocoa/ui/virtualmtnt/VegaCocoaNoWeighmentAddLotFragment")
            .title("Dispatch Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        enableProceed(false)
        enableLotAddFeatures()
        binding.tvTruckValue.text = model?.purchaseDocNum.plus("-").plus(model?.purchaseDocDesc)
        binding.tvStoWeightValue.text = model?.netWeight.plus(" ").plus(model?.unitsOfMeasure)
        binding.tvMaterialValue.text =
            model?.materialName
        binding.ivCamera.setOnClickListener { setupPermissions() }
        binding.llCamera.setOnClickListener { setupPermissions() }
        binding.clLotSummary.etEnterContainer.onChange { enableAddLot(it) }
        binding.clLotSummary.clScan.setOnClickListener { moveToScan() }
        if (model?.imageString?.isNotEmpty() == true) {
            updateCameraLayout()
        }
        binding.clLotSummary.btnAddLot.setOnClickListener {
            callback.replaceFragment(
                LOT_LIST,
                VegaCocoaLotListModel(
                    selectedList = vm.noWeighmentlots,
                    isMultipleAdd = false,
                    material = getMaterialList()
                )
            )
        }
        binding.clLotSummary.btAdd.setOnClickListener {
            vm.validateLot(
                binding.clLotSummary.etEnterContainer.text.toString(),
                model?.materialCode ?: ""
            )
        }
        binding.btnProceed.setOnClickListener { validateProceed() }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.clLotSummary.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.clLotSummary.etEnterContainer.text.toString()
                )
        })

        vm.offlineStockInfo.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                val list = prepareOfflineStockInfo(it)
                hideLoading()
                when (list.size == 1) {
                    true -> updateAdapter(list[0])
                    else -> chooseOneLotDialog(list)
                }
            } else {
                hideLoading()
                showLotDetailsNotDialog()
            }
        })

        vm.qualityNoWeighmentDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.clLotSummary.btAdd.setOnClickListener {
            vm.validateLot(
                binding.clLotSummary.etEnterContainer.text.toString(), model?.materialCode ?: ""
            )
        }

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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

    private fun showLotAlreadyExistDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.already_added)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.ok),
                "",
                { dismiss() },
                { })
        }
    }

    private fun showLotDetailsNotDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.lot_not_available)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.ok),
                "",
                { dismiss() },
                { })
        }
    }

    private fun fetchLotDetails(lotId: String) {
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getNoWeighmentLotDetails(
            lotId,
            model?.materialCode ?: "",
            getPlantDetails().plantId
        ) else {
            vm.getOfflineStockDetails(lotId, model?.materialCode ?: "")
        }
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        vm.noWeighmentlots.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
        }
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }


    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    fun updateLotList(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        val addedNew = ArrayList<VegaCocoaNoWeighmentLot>()
        val removedLots = ArrayList<VegaCocoaNoWeighmentLot>()
        val batchMap = vm.noWeighmentlots.map { it.batchNumber }
        val batchNewMap = list.map { it.batchNumber }
        list.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                addedNew.add(it)
            }
        }
        vm.noWeighmentlots.forEach {
            if (!batchNewMap.contains(it.batchNumber)) {
                removedLots.add(it)
            }
        }
        vm.noWeighmentlots.removeAll(removedLots)

        for (item in removedLots) {
            vm.removeNoWeighmentFromList(item.batchNumber, item.materialCode)
        }
        addedNew.forEach {
            it.weighBridgeId = model?.weighBridgeId.toString()
            it.isOfflineData = AppUtils.isOnline()
        }
        vm.addLoTInDB(addedNew)
        vm.noWeighmentlots.addAll(addedNew)
        setUpAdapter(vm.noWeighmentlots)
        enableProceed(vm.noWeighmentlots.isNotEmpty())
        enableLotAddFeatures()
    }

    private fun enableProceed(enable: Boolean) {
        val enabled = enable && vm.dispatchNoWeighmentWh.imageString?.isNotEmpty() == true
        if (enabled) {
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enabled
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.noWeighmentlots.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }


    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.noWeighmentlots.filter {
                it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") || it.editedWeight?.toDouble()
                    ?.compareTo(0) ?: 0 <= 0
            }
        return emptyWeight.isEmpty()
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SELECTION_MULTI.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaNoWeighmentLot>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        when (it1.size == 1) {
                            true -> updateAdapter(it1[0])
                            else -> chooseOneLotDialog(it1)
                        }

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

    private fun chooseOneLotDialog(lots: List<VegaCocoaNoWeighmentLot>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(R.string.weight)).plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.st_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(lots[index])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(lots: VegaCocoaNoWeighmentLot) {
        binding.clLotSummary.etEnterContainer.setText("")
        lots.weighBridgeId = vm.dispatchNoWeighmentWh.weighBridgeId
        lots.isOfflineData = AppUtils.isOnline()
        vm.noWeighmentlots.add(lots)
        vm.addLoTInDB(lots)
        setUpAdapter(vm.noWeighmentlots)
    }

    private fun refreshAdapter(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        setUpAdapter(list)
        calculateAndUpdateWeightToDispatch()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    updateCameraLayout()
                    vm.dispatchNoWeighmentWh.imageString = setScaledBitmap()
                    enableProceed(vm.noWeighmentlots.isNotEmpty())
                } else {
                    imageFilePath = ""
                }
            }
            Constants.SCAN_QR -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                        fetchLotDetails(it)
                    }
                }
            }
            else -> {
                UIUtils.showErrorDialog(requireContext(), "Unrecognized request code")
                //activity?.toast("Unrecognized request code")
            }
        }
    }

    private fun validateProceed() {
        if (vm.noWeighmentlots.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    if (validatePalletCount()) {
                        if (validateLotWeightWithOpenQuantity()) {
                            vm.saveNoWeighmentDetails()
                            vm.addLoTInDB(vm.noWeighmentlots)
                            callback.replaceFragment(NO_WEIGHMENT_SUMMARY, vm.dispatchNoWeighmentWh)
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.less_weight_for_so_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else Toast.makeText(
                        activity,
                        getString(R.string.pallet_not_matched).plus(" ").plus(palletCountNotMatchBatches),
                        Toast.LENGTH_SHORT
                    ).show()
                    /*if (validateLotWeightWithMaterialWeight()) {*/
                    /*} else {

                    }*/
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.zero_less_weight_error, vm.noWeighmentlots[0].batchNumber),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaNoWeighmentLot>) {
        val lots = list
        binding.clLotSummary.rvList.setUp(lots, R.layout.item_vega_cocoa_no_weighment_lot, { item, pos ->
            tvScaleLotValue.text = item.batchNumber
            tvStLocationValue.text = item.storageLocationCode
            tvScaleWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
            tvScaleGradeValue.text = item.materialName
            val editedWeight = if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString().toDouble()
                .formatThreeDigits()
            tvScaleDispatchValue.text = editedWeight
            tvDispatchUOMValue.text = item.weightToDispatchUOM
            ivScaleClose.setOnClickListener {
                showConformationDialog(pos, ivScaleClose)
            }
            tv_add_weight.setOnClickListener {
                addWeightPosition = pos
                callback.replaceFragment(ADD_WEIGHT, item)
            }
            cbEndLot.isChecked = item.isEndLot ?: false
            cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                item.isEndLot = isChecked
            }
            tvVendor.visibility = if (model?.isThirdPartyMaterial == true) View.VISIBLE else View.GONE
            tvVendorValue.visibility = if (model?.isThirdPartyMaterial == true) View.VISIBLE else View.GONE
            tvVendorValue.text = item.vendorName
        })
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { itemRemoved(position) },
                { dismiss() })
        }
    }


    private fun getMaterialList(): ArrayList<String> {
        val material = ArrayList<String>()
        material.add(model?.materialCode ?: "")
        return material
    }

    private fun itemRemoved(pos: Int) {
        vm.removeNoWeighmentFromList(vm.noWeighmentlots[pos].batchNumber, vm.noWeighmentlots[pos].materialCode)
        vm.noWeighmentlots.removeAt(pos)
        binding.clLotSummary.rvList.adapter?.notifyItemRemoved(pos)
        calculateAndUpdateWeightToDispatch()
        enableLotAddFeatures()
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

    @Throws(IOException::class)
    fun createImageFile(): File {
        val imageFileName: String = "JPEG_".plus(binding.tvTruckNo.text)
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        return imageFile
    }

    private fun updateCameraLayout() {
        binding.ivCamera.text = model?.vehicleNumber.plus(".jpg")
        enableProceed(vm.noWeighmentlots.isNotEmpty())
        ViewCompat.setBackgroundTintList(
            binding.ivCamera,
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

    fun updateAddWeight(weight: VegaCocoaNWBagModel) {
        val split = weight.weight.split(" ")
        vm.noWeighmentlots[addWeightPosition].editedWeight = split[0]
        vm.noWeighmentlots[addWeightPosition].weightToDispatchUOM = split[1]
        binding.clLotSummary.rvList.adapter?.notifyItemChanged(addWeightPosition)
        var lotWeight: Double = vm.noWeighmentlots[addWeightPosition].weight?.toDouble() ?: 0.0
        if (model?.unitsOfMeasure == "MT" && split[1] == "KG") {
            lotWeight *= 1000
        }
        val come: Int? = split[0].toDouble().compareTo(lotWeight)
        vm.noWeighmentlots[addWeightPosition].isBagCountMatched = weight.isPalletMatched
        vm.noWeighmentlots[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.addLoTInDB(vm.noWeighmentlots[addWeightPosition])
        vm.dispatchNoWeighmentWh.truckOutWeight = weight.truckOut
        calculateAndUpdateWeightToDispatch()
    }

    fun saveLotDetails() {
        vm.saveNoWeighmentDetails()
        vm.addLoTInDB(vm.noWeighmentlots)
    }

    private fun setScaledBitmap(): String {
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

        val baos = ByteArrayOutputStream()
        BitmapFactory.decodeFile(imageFilePath, bmOptions).compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return encodeToString(b, DEFAULT)
    }

    private fun validateLotWeightWithOpenQuantity(): Boolean {
        var totalValue = 0.0
        vm.noWeighmentlots.forEach {
            totalValue = totalValue.plus(it.editedWeight?.toDouble() ?: 0.0)
        }
        var lotWeight: Double = model?.netWeight?.toDouble() ?: 0.0
        if (model?.unitsOfMeasure == "MT") {
            lotWeight *= 1000
        }
        val come: Int? = totalValue.compareTo(lotWeight)
        return come ?: 0 <= 0
    }

    private fun validatePalletCount(): Boolean {
        val selected = vm.noWeighmentlots.filter { !it.isBagCountMatched }
        palletCountNotMatchBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun enableLotAddFeatures() {
        val enableAll = vm.noWeighmentlots.isEmpty()
        binding.clLotSummary.clScan.isClickable = enableAll
        binding.clLotSummary.clScan.isEnabled = enableAll
        binding.clLotSummary.etEnterContainer.isEnabled = enableAll
    }
}
