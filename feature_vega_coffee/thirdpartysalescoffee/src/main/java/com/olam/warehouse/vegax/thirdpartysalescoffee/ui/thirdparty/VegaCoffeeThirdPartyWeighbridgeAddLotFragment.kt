package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.thirdparty

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartyWeighbridgeAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyViewModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import kotlinx.android.synthetic.main.item_third_party_add_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeThirdPartyWeighbridgeAddLotFragment : BaseFragment(), VegaSingleSelectListener {

    override val layoutResourceId = R.layout.fragment_third_party_weighbridge_add_lot
    private lateinit var binding: FragmentThirdPartyWeighbridgeAddLotBinding
    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null
    private var summaryCallback: ReplaceFragmentCallback? = null
    private var vendorList = mutableListOf<VegaVendor>()
    private var materialList = mutableListOf<VegaMaterial>()
    private var model: VegaCocoaDispatchWB? = null
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private var materialCode: String = ""
    private var materialName: String = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var fromVendorList = ArrayList<VegaVendor>()
    private var fromVendor = VegaVendor()
    private var moreWeightBatches = ""
    private var dispatchType = ""
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private var isEditLot = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
        summaryCallback = context as? ReplaceFragmentCallback
    }

    interface ReplaceFragmentCallback {
        fun replaceFragment(
            receivingType: String,
            data: Any, model: VegaCocoaDispatchWB
        )
    }

    companion object {

        fun newInstance(
            edit: Boolean,
            alreadySelected: ArrayList<VegaCocoaDispatchLots>
        ) = VegaCoffeeThirdPartyWeighbridgeAddLotFragment().putArgs {
            putBoolean("EDIT_LOT", edit)
            putParcelableArrayList(THIRD_PARTY_EDIT, alreadySelected)
        }

        fun newInstance(model: VegaCocoaDispatchWB) =
            VegaCoffeeThirdPartyWeighbridgeAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                    putString("type", WEIGHBRIDGE)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartyWeighbridgeAddLotBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementFragment").title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        materialCode = model?.materialCode.toString()
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        enableBottom()
        enableProceed()
        vm.dispatchWh = model ?: VegaCocoaDispatchWB()
        dispatchWbFromTruckList = arguments?.getParcelable<VegaCocoaDispatchWB>(MODEL_BUNDLE) as VegaCocoaDispatchWB
        isEditLot = arguments?.getBoolean("EDIT_LOT") ?: false
        dispatchType = arguments?.getString("type") ?: ""

        vm.stockLots.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateUI(it) })
        vm.getStockList(getMaterialList())

        vm.material.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            materialList = it.toMutableList()
        })
        vm.getThirdPartyMaterials()
        vm.suppplier.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            vendorList = it.toMutableList()
        })
        vm.getSuppliers()

        vm.qualityDetails.observe(viewLifecycleOwner, androidx.lifecycle.Observer { updateLotInfo(it) })
        binding.tvstoValue.text = vm.dispatchWh.materialName
        vm.thirdPartyInfo.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { if (it != null) updateLocalDBData(it) })
        binding.tvvendorvalue.setOnClickListener {
            showSingleSelectDialog()

        }

        binding.clInventory.setOnClickListener {

            moveToLotList()
        }
        binding.btProceed.setOnClickListener {
            validateProceed()
            // if (isEditLot) activity?.onBackPressed() else validateProceed()
        }
        binding.btnInventory.setOnClickListener {

            moveToLotList()
        }
        binding.btAdd.setOnClickListener { vm.validateLot(binding.etEnterContainer.text.toString()) }
        vm.validateLot.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.etEnterContainer.text.toString()
                )
        })
    }

    private fun moveToLotList() {
        callBack?.replaceFragment(
            LOT_LIST,
            VegaCoffeeThirdPartyLotListModel(
                selectedList = vm.lotList,
                isMultipleAdd = true,
                material = getMaterialList(), vendorCode = fromVendor.vendorCode
            )
        )
    }

    private fun fetchLotDetails(lotId: String) {
        binding.etEnterContainer.hideKeyboard()
        binding.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, getMaterialList(), getPlantDetails().plantId)
    }

    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    private fun updateLocalDBData(data: VegaCoffeeThirdPartyModelWithLots) {
        binding.tvvendorvalue.text = data.model.toVendorName
        if (data.lots != null) {
            vm.lotList = data.lots as ArrayList<VegaCocoaDispatchLots>
        }
        setUpAdapter(vm.lotList)
        enableBottom()
        enableProceed()
    }

    //    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
