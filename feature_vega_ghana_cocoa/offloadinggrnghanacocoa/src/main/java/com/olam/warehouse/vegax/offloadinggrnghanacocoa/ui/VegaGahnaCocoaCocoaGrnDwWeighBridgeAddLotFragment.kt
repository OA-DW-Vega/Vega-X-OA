package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGRNDWLotManualModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaGrnPurchaseOrders
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoGrnLotWeighbridgeAddLayoutBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.ItemGhanaCocoaWeighscaleLotCardLayoutGrnBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.LOT_LIST
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.SUPPLIER
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.WAREHOUSE
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment : BaseFragment(), GhanaCocoaMTNTItemRemoveListener,
    VegaSingleSelectCommonListener {
    override val layoutResourceId = R.layout.fragment_ghana_coco_grn_lot_weighbridge_add_layout
    private lateinit var binding: FragmentGhanaCocoGrnLotWeighbridgeAddLayoutBinding

    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()

    // private var model: VegaCocoaDispatchWB? = null
    private var weightToProcess = 0.0
    private var isMultipleLot: Boolean = false

    //private var selectedPurchaseOrder: VegaCoffeePurchaseOrders? = null
    private var isEditableLot = true
    private var purchaseOrder = ArrayList<VegaGhanaGrnPurchaseOrders>()
    private var remark: String = ""
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private lateinit var callbackCocoa: VegaGhanaCocoaOffloadReplaceFragmentCallback
    private lateinit var adapterCocoa: VegaGhanaCocoaDWGRNLotAdapter
    private var moreWeightBatches = ""
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var selectedLotModel: VegaGRNDWLotManualModel? = null
    private var supplierLists = ArrayList<String>()
    private var defaultLocation = ArrayList<String>()
    private var customDialog: VegaCommonSingleSelectDialogWithSearch? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var suppliervendorList = mutableListOf<VegaVendor>()
    var SELECTED_VENDOR_CODE = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbackCocoa = context as VegaGhanaCocoaOffloadReplaceFragmentCallback
    }

    companion object {
        fun newInstance() =
            VegaGahnaCocoaCocoaGrnDwWeighBridgeAddLotFragment()
                .putArgs {
                    //putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoGrnLotWeighbridgeAddLayoutBinding.inflate(layoutInflater)
        initExtra()
        initAdapter()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        binding.apply {
            vm.apply {

            }
        }
    }


    private fun initUI() {
        binding.tvTruckValue.text = vm.dispatchWh.vehicleNumber
        binding.tvMaterialName.text = vm.dispatchWh.materialName
        binding.tvWhValue.setText(vm.dispatchWh.wayBillNo)
        binding.clLotSummary.llRecevingWH.visible()
        enableProceed()
        enableLotAddFeatures()
        binding.clLotSummary.etEnterContainer.onChange { enableAddLot(it) }
        binding.clLotSummary.clScan.setOnClickListener {
            moveToScan()
        }
        binding.clLotSummary.btnAddLot.setOnClickListener {
            selectedLotModel?.let {
                vm.saveSelectedLot(it)
            }
            if (checkStringInTextView())
                callbackCocoa.replaceFragment(
                    LOT_LIST,
                    vm.lotListSelected,
                    SELECTED_VENDOR_CODE
                )
            else
                showSelecteWareHouseToast(binding.clLotSummary.btAdd.context.getString(R.string.please_select_warehouse))
        }

        vm.getSuppliers()

        binding.clLotSummary.btAdd.setOnClickListener {
            when {
                binding.clLotSummary.etEnterContainer.text.toString().isEmpty() -> {
                    return@setOnClickListener
                }
                else -> {
                    vm.validateLot(
                        getPlantDetails().plantId,
                        binding.clLotSummary.etEnterContainer.text.toString(),
                        getCurrentKey()
                    )
                }
            }
        }

        /*  binding.clLotSummary.btAdd.setOnClickListener {
              vm.validateLot(
                  "",
                  binding.clLotSummary.etEnterContainer.text.toString(),
                  getCurrentKey()
              )
          }*/
        binding.btnProceed.setOnClickListener {
            validateProceed()
        }
        vm.loader.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    showLoading()
                }

                else -> {

                }
            }
        }
        binding.clLotSummary.tvReceivingWH.setOnClickListener {
            showSingleSelectDialog(getString(R.string.supplier), WAREHOUSE)
        }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            updateLot(it)
        })
        vm.product.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    // binding.tvMaterialName.text = it.materialName ?: ""
                    //selectedPurchaseOrder?.materialName = it.materialName ?: ""
                    //vm.dispatchWh.materialName = it.materialName ?: ""
                }
            })
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
        /* binding.tvWhValue.setOnFocusChangeListener { v, hasFocus ->
             if (!hasFocus) {
                 vm.dispatchWh.wayBillNo = binding.tvWhValue.text.toString()
                 saveWeighBridgeInfo()
                 enableProceed()
             }
         }*/
        vm.supplier.observe(viewLifecycleOwner, Observer {
            it?.let {
                supplierList = it.toMutableList()
                val suppliers =
                    it.map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
                supplierLists = suppliers as ArrayList<String>

            }
        })

    }

    private fun updateLot(response: Resource<GenericReqAndResp<VegaGRNDWLotManualModel>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when(it.data?.success)
                    {
                        true->
                        {
                            setUpLotAdapter(it.data?.data ?: VegaGRNDWLotManualModel())
                            selectedLotModel = it.data?.data ?: VegaGRNDWLotManualModel()
                            enableProceed()
                        }
                        else->{
                            binding.apply {
                              showErrorDialogWithFAQLink(
                                 binding.btnProceed.context ,
                                  it.data?.message?:""
                              )
                            }
                        }
                    }

                    hideLoading()
                }

                Resource.Status.LOADING -> {
                    showLoading()
                }

                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(
                        binding.btnProceed.context,
                        it.error?: "", ""
                    )
                    hideLoading()
                }
            }
        }
    }


    fun showSelecteWareHouseToast(msg:String) {
        binding.apply {
            Toast.makeText(
                binding.btnProceed.context,
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSingleSelectDialog(title: String, currentFalg: String) {
        /*when (currentFalg) {
            SUPPLIER -> {
                list = supplierLists
            }
            WAREHOUSE -> {
                list = defaultLocation
            }
        }*/
        customDialog =
            VegaCommonSingleSelectDialogWithSearch(
                title,
                currentFalg,
                supplierLists,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    fun checkStringInTextView(): Boolean {
        return binding.clLotSummary.tvReceivingWH.text.isNotEmpty()
    }

    private fun setUpLotAdapter(vegaGrnResponse: VegaGRNDWLotManualModel) {
        binding.apply {
            binding.clLotSummary.apply {
                val list = ArrayList<VegaGRNDWLotManualModel>()
                list.add(vegaGrnResponse)
                rvList.setUpAdapter(list,
                    R.layout.item_ghana_cocoa_weighscale_lot_card_layout_grn,
                    ItemGhanaCocoaWeighscaleLotCardLayoutGrnBinding::inflate,
                    { item, _, bindingItem ->
                        bindingItem.apply {
                            tvLotLbl.visible()
                            tvWeightDispatch.visible()
                            tvStLocation.visible()
                            tvGrade.visible()
                            tvWeightDispatch.text = tvWeight.context.getString(R.string.noofbags)
                            tvStLocation.text = tvWeight.context.getString(R.string.vendor_name)

                            //values tags
                            tvScaleLotValue.visible()
                            tvScaleDispatchValue.visible()
                            tvStLocationValue.visible()
                            tvScaleGradeValue.visible()
                            tvScaleLotValue.text = item.lotId
                            tvScaleDispatchValue.text = item.noOfBags
                            tvStLocationValue.text = item.vendorName
                            tvScaleGradeValue.text = item.productName

                            ivScaleClose.setOnClickListener {
                                list.clear()
                                rvList.adapter?.notifyItemRemoved(0)
                            }
                        }
                    }
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }


    private fun fetchLotDetails(lotId: String, whId: String) {
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.validateLot(
            "",
            lotId,
            getCurrentKey()
        )
    }


    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.green
                )
            )

            else -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    fun updateLotList(list: ArrayList<VegaGRNDWLotManualModel>) {
        setUpLotAdapter(list[0])
        selectedLotModel = list[0]
        enableProceed()
    }

    private fun enableProceed() {
        val enabled = selectedLotModel != null
        if (enabled)
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enabled
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

                ConfigItems.LOT_WEIGHT_EDITABLE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> isEditableLot = true
                        it.applicable?.contains("N")!! -> {
                            isEditableLot = false
                        }
                    }
                }
            }
        }
        adapterCocoa.isEdit = isEditableLot
    }


    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaGhanaCocoaDispatchLots>>>) {
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
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun chooseOneLotDialog(lots: List<VegaGhanaCocoaDispatchLots>) {
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

    private fun updateAdapter(vegaCocoaDispatchLots: VegaGhanaCocoaDispatchLots) {
        adapterCocoa.addLotData(vegaCocoaDispatchLots)
        binding.clLotSummary.etEnterContainer.setText("")
        vegaCocoaDispatchLots.weighBridgeId = vm.dispatchWh.weighBridgeId
        vm.lotList.add(vegaCocoaDispatchLots)
        vm.addLoTInDB(vegaCocoaDispatchLots)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR && resultCode == Activity.RESULT_OK) {
            data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                fetchLotDetails(it, vm.dispatchWh.plantId ?: "")
            }
        }
    }

    private fun initAdapter() {
        adapterCocoa =
            VegaGhanaCocoaDWGRNLotAdapter(
                ArrayList(),
                uomDetails,
                true,
                true, this
            )
        val manager = LinearLayoutManager(activity)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.clLotSummary.rvList.layoutManager = manager
        binding.clLotSummary.rvList.adapter = adapterCocoa
    }

    private fun saveWeighBridgeInfo() {
        vm.saveWeighBridgeDetails()
    }


    private fun enableLotAddFeatures() {
        val enableAll = vm.lotList.isEmpty()
        binding.clLotSummary.clScan.isClickable = enableAll
        binding.clLotSummary.clScan.isEnabled = enableAll
        binding.clLotSummary.etEnterContainer.isEnabled = enableAll
    }

    private fun validateProceed() {
        if (selectedLotModel != null) {
            callbackCocoa.replaceFragment(SUPPLIER, selectedLotModel!!, selectedLotModel!!)
        } else {
            Toast.makeText(
                activity,
                getString(R.string.please_add_lot),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun itemRemoved(item: VegaGhanaCocoaDispatchLots) {
        vm.lotList.remove(item)
        vm.removeLotFromList(item.batchNumber)
        enableLotAddFeatures()
    }


    private fun dismissKeyboard(activity: Activity) {
        val inputManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        binding.apply {
            customDialog?.dismiss()
            when (currentFlag) {
                WAREHOUSE -> {
                    binding.clLotSummary.tvReceivingWH.text = data
                    for (items in supplierList.iterator()) {
                        if (items.vendorCode.plus(" - ").plus(items.vendorName).equals(data, true)) {
                            SELECTED_VENDOR_CODE = items.vendorCode
                            break
                        }
                    }
                }
            }
        }
    }
}
