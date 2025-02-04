package com.olam.warehouse.vegax.salescocoa.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesOrderModel
import com.olam.warehouse.vegax.salescocoa.databinding.FragmentCocoaSalesCreateDispatchBinding
import com.olam.warehouse.vegax.salescocoa.utils.*
import kotlinx.android.synthetic.main.fragment_cocoa_sales_create_dispatch.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VegaCocoaSalesAddLotFragment : BaseFragment(), ItemRemoveListener,
    AddWeightListener, VegaSingleSelectListener {

    private var callBack: CallBack? = null
    private val vm: VegaCocoaSalesViewModel by viewModel()
    private var adapter: VegaCocoaSalesLotAdapter? = null
    private var dispatchType = ""
    private var salesOrderList = mutableListOf<VegaCocoaSalesOrderModel>()
    private var materialCode: String = ""
    private var materialList = ArrayList<String>()
    private var soNumbers = ArrayList<String>()
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    private var isEditableLot = true
    private var isMultipleLot = false
    private var isAlreadyLoading = false
    private var isReceived = false
    private var isRoundOff = false
    private var addWeightPosition = 0
    private var materialNameList = ArrayList<String>()
    private var thirdPartyMaterialList = ArrayList<VegaMaterial>()
    private var isAnticipatedThirdParty = true
    private var vendorList = ArrayList<VegaVendor>()
    private var vendorCustomerCode: String = ""
    override val layoutResourceId: Int = R.layout.fragment_cocoa_sales_create_dispatch
    private lateinit var binding: FragmentCocoaSalesCreateDispatchBinding

    companion object {
        fun newInstance(dispatch: VegaCocoaSalesWB, dispatchType: String, isRoundOff: Boolean) =
            VegaCocoaSalesAddLotFragment().putArgs {
                putParcelable(BUNDLE_MODEL, dispatch)
                putString(BUNDLE_TYPE, dispatchType)
                putBoolean(ROUND_OFF, isRoundOff)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/VegaCocoaSalesAddLotFragment")
            .title("Sales Cocoa")
            .with(tracker)
    }

    /* override fun onPrepareOptionsMenu(menu: Menu) {
         menu.clear()
     }*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentCocoaSalesCreateDispatchBinding.inflate(inflater)
        initExtra()
        //initUI()
        /*listener = this*/
        return binding.root
    }

    interface CallBack {
        fun replaceFragment(fragment: String, wb: VegaCocoaSalesWB, salesType: String, isRoundoff: Boolean)
        fun replaceFragment(fragment: String, lot: VegaCocoaSalesLots, isRoundoff: Boolean)
        fun replaceFragment(
            fragment: String,
            model: VegaCocoaSalesWB,
            materialList: ArrayList<String>,
            isMultipleLot: Boolean, isRoundoff: Boolean
        )
    }

    private fun initExtra() {
        enableEnterLotId(false)
        isRoundOff = arguments?.getBoolean(ROUND_OFF) as Boolean
        vm.getConfigItems(UserRoles.SALES.role)
        vm.getThirdPartyMaterials()
        vm.getSuppliers()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vendorList.addAll(it)
            }
        })
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                thirdPartyMaterialList.addAll(it)
            }
        })
        vm.dispatchWh = arguments?.getParcelable<VegaCocoaSalesWB>(BUNDLE_MODEL) as VegaCocoaSalesWB

        dispatchType = arguments?.getString(BUNDLE_TYPE) ?: ""
        vm.dispatchWh.salesType = dispatchType
        vm.dispatchWB.observeOnce(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it
                updateUIWithLocalData()
            }
            vm.getPurchaseOrder()
            vm.getLotsDataFromLocal(vm.dispatchWh.weighBridgeId)
        })
        vm.dispatchLots.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                vm.lots.clear()
                adapter?.clear()
                vm.lots.addAll(it)
                adapter?.addAllLots(vm.lots)
                enableProceed(true)
            }
            enableEnterLotId(vm.dispatchWh.saleOrderId.isNotEmpty())
            enableSave(vm.dispatchWh.saleOrderId.isNotEmpty())
        })
        initUI()
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        vm.getWeighBrideData(vm.dispatchWh.weighBridgeId)
    }

    private fun initUI() {
        binding.tvMaterialName.text = vm.dispatchWh.materialName ?: ""
        var titleString = ""
        when (dispatchType) {
            SALES_TYPE_WEIGHSCALE -> titleString = getString(R.string.weighscale)
            SALES_TYPE_ANTICIPATED -> titleString = getString(R.string.anticipated)
            SALES_TYPE_ANTICIPATED_VIRTUAL -> titleString = getString(R.string.anticipated)
            SALES_TYPE_WEIGHBRIDGE -> titleString = getString(R.string.weighbridge)
        }
        binding.tvTitle.text = getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(titleString)
        binding.btProceed.setOnClickListener {
            if (vm.lots.isNotEmpty())
                if (validateLotWeight()) {
                    if (validateZeroWeight()) {
                        if (!dispatchType.equals(SALES_TYPE_WEIGHSCALE, true) || binding.etOperatorName.text?.toString()
                                ?.isNotEmpty()!!
                        ) {
                            vm.dispatchWh.salesType = dispatchType
                            vm.dispatchWh.operatorName = binding.etOperatorName.text?.toString()
                            vm.saveWeighBridgeAndLotDetails()
                            vm.dispatchWh.lotList = vm.lots
                            callBack?.replaceFragment(SUMMARY, vm.dispatchWh, dispatchType, isRoundOff)
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.enter_operator_name),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else Toast.makeText(
                        activity,
                        getString(R.string.zero_weight_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    activity,
                    getString(R.string.less_weight_error),
                    Toast.LENGTH_SHORT
                ).show()
            else Toast.makeText(
                activity,
                getString(R.string.prceed_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.tvInventory.setOnClickListener {
            val list = ArrayList<String>()
            list.add(vm.dispatchWh.materialCode ?: "")
            if (materialList.isNotEmpty())
                callBack?.replaceFragment(
                    "INVENTORY_FRAG",
                    vm.dispatchWh,
                    list, isMultipleLot, isRoundOff
                )
        }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.btAdd.setOnClickListener {
            vm.validateLot(etEnterContainer.text.toString())
            isReceived = false
        }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (!isAlreadyLoading) {
                isAlreadyLoading = true
                if (it != null) {
                    showLotAlreadyExistDialog(true)
                    etEnterContainer.setText("")
                    isAlreadyLoading = false
                } else
                    fetchLotDetails(
                        etEnterContainer.text.toString(),
                        vm.dispatchWh.materialCode ?: "", vm.dispatchWh.plantId ?: ""
                    )
            }
        })

        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
            initAdapter(isRoundOff)
        })

        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so)) }
        binding.tvMaterialName.setOnClickListener { showSingleSelectDialog(false, getString(R.string.select_material)) }
        when {
            dispatchType.equals(SALES_TYPE_WEIGHSCALE, true) -> {
                binding.btSave.visibility = View.VISIBLE
                binding.ivProceed.visibility = View.VISIBLE
                binding.tvOperatorName.visibility = View.VISIBLE
                binding.etOperatorName.visibility = View.VISIBLE
            }
            else -> {
                binding.btSave.visibility = View.GONE
                binding.ivProceed.visibility = View.GONE
            }
        }
        binding.btSave.setOnClickListener {
            getCurrentDate()
            vm.dispatchWh.operatorName = binding.etOperatorName.text.toString()
            vm.dispatchWh.isRoundOff = isRoundOff
            vm.saveWeighBridgeAndLotDetails()
            Toast.makeText(activity, getString(R.string.local_save_msg), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate() {
        if (SALES_TYPE_WEIGHBRIDGE != dispatchType) {
            val stamp = Timestamp(System.currentTimeMillis())
            val date = Date(stamp.time)
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val dat = dateFormat.format(date)
            vm.dispatchWh.ertim = dat
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (!isReceived) {
                        isReceived = true
                        isAlreadyLoading = false
                        response.data?.data?.let { it1 ->
                            when (it1.size == 1) {
                                true -> updateAdapter(it1[0])
                                else -> chooseOneLotDialog(it1)
                            }

                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    if (!isReceived) {
                        isReceived = true
                        isAlreadyLoading = false
                        UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    }
                }
            }
        }
    }

    private fun chooseOneLotDialog(lots: List<VegaCocoaSalesLots>) {
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

    private fun updateAdapter(vegaCocoaDispatchLots: VegaCocoaSalesLots) {
        vegaCocoaDispatchLots.salesOrderId = vm.dispatchWh.saleOrderId
        vegaCocoaDispatchLots.salesType = vm.dispatchWh.salesType
        if (SALES_TYPE_ANTICIPATED_VIRTUAL == dispatchType) {
            vegaCocoaDispatchLots.vendor = vendorCustomerCode
        }
        if (SALES_TYPE_WEIGHBRIDGE != dispatchType) getCurrentDate()
        vm.saveWeighBridgeAndLotDetails()
        binding.etEnterContainer.setText("")
        enableProceed(true)
        enableEnterLotId(isMultipleLot)
        vm.lots.add(vegaCocoaDispatchLots)
        adapter?.addLot(vegaCocoaDispatchLots)
    }

    private fun showLotAlreadyExistDialog(isExist: Boolean) {
        MaterialDialog(requireContext()).show {
            message(if (isExist) R.string.already_added else R.string.details_lose)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                vm.lots.forEach { vm.removeLotFromList(it.batchNumber) }
                dismiss()
            }
        }
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

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    private fun updateUIWithLocalData() {
        binding.tvCustomerValue.text = vm.dispatchWh.customerName
        binding.tvStoWeightValue.text = vm.dispatchWh.soWeight
        binding.tvstoValue.text = vm.dispatchWh.saleOrderId
        binding.tvMaterialName.text = vm.dispatchWh.materialName
        binding.etOperatorName.setText(vm.dispatchWh.operatorName.toString())
    }

    private fun initAdapter(isRoundOff: Boolean) {
        adapter =
            VegaCocoaSalesLotAdapter(
                ArrayList(),
                isEditableLot,
                true, false,
                SALES_TYPE_WEIGHSCALE.equals(dispatchType, true),
                this,
                this, isRoundOff
            )
        val manager = LinearLayoutManager(activity)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = manager
        binding.rvLots.adapter = adapter
    }

    override fun itemRemoved(item: VegaCocoaSalesLots) {
        vm.removeLotFromList(item.batchNumber)
        vm.lots.remove(item)
        enableProceed(vm.lots.isNotEmpty())
        enableEnterLotId(vm.lots.isEmpty() || isMultipleLot)
    }

    override fun weightUpdated(item: VegaCocoaSalesLots) {
        vm.updateLotWeightInfo(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(
                        it,
                        vm.dispatchWh.materialCode ?: "",
                        vm.dispatchWh.plantId ?: ""
                    )
                }
            }
        }
    }

    private fun fetchLotDetails(lotId: String, materialCode: String, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, materialCode, whId)
    }


    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        return selected.isEmpty()
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaCocoaSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        salesOrderList = if (vm.dispatchWh.materialCode.isNullOrEmpty())
                            it1 as MutableList<VegaCocoaSalesOrderModel>
                        else
                            it1.filter { it.materialNumber == vm.dispatchWh.materialCode } as MutableList<VegaCocoaSalesOrderModel>
                        soNumbers.addAll(salesOrderList.listOfField(VegaCocoaSalesOrderModel::salesOrderId).toSet())
                        filterMaterial()
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

    private fun showSingleSelectDialog(isWh: Boolean, title: String) {
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                isWh, false, false,
                if (isWh) soNumbers else materialNameList,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun updateSODetails(selectedSales: VegaCocoaSalesOrderModel) {

        binding.tvMaterialName.text = selectedSales.materialDesc ?: ""
        binding.tvStoWeightValue.text = selectedSales.openQuantity?.toDouble()?.formatThreeDigits().plus(" ")
            .plus(selectedSales.meins.toString())
        vm.dispatchWh.unitsOfMeasure = selectedSales.meins
        binding.tvCustomerValue.text = selectedSales.soldToPartyName
        vm.dispatchWh.materialCode = selectedSales.materialNumber
        vm.dispatchWh.materialName = selectedSales.materialDesc
        vm.dispatchWh.customerId = selectedSales.soldToPartyCode
        vm.dispatchWh.customerName = selectedSales.soldToPartyName
        vm.dispatchWh.createdDate = selectedSales.createdDate
        vm.dispatchWh.salesItem = selectedSales.salesItemNum
        val weight = selectedSales.openQuantity?.toDouble()?.formatThreeDigits().plus(" ")
            .plus(selectedSales.meins.toString())
        vm.dispatchWh.soWeight = weight
        enableEnterLotId(true)
        if (vm.dispatchWh.weighBridgeId.isNotEmpty()) {
            vm.saveWeighBridgeAndLotDetails()
        }
        vm.getLotsDataFromLocal(vm.dispatchWh.weighBridgeId)
        enableSave(true)
        if (SALES_TYPE_ANTICIPATED_VIRTUAL == dispatchType)
            validateThirdPartyMaterialMapping(selectedSales.materialNumber ?: "")
    }

    private fun clearOldData() {
        binding.tvMaterialName.text = ""
        binding.tvStoWeightValue.text = ""
        binding.tvCustomerValue.text = ""
        vm.dispatchWh.materialCode = ""
        vm.dispatchWh.materialName = ""
        vm.dispatchWh.customerId = ""
        vm.dispatchWh.customerName = ""
        vm.dispatchWh.createdDate = ""
        vm.dispatchWh.salesItem = ""
        vm.dispatchWh.soWeight = ""
        enableEnterLotId(false)
        enableSave(false)
    }

    private fun filterMaterial() {
        materialList.addAll(salesOrderList.listOfField(VegaCocoaSalesOrderModel::materialNumber).toSet())
    }

    private fun enableEnterLotId(enable: Boolean) {
        val enabled = ((vm.lots.isEmpty() || isMultipleLot) && enable && isAnticipatedThirdParty)
        binding.etEnterContainer.isEnabled = enabled
        binding.clScan.isEnabled = enabled
        binding.clScan.isClickable = enabled
        binding.tvInventory.isClickable = enabled
        binding.tvInventory.isEnabled = enabled
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SCAN.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> removeScanLot(false)
                        it.applicable?.contains("N")!! -> removeScanLot(true)
                    }
                }
                ConfigItems.LOT_MANUAL.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            removeManualLot(false)
                        }
                        it.applicable?.contains("N")!! -> {
                            removeManualLot(true)
                        }
                    }
                }
                ConfigItems.LOT_WEIGHT_EDITABLE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isEditableLot = true
                        it.applicable?.contains("N")!! -> isEditableLot = false
                    }
                    adapter?.isEdit = isEditableLot
                    adapter?.notifyDataSetChanged()
                }
                ConfigItems.LOT_SELECTION_MULTI_WEIGHBRIDGE.item -> {
                    when {
                        (it.applicable?.contains("Y")!!) && (it.value != null && it.value.equals(
                            dispatchType,
                            true
                        )) -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
                ConfigItems.LOT_SELECTION_MULTI_WEIGHSCALE.item -> {
                    when {
                        (it.applicable?.contains("Y")!!) && (it.value != null && it.value.equals(
                            dispatchType,
                            true
                        )) -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
                ConfigItems.LOT_SELECTION_MULTI_ANTICIPATED.item -> {
                    when {
                        (it.applicable?.contains("Y")!!) && (it.value != null && it.value.equals(
                            dispatchType,
                            true
                        )) -> isMultipleLot = true
                        it.applicable?.contains("N")!! -> {
                            isMultipleLot = false
                        }
                    }
                }
            }
            adapter?.updateEditState(isEditableLot)
        }
    }

    private fun removeScanLot(isRemove: Boolean) {
        binding.clScan.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.tvOr.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun removeManualLot(isRemove: Boolean) {
        binding.etEnterContainer.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.btAdd.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun enableProceed(enable: Boolean) {
        binding.btProceed.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    private fun enableSave(enable: Boolean) {
        binding.btSave.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(
                    activity!!,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(activity!!, android.R.color.darker_gray)
            )
        }
    }

    override fun addWeightForLot(pos: Int, item: VegaCocoaSalesLots) {
        addWeightPosition = pos
        callBack?.replaceFragment(ADD_WEIGHT, item, isRoundOff)
    }

    fun updateAddWeight(weight: String, isRoundoff: Boolean) {
        isRoundOff = isRoundoff
        val split = weight.split(" ")
        adapter?.updateAddWeight(addWeightPosition, split[0], split[1])
        vm.lots[addWeightPosition].editedWeight = split[0]
        vm.lots[addWeightPosition].weightToDispatchUOM = split[1]
    }

    fun saveLotDetails() {
        vm.saveWeighBridgeAndLotDetails()
    }

    private fun validateZeroWeight(): Boolean {
        val selected = vm.lots.filter { (it.editedWeight ?: "0").toDouble() <= 0 }
        return selected.isEmpty()
    }

    private fun validateThirdPartyMaterialMapping(materialCode: String) {
        val material = thirdPartyMaterialList.filter {
            it.thirdPartyMaterialCode?.isNotEmpty() == true && materialCode.contains(it.thirdPartyMaterialCode!!, true)
        }
        if (material.isEmpty()) {
            isAnticipatedThirdParty = false
            enableEnterLotId(false)
            enableProceed(false)
            showThirdPartyMaterialErrorDialog(true)
        } else {
            vm.dispatchWh.thirdPartyMaterialCode = material[0].materialCode
            validateVendorCustomerCode(vm.dispatchWh.customerId ?: "")
        }
    }

    private fun validateVendorCustomerCode(soldToPartyCode: String) {
        val material = vendorList.filter { soldToPartyCode.contains(it.vendorCustomerCode ?: "dummy") }
        if (material.isEmpty()) {
            isAnticipatedThirdParty = false
            enableEnterLotId(false)
            enableProceed(false)
            showThirdPartyMaterialErrorDialog(false)
        } else {
            isAnticipatedThirdParty = true
            vendorCustomerCode = material[0].vendorCode
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun showThirdPartyMaterialErrorDialog(isMaterial: Boolean) {
        val msg = getString(
            R.string.anticipated_material_error,
            vm.dispatchWh.materialCode.plus("-").plus(vm.dispatchWh.materialName)
        )

        val vendorMsg =
            getString(
                R.string.anticipated_customer_error,
                vm.dispatchWh.customerId?.plus("-").plus(vm.dispatchWh.customerName)
            )
        MaterialDialog(requireContext()).show {
            message(
                if (isMaterial) R.string.anticipated_material_error else R.string.anticipated_customer_error,
                if (isMaterial) msg else vendorMsg
            )
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

    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        val selectedSales = salesOrderList.filter { it.salesOrderId == data } as ArrayList
        if (isWh) {
            materialNameList.clear()
            binding.tvstoValue.text = data
            vm.dispatchWh.saleOrderId = data
            if (selectedSales.size <= 1) {
                binding.tvMaterialName.isEnabled = false
                updateSODetails(selectedSales[0])
            } else {
                binding.tvMaterialName.isEnabled = true
                clearOldData()
            }
            materialNameList.addAll(selectedSales.listOfField(VegaCocoaSalesOrderModel::materialDesc).toSet())
        } else {
            val selected =
                salesOrderList.filter { it.salesOrderId == vm.dispatchWh.saleOrderId && it.materialDesc == data } as ArrayList
            vm.dispatchWh.materialCode = data
            updateSODetails(selected[0])
        }
    }

    fun updateLotList(list: ArrayList<VegaCocoaSalesLots>) {
        enableEnterLotId(isMultipleLot)
        val addedNew = ArrayList<VegaCocoaSalesLots>()
        val removedLots = ArrayList<VegaCocoaSalesLots>()
        val batchMap = vm.lots.map { it.batchNumber }
        val batchNewMap = list.map { it.batchNumber }
        list.forEach {
            if (!batchMap.contains(it.batchNumber)) {
                addedNew.add(it)
            }
        }
        vm.lots.forEach {
            if (!batchNewMap.contains(it.batchNumber)) {
                removedLots.add(it)
            }
        }
        vm.lots.removeAll(removedLots)

        for (item in removedLots) {
            vm.removeLotFromList(item.batchNumber)
        }
        vm.lots.addAll(addedNew)
        adapter?.addAllLots(vm.lots)
        enableProceed(vm.lots.isNotEmpty())
    }

}
