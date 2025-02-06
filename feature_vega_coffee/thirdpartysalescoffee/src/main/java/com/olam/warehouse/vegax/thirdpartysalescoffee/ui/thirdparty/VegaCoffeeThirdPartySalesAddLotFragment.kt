package com.olam.warehouse.vegax.thirdpartysalescoffee.ui.thirdparty

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
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeThirdPartyLotListModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeWSBagModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartySalesAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.ItemCoffeeTpLotWithAddWeightBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyReplaceFragmentCallback
import com.olam.warehouse.vegax.thirdpartysalescoffee.ui.VegaCoffeeThirdPartyViewModel
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCoffeeThirdPartySalesAddLotFragment : BaseFragment(), VegaSingleSelectListener {

    private var addWeightPosition: Int = 0
    override val layoutResourceId = R.layout.fragment_third_party_sales_add_lot
    private lateinit var binding: FragmentThirdPartySalesAddLotBinding
    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null
    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var thirdPartyMaterials: List<VegaMaterial> = mutableListOf()
    private var supplierList = mutableListOf<VegaVendor>()
    private var fromVendor = VegaVendor()
    private var toVendor = VegaVendor()
    private var materialCode: String = ""
    private var materialName: String = ""
    private var moreWeightBatches = ""
    private var saleType = ""
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private var fromVendorList = ArrayList<VegaVendor>()
    private var palletCountNotMatchBatches = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
    }

    companion object {
        fun newInstance(type: String) = VegaCoffeeThirdPartySalesAddLotFragment()
            .putArgs {
                putString("type", type)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartySalesAddLotBinding.inflate(layoutInflater)
        if (getCurrentKey().split("_")[1].contains("NI")) binding.tvSummary.text =
            getString(R.string.lots_summary)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaThirdPartySalesAddLotFragment")
            .title("IVC/Coffee/Third Party Sales/Sales Add Lot").with(tracker)
    }

    private fun initUI() {

        saleType = arguments?.getString("type") ?: ""
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        enableBottom()
        enableProceed()
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog() }
        binding.spWareHouse.setOnClickListener { showMaterialDialog(thirdPartyMaterials) }
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
            thirdPartyMaterials = it
        })
        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.filteredsupplier.observe(viewLifecycleOwner, Observer {
                supplierList = it.toMutableList()
            })
        } else {
            vm.suppplier.observe(viewLifecycleOwner, Observer {
                /*supplierList = it.filter { data -> data.vendorCode.startsWith("2", true) } as MutableList*/
                supplierList = it.toMutableList()
                //fromVendorList = supplierList as ArrayList<VegaVendor>
            })
        }
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })

        vm.thirdPartyInfo.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateLocalDBData(it) })
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

    private fun showSingleSelectDialog() {
        var list = ArrayList<String>()
        //list = supplierList.map { it.vendorCode.plus("-").plus(it.vendorName) } as ArrayList<String>
        list.addAll(fromVendorList.map { it.vendorCode.plus("-").plus(it.vendorName) })
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                getString(R.string.select_from_vendor),
                true, false, false,
                list,
                requireActivity(),
                this, isOrigin = false, isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        val code = data.split("-")
        binding.tvstoValue.text = data
        if (getCurrentKey().split("_")[1].contains("NI")) {
            fromVendor =
                supplierList.single { it.vendorCode == code[0] && it.vendorName == code[1] }
        } else
            fromVendor = supplierList.single { it.vendorCode == code[0] }
        clearLotList()
        fetchLocalDBData()
        enableBottom()
        enableProceed()
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
                clearLotList()
                binding.tvstoValue.text = ""
                enableBottom()
                enableProceed()
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
            vm.getThirdPartyLocalData(materialCode.plus(fromVendor.vendorCode).plus(SAME_TP))
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
            model.toVendorCode = fromVendor.vendorCode
            model.toVendorName = fromVendor.vendorName
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
            R.layout.item_coffee_tp_lot_with_add_weight,
            ItemCoffeeTpLotWithAddWeightBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocationValue1.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                if (getCurrentKey().split("_")[1].contains("NI")) {
                   /* bindItem.tvEditedWeight.text = item.weight?.toDouble()
                        ?.formatThreeDigits().plus(" ").plus(item.unitOfMeasure)
                    bindItem.tvEditedWeight.visibility = View.VISIBLE
                    item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()
                    item.isLowerWeight = true*/
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.tvEditedWeight.setText(editedWeight)
                    bindItem.tvEditedWeight.visibility = View.VISIBLE
                    item.weightToDispatchUOM = item.unitOfMeasure
                } else {
                    bindItem.tvEditedWeight.setBackgroundResource(0)
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.tvEditedWeight.setText(editedWeight)
                    bindItem.tvEditedWeight.visibility = View.VISIBLE
                }
                /*bindItem.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                    item.isChecked = isChecked
                    if (isChecked) {
                        item.editedWeight = item.weight?.toDouble()?.formatThreeDigits()

                    } else {
                        item.editedWeight = "0.0"
                    }
                    bindItem.tvEditedWeight.setText(item.editedWeight)
                }*/

                bindItem.tvEditedWeight.onChange {it1->
                    val it = it1.filter { it.isDigit() }
                    if (it.isNotEmpty()) {
                        item.editedWeight = it
                        val come: Int? = it.toDouble().compareTo(item.weight?.toDouble() ?: 0.0)
                        if (come ?: 0 <= 0) {
                            item.isLowerWeight = true
                        } else {
                            item.isLowerWeight = false
                            bindItem.tvEditedWeight.error =
                                bindItem.tvEditedWeight.context.getString(R.string.less_weight_error)
                        }
                    }
                }
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    bindItem.ivClose.setOnClickListener {
                        vm.lotList.remove(item)
                        binding.rvLots.adapter?.notifyItemRemoved(pos)
                        vm.removeLot(item.batchNumber)
                    }
                } else {
                    bindItem.ivClose.setOnClickListener {
                        showConformationDialog(pos, bindItem.ivClose)
                    }
                }
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    bindItem.tvAddWeight.visibility = View.GONE
                } else {
                    bindItem.tvAddWeight.setOnClickListener {
                        addWeightPosition = pos
                        callBack?.replaceFragment(ADD_WEIGHT, item)
                    }
                }
            })
    }

    fun updateAddWeight(weight: VegaCoffeeWSBagModel) {
        val split = weight.weight.split(" ")
        vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = split[1]
        binding.rvLots.adapter?.notifyItemChanged(addWeightPosition)
        val come: Int? = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.lotList[addWeightPosition].isBagCountMatched = weight.isPalletMatched
        vm.addLoTInDB(vm.lotList[addWeightPosition])
    }

    private fun validatePalletCount(): Boolean {
        val selected = vm.lotList.filter { !it.isBagCountMatched }
        palletCountNotMatchBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
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
                    if (validatePalletCount())
                        moveToSummary()
                    else Toast.makeText(
                        activity,
                        getString(R.string.pallet_not_matched).plus(" ").plus(palletCountNotMatchBatches),
                        Toast.LENGTH_SHORT
                    ).show()
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
        toVendor = supplierList.singleOrNull { data.model.toVendorCode == it.vendorCode } ?: VegaVendor()
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
            materialCode.isNotEmpty() && binding.tvstoValue.text.isNotEmpty() && vm.lotList.isNotEmpty()

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
                    vm.lotList.removeAt(position)
                    binding.rvLots.adapter?.notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }
}
