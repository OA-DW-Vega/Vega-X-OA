package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.ownership

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
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartycocoa.R
import com.olam.warehouse.vegax.thirdpartycocoa.databinding.FragmentCocoaThirdPartyOwnershipAddLotBinding
import com.olam.warehouse.vegax.thirdpartycocoa.databinding.ItemCocoaThirdPartyAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCocoaThirdPartyViewModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.LOT_LIST
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.Ownership_Transfer_Summary
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.TP_TO_TP
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCocoaThirdPartyOwnershipAddLotFragment : BaseFragment(), VegaSingleSelectListener {

    private lateinit var saleType: String
    override val layoutResourceId = R.layout.fragment_cocoa_third_party_ownership_add_lot
    private lateinit var binding: FragmentCocoaThirdPartyOwnershipAddLotBinding
    private var callBack: VegaCocoaThirdPartyReplaceFragmentCallback? = null
    private val vm: VegaCocoaThirdPartyViewModel by viewModel()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var fromVendor = VegaVendor()
    private var toVendor = VegaVendor()
    private var materialCode: String = ""
    private var materialName: String = ""
    private var thirdPartyMaterials: List<VegaMaterial> = mutableListOf()
    private var moreWeightBatches = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var fromVendorList = ArrayList<VegaVendor>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCocoaThirdPartyReplaceFragmentCallback
    }

    companion object {
        fun newInstance(type: String) = VegaCocoaThirdPartyOwnershipAddLotFragment()
            .putArgs {
                putString("type", type)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCocoaThirdPartyOwnershipAddLotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("thirdpartysalescoffee/ui/ownership/VegaCocoaThirdPartyOwnershipAddLotFragment")
            .title("Third party sales Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        saleType = arguments?.getString("type") ?: ""
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        enableBottom()
        enableProceed()
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, "Select from vendor", false) }
        binding.tvvendorvalue.setOnClickListener { showSingleSelectDialog(false, "Select To vendor", false) }
        binding.spWareHouse.setOnClickListener { showMaterialDialog(thirdPartyMaterials) }
        binding.clInventory.setOnClickListener {
            moveToLotList()
        }
        binding.btnInventory.setOnClickListener { moveToLotList() }
        vm.getSuppliers()
        binding.btAdd.setOnClickListener { vm.validateLot(binding.etEnterContainer.text.toString()) }
        binding.btProceed.setOnClickListener { validateProceed() }

        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.etEnterContainer.text.toString()
                )
        })

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            /*supplierList = it.filter { data -> data.vendorCode.startsWith("2", true) } as MutableList*/
            supplierList = it.toMutableList()
        })

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })

        vm.thirdPartyInfo.observe(viewLifecycleOwner, Observer { if (it != null) updateLocalDBData(it) })
    }

    private fun updateLocalDBData(data: VegaCoffeeThirdPartyModelWithLots) {
        toVendor = supplierList.singleOrNull { data.model.toVendorCode == it.vendorCode } ?: VegaVendor()
        binding.tvvendorvalue.text = data.model.toVendorCode.plus("-").plus(data.model.toVendorName)
        if (data.lots != null) {
            vm.lotList = data.lots as ArrayList<VegaCocoaDispatchLots>
        }
        setUpAdapter(vm.lotList)
        enableBottom()
        enableProceed()
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
                            "Vendor details is not matched with Lot details"
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

    private fun moveToLotList() {
        callBack?.replaceFragment(
            LOT_LIST,
            VegaCoffeeThirdPartyLotListModel(
                selectedList = vm.lotList,
                isMultipleAdd = false,
                material = getMaterialList(), vendorCode = fromVendor.vendorCode
            )
        )
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

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        val lots = list
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_cocoa_third_party_add_lot,
            ItemCocoaThirdPartyAddLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocationValue1.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                item.editedWeight = item.weight
                val editedWeight =
                    if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                bindItem.etWeight.setText(editedWeight)
                bindItem.etWeight.gone()
                bindItem.tvSelectAll.gone()
                bindItem.cbSelectAll.gone()
                bindItem.tvEditedWeight.visible()
                bindItem.tvEditedWeight.text = editedWeight
                bindItem.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.isChecked = isChecked
                    if (isChecked) {
                        item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()

                    } else {
                        item.editedWeight = "0.0"
                    }
                    bindItem.etWeight.setText(item.editedWeight)
                }
                bindItem.etWeight.onChange {
                    if (it.isNotEmpty()) {
                        item.editedWeight = it
                        val come: Int? = it.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
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
                    showConformationDialog(pos, bindItem.ivClose)
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
        callBack?.replaceFragment(Ownership_Transfer_Summary, vm.model)
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

    private fun showSingleSelectDialog(isWh: Boolean, title: String, isVendor: Boolean) {
        val list = ArrayList<String>()
        if (isWh) {
            val subList = fromVendorList.filter { it.vendorCode != toVendor.vendorCode }
            list.addAll(subList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        } else {
            val subList = supplierList.filter { it.vendorCode != fromVendor.vendorCode }
            list.addAll(subList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        }

        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, isVendor, false,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        val code = data.split("-")
        if (isWh) {
            binding.tvstoValue.text = data
            fromVendor = supplierList.single { it.vendorCode == code[0] }
            clearLotList()
            fetchLocalDBData()
            enableBottom()
            enableProceed()
        } else {
            binding.tvvendorvalue.text = data
            toVendor = supplierList.single { it.vendorCode == code[0] }
            enableBottom()
            enableProceed()
        }
    }

    private fun enableBottom() {
        val enable =
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvvendorvalue.text.isNotEmpty()
        binding.clScan.isClickable = enable
        binding.clScan.isEnabled = enable
        binding.etEnterContainer.isEnabled = enable
        binding.btnInventory.isClickable = enable
        binding.clInventory.isClickable = enable
        binding.btnInventory.isEnabled = enable
        binding.clInventory.isEnabled = enable
        if (enable) {
            val model = VegaCoffeeThirdPartyRequestModel(
                materialCode, materialName, fromVendor.vendorName ?: "", fromVendor.vendorCode,
                toVendor.vendorCode, toVendor.vendorName, "", vm.getCurrentDate(), 1, false
            )
            model.transferType = saleType
            model.vendorWithTransferType = materialCode.plus(fromVendor.vendorCode).plus(saleType)
            vm.model = model
            vm.saveThirdPartyInfo()
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

    private fun getMaterialList(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add(materialCode)
        return list
    }

    private fun fetchLocalDBData() {
        if (binding.tvstoValue.text.isNotEmpty() && materialCode.isNotEmpty()) {
            vm.getThirdPartyLocalData(materialCode.plus(fromVendor.vendorCode).plus(TP_TO_TP))
        }
    }

    private fun updateAdapter(list: List<VegaCocoaDispatchLots>) {
        binding.etEnterContainer.setText("")
        if (vm.lotList.isNotEmpty())
            vm.removeLot(vm.lotList[0].batchNumber)
        list.forEach {
            it.apply {
                vendor = fromVendor.vendorCode
                weightToDispatchUOM = unitOfMeasure
                vendorWithTransferType = vm.model.vendorWithTransferType
            }
        }
        vm.addLoTInDB(list)
        vm.lotList.addAll(list)
        setUpAdapter(list as ArrayList<VegaCocoaDispatchLots>)
        enableProceed()
    }

    fun updateLotList(lots: ArrayList<VegaCocoaDispatchLots>) {
        updateAdapter(lots)
    }

    fun saveAndBack() {
        vm.saveThirdPartyInfo()
    }

    private fun clearLotList() {
        vm.lotList.clear()
        setUpAdapter(vm.lotList)
        enableProceed()
    }

    private fun enableProceed() {
        val enable =
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && binding.tvvendorvalue.text.isNotEmpty() && vm.lotList.isNotEmpty()

        if (enable) {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btProceed.isEnabled = enable
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
                    vm.lotList.clear()
                    setUpAdapter(vm.lotList)
                },
                { dismiss() })
        }
    }
}