//        response.let {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    hideLoading()
//                    response.data?.data?.let { it1 ->
//                        when (it1.size == 1) {
//                            true -> updateAdapter(it1)
//                            else -> chooseOneLotDialog(it1)
//                        }
//
//                    }
//                }
//                Resource.Status.LOADING -> {
//                    showLoading()
//                }
//                Resource.Status.ERROR -> {
//                    hideLoading()
//                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
//                }
//            }
//        }
//    }
    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        val vendorFilter = it1.filter { it.vendor == fromVendor.vendorCode }
                        if (vendorFilter.isNotEmpty()) {
                            when (vendorFilter.size == 1) {
                                true -> updateAdapter(it1)
                                else -> chooseOneLotDialog(it1)
                            }
                        } else UIUtils.showErrorDialog(
                            requireContext(),
                            getString(R.string.vendor_code_not_matched)
                        )

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it)
                }
            }
        }
    }

    private fun chooseOneLotDialog(lots: List<VegaCocoaDispatchLots>) {
        val lotItem = lots.map {
            getString(com.olam.warehouse.login.R.string.lot_no).plus(" : ").plus(it.batchNumber).plus("\n")
                .plus(getString(R.string.weight)).plus(" : ").plus(it.weight).plus(" ").plus(it.unitOfMeasure)
                .plus("\n").plus(getString(R.string.storage_location)).plus(" : ").plus(it.storageLocationCode)
        }
        MaterialDialog(requireContext()).show {
            message(R.string.choose_lot)
            cancelOnTouchOutside(false)
            cancelable(false)
            listItemsSingleChoice(items = lotItem) { _, index, text ->
                updateAdapter(lots)
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(list: List<VegaCocoaDispatchLots>) {
        val addedNew = ArrayList<VegaCocoaDispatchLots>()
        val removedLots = ArrayList<VegaCocoaDispatchLots>()
        val batchMap = vm.lotList.map { it.batchNumber }
        val batchNewMap = list.map { it.batchNumber }
        list.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                it.vendorWithTransferType = vm.model.vendorWithTransferType
                it.weighBridgeId = model?.weighBridgeId ?: ""
                addedNew.add(it)
            }
        }
        vm.lotList.forEach {
            if (!batchNewMap.contains(it.batchNumber)) {
                removedLots.add(it)
            }
        }
        vm.lotList.removeAll(removedLots)

        for (item in removedLots) {
            vm.removeLot(item.batchNumber)
        }
        binding.etEnterContainer.setText("")
        list.forEach {
            it.apply {
                vendor = fromVendor.vendorCode
                weightToDispatchUOM = unitOfMeasure
            }
        }
        vm.addLoTInDB(addedNew)
        vm.lotList.addAll(addedNew)
        setUpAdapter(vm.lotList)
        enableProceed()
//        binding.etEnterContainer.setText("")
//        if (vm.lotsList.isNotEmpty())
//            vm.removeLot(vm.lotsList[0].batchNumber)
//        list.forEach {
//            it.apply {
//                weightToDispatchUOM = unitOfMeasure
//                vendorWithTransferType = vm.model.vendorWithTransferType
//            }
//        }
//        vm.addLoTInDB(list)
//        vm.lotsList.addAll(list)
//        setUpAdapter(list as ArrayList<VegaCocoaDispatchLots>)
//        enableProceed(true)
    }

    private fun getCurrentDate(type: String) {
        val stamp = Timestamp(System.currentTimeMillis())
        val date = Date(stamp.time)
        val dateFormat = SimpleDateFormat(Settings.System.DATE_FORMAT, Locale.getDefault())
        val dat = dateFormat.format(date)
        when (type) {
            "START" -> vm.salesOrder.startTime = DateUtils.getCurrentTimeInMills().toString()
            "END" -> vm.salesOrder.endTime = DateUtils.getCurrentTimeInMills().toString()
        }
    }

//    private fun enableProceed(enable: Boolean) {
//        binding.btProceed.isEnabled = enable
//        when (enable) {
//            true -> ViewCompat.setBackgroundTintList(
//                    binding.btProceed,
//                    ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
//            )
//            else -> ViewCompat.setBackgroundTintList(
//                    binding.btProceed,
//                    ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
//            )
//        }
//    }

    private fun enableBottom() {
        val enable =
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty()
        binding.clScan.isClickable = enable
        binding.clScan.isEnabled = enable
        binding.etEnterContainer.isEnabled = enable
        binding.btnInventory.isClickable = enable
        binding.clInventory.isClickable = enable
        binding.btnInventory.isEnabled = enable
        binding.clInventory.isEnabled = enable
        if (enable) {
            val model = VegaCoffeeThirdPartyRequestModel()
            model.materialCode = materialCode
            model.materialName = materialName
            model.fromVendorCode = fromVendor.vendorCode
            model.fromVendorName = fromVendor.vendorName ?: ""
            model.transferType = dispatchType
            model.createDate = vm.getCurrentDate()
            model.vendorWithTransferType = materialCode.plus(fromVendor.vendorCode).plus(dispatchType)
            vm.model = model
            vm.saveThirdPartyInfo()
        }
    }

    fun saveAndBack() {
        vm.saveThirdPartyInfo()
    }

    private fun showLotAlreadyExistDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.already_added)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                dismiss()
            }
        }
    }


    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    moveToSummary()
                    //showRemarkDialog()
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.zero_weight_error),
                Toast.LENGTH_SHORT
            ).show()
        } else Toast.makeText(
            activity,
            getString(R.string.please_add_lot),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }


    private fun validateLotWeight(): Boolean {
        val selected = vm.lotList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            dispatchLotsList.clear()
                            dispatchLotsList.addAll(it.data?.data!!)
                            filterFromVendor()
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

    private fun filterFromVendor() {
        fromVendorList.clear()
        val fromVendor = dispatchLotsList.map { it.vendor ?: "" }.toSet().toList()
        vendorList.forEach {
            if (fromVendor.toString().contains(it.vendorCode)) {
                fromVendorList.add(it)
            }
        }
        hideLoading()
    }

    private fun getMaterialList(): ArrayList<String> {
        val list = ArrayList<String>()
        //list.add(fromMaterial.materialCode)
        dispatchWbFromTruckList?.materialCode?.let { list.add(it) }
        return list
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    private fun showSingleSelectDialog() {
        val list = ArrayList<String>()
        list.addAll(fromVendorList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                getString(R.string.select_vendor),
                false, true, false,
                list,
                activity!!,
                this,isOrigin = false,isDepartment = false
            )


        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun showSingleMaterialSelectDialog(materialList: List<VegaMaterial>) {
        val list = ArrayList<String>()
        list.addAll(materialList.map { it.materialCode.plus("-").plus(it.materialName) })
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                getString(R.string.select_material),
                true, false, false,
                list,
                activity!!,
                this,isOrigin = false,isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isMaterial: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        val code = data.split("-")
        binding.tvvendorvalue.text = data
        binding.tvstoValue.hideKeyboard()
        fromVendor = vendorList.single { it.vendorCode == code[0] }
        fetchLocalDBData()
        enableBottom()
        clearLotList()
        enableProceed()
        vm.model.toVendorCode = fromVendor.vendorCode
        vm.model.toVendorName = fromVendor.vendorName
    }

    private fun clearLotList() {
        vm.lotList.clear()
        setUpAdapter(vm.lotList)
    }

    private fun fetchLocalDBData() {
        if (binding.tvstoValue.text.isNotEmpty() && materialCode.isNotEmpty()) {
            vm.getThirdPartyLocalData(materialCode.plus(fromVendor.vendorCode).plus(TP_TO_OLAM))
        }
    }

    fun updateLotList(lots: ArrayList<VegaCocoaDispatchLots>) {
        updateAdapter(lots)
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        val lots = list
        binding.rvLots.setUp(lots, R.layout.item_third_party_add_lot, { item, pos ->
            tvLotId.text = item.batchNumber
            tvStLocationValue1.text = item.storageLocationCode
            tvWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(item.unitOfMeasure)
            tvGradeValue.text = item.materialName
            val editedWeight = if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString().toDouble()
                .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
            etWeight.setText(editedWeight)
            cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                item.isChecked = isChecked
                if (isChecked) {
                    item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()

                } else {
                    item.editedWeight = "0.0"
                }
                etWeight.setText(item.editedWeight)
            }
            etWeight.onChange {
                if (it.isNotEmpty()) {
                    item.editedWeight = it
                    val come: Int? = it.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        item.isLowerWeight = true
                    } else {
                        item.isLowerWeight = false
                        etWeight.error = etWeight.context.getString(R.string.less_weight_error)
                    }
                }
            }
            ivClose.setOnClickListener {
                /*vm.lotList.remove(item)
                binding.rvLots.adapter?.notifyItemRemoved(pos)
                vm.removeLot(item.batchNumber)*/
                showConformationDialog(pos, ivClose)
            }
        })
//        val lots = list
//        binding.rvLots.setUp(lots, R.layout.item_third_party_add_lot, { item, pos ->
//            tvLotId.text = item.batchNumber
//            tvStLocationValue1.text = item.storageLocationCode
//            tvGradeValue.text = item.materialName
//            tvWeightValue.text = item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
//            cbSelectAll.isChecked = item.isChecked
//            etWeight.setText(item.editedWeight)
//            llSelectAll.setOnClickListener { view ->
//                item.isChecked = !item.isChecked
//                if (item.isChecked) {
//                    cbSelectAll.isChecked = true
//                    item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()
//
//                } else {
//                    cbSelectAll.isChecked = false
//                    item.editedWeight = "0"
//                }
//                etWeight.setText(item.editedWeight)
//                vm.lots.remove(item)
//                vm.lots.add(item)
//            }
//
//            ivClose.setOnClickListener { view -> showConformationDialog(pos, view) }
//            etWeight.onChange {
//                if (it.isNotEmpty()) {
//                    val editVal: String?
//                    val dot = it.get(0).toString()
//                    editVal = if (dot == ".") {
//                        if (it.length == 1) "0.0" else "0".plus(it)
//                    } else it
//
//                    item.editedWeight = editVal
//                    val come: Int? = editVal.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
//                    if (come ?: 0 <= 0) {
//                        item.isLowerWeight = true
//                        etWeight.error = null
//                        vm.lots.forEach { item ->
//                            if (item.batchNumber.equals(item.batchNumber) && item.materialCode.equals(item.materialCode)) item.editedWeight =
//                                    editVal
//                        }
//                    } else {
//                        item.isLowerWeight = false
//                        etWeight.error = etWeight.context.getString(R.string.less_weight_error)
//                    }
//                } else {
//                    item.editedWeight = "0"
//                }
//                vm.lots.remove(item)
//                vm.lots.add(item)
//            }
//        })

    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.removeLot(vm.lotList[position].batchNumber)
                    vm.lotList.removeAt(position)
                    setUpAdapter(vm.lotList)
                },
                { dismiss() })
        }
    }

    private fun enableProceed() {
        val enable =
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvvendorvalue.text.toString()
                .isNotEmpty() && vm.lotList.isNotEmpty()
        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
    }

    private fun moveToSummary() {
        vm.saveThirdPartyInfo()
        vm.model.materialCode = vm.dispatchWh.materialCode ?: ""
        vm.model.materialName = vm.dispatchWh.materialName ?: ""
        summaryCallback?.replaceFragment(Ownership_Transfer_Summary, vm.model, model ?: VegaCocoaDispatchWB())
    }

    private fun showRemarkDialog() {
        showDialog(getString(R.string.stop_loding_confirmation), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                ).show()
                else {
                    vm.model.remarks = remark
                    moveToSummary()
                }
            }
        }, true, vm.salesOrder.remarks.toString())
    }

}
