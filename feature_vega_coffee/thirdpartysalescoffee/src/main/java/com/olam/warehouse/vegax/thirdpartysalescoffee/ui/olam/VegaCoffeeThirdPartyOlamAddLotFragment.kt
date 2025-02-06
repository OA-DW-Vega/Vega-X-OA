package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.olam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.PODetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorPurchaseOrder
import com.olam.warehouse.navigation.features.DynamicFeature
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartyOlamAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.ItemThirdPartyAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyViewModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeThirdPartyOlamAddLotFragment : BaseFragment(), VegaSingleSelectCommonListener {

    private lateinit var saleType: String
    override val layoutResourceId = R.layout.fragment_third_party_olam_add_lot
    private lateinit var binding: FragmentThirdPartyOlamAddLotBinding

    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null
    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var thirdPartyMaterials: List<VegaMaterial> = mutableListOf()
    private var materialList:List<VegaMaterial> = mutableListOf()
    private var supplierList = mutableListOf<VegaVendor>()
    private var fromVendor = VegaVendor()
    private var materialCode: String = ""
    private var materialName: String = ""
    private var moreWeightBatches = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var fromVendorList = ArrayList<VegaVendor>()
    private var purchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    private var matPurchaseOrderList = mutableListOf<VegaEcuadorPurchaseOrder>()
    var ttFeatureList = java.util.ArrayList<VegaFeatureMaster>()
    var isComplaintMaterial = false
    var eudrComplaintStatus = ""



    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
    }

    companion object {
        fun newInstance(type: String) = VegaCoffeeThirdPartyOlamAddLotFragment()
            .putArgs {
                putString("type", type)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartyOlamAddLotBinding.inflate(layoutInflater)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.tvAddBale.text = getString(R.string.purchase_lot)
            binding.spWareHouse.text = getString(R.string.tolling_material)
            binding.tvsto.text = getString(R.string.tolling_vendor)
            binding.tvSummary.text = getString(R.string.lots_summary)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaThirdPartyOlamAddLotFragment")
            .title("IVC/Coffee/Third Party Sales/Olam Add Lot").with(tracker)
    }

    private fun initUI() {

        saleType = arguments?.getString("type") ?: ""
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        enableBottom()
        enableProceed()
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(getString(R.string.select_vendor), VENDOR) }
        binding.spWareHouse.setOnClickListener { showSingleSelectDialog(getString(R.string.select_material), MATERIAL)/*showMaterialDialog(thirdPartyMaterials)*/ }
        binding.tvProcureTypeValue.setOnClickListener { showSingleSelectDialog(getString(R.string.procure_type), PROCURE_TYPE)}
        binding.tvPoValue.setOnClickListener { showSingleSelectDialog(getString(R.string.select_po), PO)}
        binding.clInventory.setOnClickListener {
            moveToLotList()
        }
        binding.btnInventory.setOnClickListener {
            moveToLotList()
        }
        binding.btAdd.setOnClickListener { vm.validateLot(binding.etEnterContainer.text.toString()) }
        binding.btProceed.setOnClickListener { validateProceed() }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.etEnterContainer.text.toString()
                )
        })
        vm.getSuppliers()
        if (getCurrentKey().split("_")[1].contains("NI")) vm.getSuppliers("NI01")
        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            materialList = it
            thirdPartyMaterials = it.filter { it.thirdPartyFlag.equals("X") }
        })

        vm.trackTraceFeatureMaster.observe(viewLifecycleOwner, Observer {
//            ttFeatureList = it as java.util.ArrayList<VegaFeatureMaster>
            ttFeatureList = it.filter { it.featureName.equals(Constants.DIRECT) || it.featureName.equals(Constants.IN_DIRECT) } as java.util.ArrayList<VegaFeatureMaster>
            if(ttFeatureList.size>0){
                showTTDialog(R.string.select_procurement_type, false)
            }
        })
        vm.getTTFeatureMaster(Constants.TRACK_TRACE)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            binding.tvProcureType.visible()
            binding.tvProcureTypeValue.visible()
            binding.tvGrnPrice.gone()
            binding.tvGrnPriceValue.gone()

            vm.filteredsupplier.observe(viewLifecycleOwner, Observer {
                supplierList = it.toMutableList()
            })

            vm.poList.observe(viewLifecycleOwner, Observer { response ->
                response.let {
                    when (it.status) {
                        Resource.Status.SUCCESS -> {
                            hideLoading()
                            when (it.data?.success) {
                                true -> {
                                    response.data?.data?.let {
                                        val poList = it.toMutableList()
                                        poList.forEach { po ->
                                            if (po.poType == "Z001") purchaseOrderList.add(po)
                                        }

                                    }
                                }
                                else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                            }
                        }
                        Resource.Status.LOADING -> showLoading()
                        Resource.Status.ERROR -> {
                            hideLoading()
                            showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                        }
                    }
                }
            })
            vm.getPOList()
        }else
            vm.suppplier.observe(viewLifecycleOwner, Observer {
                /*supplierList = it.filter { data -> data.vendorCode.startsWith("2", true) } as MutableList*/
                supplierList = it.toMutableList()
            })

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })

        vm.thirdPartyInfo.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateLocalDBData(it) })
        binding.tvGrnPriceValue.onChange { enableProceed() }
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
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btAdd,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
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

    private fun showSingleSelectDialog(title: String, currentFlag: String) {
        val list = ArrayList<String>()
        when (currentFlag) {
            VENDOR -> {
                list.addAll(fromVendorList.map { it.vendorCode.plus("-").plus(it.vendorName) })
            }
            MATERIAL->{
                if(ttFeatureList.size>0){
                    if(isComplaintMaterial){
                        thirdPartyMaterials.forEach {tm->
                            var complaintMaterials = materialList.filter { it.materialCode.equals(tm.thirdPartyMaterialCode) && it.complainceFlag.equals(Constants.COMPLAINT) }
                            if(complaintMaterials.size > 0) {
                                list.addAll(complaintMaterials.map { tm.materialName ?: "" })
                            }
                        }
                    } else {
                        thirdPartyMaterials.forEach {tm->
                            var nonComplaintMaterials = materialList.filter { it.materialCode.equals(tm.thirdPartyMaterialCode) && it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                            if(nonComplaintMaterials.size>0) {
                                list.addAll(nonComplaintMaterials.map { tm.materialName ?: "" })
                            }
//                            list.addAll(nonComplaintMaterials.map { it.materialName?:"" })
                        }
                    }
                    if(list.size == 0){
                        list.addAll(thirdPartyMaterials.map { it.materialName ?: "" })
                    }
                } else {
                    list.addAll(thirdPartyMaterials.map { it.materialName ?: "" })
                }
            }
            PO->{
                list.addAll(matPurchaseOrderList.filter { it.supplier.contains(fromVendor.vendorCode) }.map { it.poId})
            }
            PROCURE_TYPE->{
                list.addAll(resources.getStringArray(R.array.procurementType))
            }
        }

        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFlag,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog?.dismiss()
        when(currentFlag){
            VENDOR->{
                val code = data.split("-")
                binding.tvstoValue.text = data
                fromVendor = supplierList.single { it.vendorCode == code[0] }
                fetchLocalDBData()
                enableBottom()
                clearLotList()
                enableProceed()
            }
            MATERIAL->{
                //binding.spWareHouse.text = data
                thirdPartyMaterials.find {it.materialName.equals(data) }.apply {
                    materialCode = this?.materialCode.toString()
                    materialName = this?.materialName.toString()
                    binding.spWareHouse.text = this?.materialName.toString()
                    matPurchaseOrderList.clear()
                    matPurchaseOrderList.addAll(purchaseOrderList.filter { it.material.contains(this?.thirdPartyMaterialCode.toString()) })
//                    vm.model.eudrStatus = materialList.singleOrNull { it.materialCode.equals(materialCode) == true }?.complainceFlag?:""
                }
                enableBottom()
                clearLotList()
                enableProceed()
                binding.tvstoValue.text = ""
                vm.stockLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
                vm.getStockList(getMaterialList())
            }
            PO->{
                binding.tvPoValue.text = data
                matPurchaseOrderList.find { it.poId.equals(data) }.apply {
                    val poDetail = PODetails()
                    poDetail.meins = this?.meins.toString()
                    poDetail.menge = this?.menge.toString()
                    poDetail.poId = this?.poId.toString()
                    poDetail.unitPrice = this?.unitPrice.toString()
                    poDetail.openQuantity = this?.openQuantity.toString()
                    poDetail.storageLocationCode = this?.storageLocationCode.toString()
                    poDetail.bsart = this?.bsart.toString()
                    poDetail.ebelp = this?.ebelp.toString()
                    poDetail.ekgrp = this?.ekgrp.toString()
                    poDetail.ekorg = this?.ekorg.toString()
                    poDetail.poType = this?.poType.toString()
                    poDetail.supplierCode = this?.supplier.toString()
                    vm.model.poDetails = poDetail
                }
                enableProceed()
            }
            PROCURE_TYPE->{
                binding.tvProcureTypeValue.text = data
                vm.model.procureType = data
                when{
                    data.contains("Fixed", true)->{
                        binding.tvPo.visible()
                        binding.tvPoValue.visible()
                        binding.tvGrnPrice.gone()
                        binding.tvGrnPriceValue.gone()
                    }
                    data.contains("Spot", true)->{
                        binding.tvPo.gone()
                        binding.tvPoValue.gone()
                        binding.tvGrnPrice.visible()
                        binding.tvGrnPriceValue.visible()
                    }
                }
                enableProceed()
            }
        }
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
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun filterFromVendor() {
        fromVendorList.clear()
        val fromVendor = dispatchLotsList.map { it.vendor ?: "" }.toSet().toList()
        supplierList.forEach {
            if (fromVendor.toString().contains(it.vendorCode)) {
                fromVendorList.add(it)
            }
        }
        hideLoading()
    }

    private fun showMaterialDialog(materialList: List<VegaMaterial>) {
        val suppliers = materialList.map { it.materialName ?: "" }
        MaterialDialog(requireContext()).show {
            title(R.string.select_tolling_material)
            listItemsSingleChoice(items = suppliers) { _, index, text ->
                binding.spWareHouse.text = text
                materialCode = materialList[index].materialCode
                materialName = materialList[index].materialName ?: ""
                enableBottom()
                clearLotList()
                enableProceed()
                binding.tvstoValue.text = ""
                vm.stockLots.observe(viewLifecycleOwner, Observer { updateUI(it) })
                vm.getStockList(getMaterialList())
            }
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            )
        }
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

    private fun getMaterialList(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add(materialCode)
        return list
    }

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
            model.grnPrice = binding.tvGrnPriceValue.text.toString()
            model.transferType = saleType
            model.createDate = vm.getCurrentDate()
            model.vendorWithTransferType = materialCode.plus(fromVendor.vendorCode).plus(saleType)
            vm.model = model
            vm.saveThirdPartyInfo()
        }
    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        val lots = list
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_third_party_add_lot,
            ItemThirdPartyAddLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocationValue1.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    /*bindItem.etWeight.isEnabled = false
                    bindItem.etWeight.isClickable = false
                    bindItem.cbSelectAll.visibility = View.INVISIBLE
                    bindItem.tvSelectAll.visibility = View.INVISIBLE
                    bindItem.etWeight.setText(
                        item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                            ?.plus(item.unitOfMeasure)
                    )
                    item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()
                    item.isLowerWeight = true*/
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.etWeight.setText(editedWeight)
                    item.weightToDispatchUOM = item.unitOfMeasure
                } else {
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.etWeight.setText(editedWeight)
                }
                bindItem.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.isChecked = isChecked
                    if (isChecked) {
                        item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()

                    } else {
                        item.editedWeight = "0.0"
                    }
                    bindItem.etWeight.setText(item.editedWeight)
                }
                bindItem.etWeight.onChange {it1->
                    val it = it1.filter { it.isDigit() }
                    if (it.isNotEmpty()) {
                        item.editedWeight = it1.trim()
                        val come: Int? = it1.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
                        if (come ?: 0 <= 0) {
                            item.isLowerWeight = true
                        } else {
                            item.isLowerWeight = false
                            bindItem.etWeight.error =
                                bindItem.etWeight.context.getString(R.string.less_weight_error)
                        }
                    }
                }
                bindItem.ivClose.setOnClickListener {
                    vm.lotList.remove(item)
                    binding.rvLots.adapter?.notifyItemRemoved(pos)
                    vm.removeLot(item.batchNumber)
                }
            })
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

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    vm.model.grnPrice = binding.tvGrnPriceValue.text.toString()
                    moveToSummary()
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

    private fun moveToSummary() {
        vm.saveThirdPartyInfo()
        if(isComplaintMaterial){
            vm.model.eudrStatus = Constants.EUDR_QP_VALUE
        } else {
            vm.model.eudrStatus = Constants.ATTR_UNKNOWN_QP_VALUE
        }
        callBack?.replaceFragment(Ownership_Transfer_Summary, vm.model)
    }

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
                        } else showErrorDialogWithFAQLink(
                            requireContext(),
                            getString(R.string.vendor_code_not_matched)
                        )

                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateLocalDBData(data: VegaCoffeeThirdPartyModelWithLots) {
        binding.tvGrnPriceValue.setText(data.model.toVendorName)
        if (data.lots != null) {
            vm.lotList = data.lots as ArrayList<VegaCocoaDispatchLots>
        }
        setUpAdapter(vm.lotList)
        enableBottom()
        enableProceed()
    }

    private fun updateAdapter(list: List<VegaCocoaDispatchLots>) {
        val addedNew = ArrayList<VegaCocoaDispatchLots>()
        val removedLots = ArrayList<VegaCocoaDispatchLots>()
        val batchMap = vm.lotList.map { it.batchNumber }
        val batchNewMap = list.map { it.batchNumber }
        list.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                it.vendorWithTransferType = vm.model.vendorWithTransferType
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
        list.forEach { it.apply { vendor = fromVendor.vendorCode } }
        vm.addLoTInDB(addedNew)
        vm.lotList.addAll(addedNew)
        setUpAdapter(vm.lotList)
        enableProceed()
    }

    fun updateLotList(lots: ArrayList<VegaCocoaDispatchLots>) {
        updateAdapter(lots)
    }

    fun saveAndBack() {
        vm.saveThirdPartyInfo()
    }

    private fun chooseOneLotDialog(lots: List<VegaCocoaDispatchLots>) {
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
                updateAdapter(lots)
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun enableProceed() {
        val enable =
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && validateInputBasedOnProcureType() && vm.lotList.isNotEmpty()
        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
    }

    private fun validateInputBasedOnProcureType(): Boolean{
        return when{
            vm.model.procureType.contains("Fixed", true)-> binding.tvPoValue.text.toString().isNotEmpty()
            vm.model.procureType.contains("Spot", true)-> binding.tvGrnPriceValue.text.toString().isNotEmpty()
            else-> binding.tvGrnPriceValue.text.toString().isNotEmpty()
        }
    }

    fun showTTDialog(msg:Int, procurementType:Boolean) {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(msg)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",
                {
                        isComplaint, isThirdParty, isFarmerLessTransaction ->  if(isComplaint) isComplaintMaterial = true else false

                },

                /*{
                   if(it == true){
                        navigateToTrackTracePage(navigationType, DIRECT,"")
                    }else{
                        navigateToTrackTracePage(navigationType, IN_DIRECT,"")
                    }
                }*/
                { dismiss() },procurementType)
        }
    }
}
