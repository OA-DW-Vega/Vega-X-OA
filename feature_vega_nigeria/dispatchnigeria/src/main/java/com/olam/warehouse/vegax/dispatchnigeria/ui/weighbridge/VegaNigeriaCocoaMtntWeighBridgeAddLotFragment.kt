package com.olam.warehouse.vegax.dispatchnigeria.ui.weighbridge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaDispatchDelivery
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaCocoaMtntLotListModel
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.VegaNigeriaSesameMtntPurchaseOrders
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentNigeriaCocoaMtntWeighbridgeAddLotLayoutBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.ItemRemoveListener
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntLotAdapter
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.dispatchnigeria.utils.LOT_LIST
import com.olam.warehouse.vegax.dispatchnigeria.utils.MODEL_BUNDLE
import com.olam.warehouse.vegax.dispatchnigeria.utils.WEIGHBRIDGE_SUMMARY
import com.olam.warehouse.vegax.dispatchnigeria.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class VegaNigeriaCocoaMtntWeighBridgeAddLotFragment : BaseFragment(), ItemRemoveListener {
    override val layoutResourceId = R.layout.fragment_nigeria_cocoa_mtnt_weighbridge_add_lot_layout
    private lateinit var binding: FragmentNigeriaCocoaMtntWeighbridgeAddLotLayoutBinding

    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private var model: VegaCocoaDispatchWB? = null
    private var weightToProcess = 0.0
    private var isMultipleLot: Boolean = false
    private var materialCode: String = ""
    private var storageLocation = ArrayList<String>()

    //private var selectedPurchaseOrder: VegaCoffeePurchaseOrders? = null
    private var isEditableLot = true
    private var purchaseOrder = ArrayList<VegaNigeriaSesameMtntPurchaseOrders>()
    private var remark: String = ""
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var dispatchWbFromTruckList: VegaCocoaDispatchWB? = null
    private lateinit var callback: VegaNigeriaCocoaReplaceFragmentCallback
    private lateinit var adapter: VegaNigeriaCocoaMtntLotAdapter
    private var moreWeightBatches = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as VegaNigeriaCocoaReplaceFragmentCallback
    }

    companion object {
        fun newInstance(model: VegaCocoaDispatchWB) =
            VegaNigeriaCocoaMtntWeighBridgeAddLotFragment()
                .putArgs {
                    putParcelable(MODEL_BUNDLE, model)
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaCocoaMtntWeighbridgeAddLotLayoutBinding.inflate(layoutInflater)
        initExtra()
        initAdapter()
        initUI()
        return binding.root
    }

    private fun initExtra() {
        model = arguments?.getParcelable(MODEL_BUNDLE)
        vm.dispatchWh = model ?: VegaCocoaDispatchWB()
        vm.getConfigItems(UserRoles.MTNT.role)
        dispatchWbFromTruckList = arguments?.getParcelable<VegaCocoaDispatchWB>(MODEL_BUNDLE) as VegaCocoaDispatchWB
        vm.weighBridgeWithLots.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                vm.dispatchWh = it.dispatch
                vm.lotList.clear()
                vm.lotList.addAll(it.lineItems)
                updateUIWithLocalData()
            }
        })
        vm.getMtntWithLots(model?.weighBridgeId ?: "")
    }

    private fun updateUIWithLocalData() {
        binding.tvWhValue.setText(vm.dispatchWh.wayBillNo)
        binding.tvStoWeightValue.text =
            vm.dispatchWh.deliveryQty.plus(" ").plus(vm.dispatchWh.deliveryUOM)
        binding.tvstoValue.text = vm.dispatchWh.delivery
        binding.tvMaterialName.text = vm.dispatchWh.materialName
        startTime = if (vm.dispatchWh.startTime.isNotEmpty()) vm.dispatchWh.startTime.toLong() else 0
        enableLotAddFeatures()
        refreshAdapter(vm.lotList)
        enableProceed()
    }

    private fun initUI() {
        binding.tvTruckValue.text = vm.dispatchWh.vehicleNumber
        binding.tvMaterialName.text = vm.dispatchWh.materialName
        binding.tvWhValue.setText(vm.dispatchWh.wayBillNo)
        binding.tvWhValue.onChange { enableProceed() }
        enableProceed()
        enableLotAddFeatures()
        binding.clLotSummary.etEnterContainer.onChange { enableAddLot(it) }
        binding.clLotSummary.clScan.setOnClickListener { moveToScan() }
        binding.clLotSummary.btnAddLot.setOnClickListener {
            callback.replaceFragment(
                LOT_LIST,
                VegaNigeriaCocoaMtntLotListModel(
                    selectedList = vm.lotList,
                    isMultipleAdd = false,
                    material = getMaterialList()
                )
            )
        }
        binding.clLotSummary.btAdd.setOnClickListener { vm.validateLot(binding.clLotSummary.etEnterContainer.text.toString()) }
        binding.btnProceed.setOnClickListener {
            validateProceed()
        }


        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog()
                binding.clLotSummary.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    binding.clLotSummary.etEnterContainer.text.toString(),
                    vm.dispatchWh.plantId ?: ""
                )
        })


        vm.product.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    binding.tvMaterialName.text = it.materialName ?: ""
                    //selectedPurchaseOrder?.materialName = it.materialName ?: ""
                    vm.dispatchWh.materialName = it.materialName ?: ""
                }
            })
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.clLotSummary.btAdd.setOnClickListener {
            vm.validateLot(binding.clLotSummary.etEnterContainer.text.toString())
        }

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })

        vm.delivery.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getDelivery(vm.dispatchWh.delivery.toString(), vm.dispatchWh.deliveryItem)

        binding.tvWhValue.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                vm.dispatchWh.wayBillNo = binding.tvWhValue.text.toString()
                saveWeighBridgeInfo()
                enableProceed()
            }
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaDispatchDelivery>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data.let { it1 ->
                    vm.dispatchWh.deliveryQty = it1?.deliveryQty
                    vm.dispatchWh.deliveryUOM = it1?.deliveryUOM
                    vm.dispatchWh.stockUOM = it1?.stockUOM
                    vm.dispatchWh.stockQty = it1?.stockQty
                    vm.dispatchWh.denominator = it1?.denominator
                    vm.dispatchWh.numerator = it1?.numerator
                    binding.tvStoWeightValue.text = it1?.deliveryQty.plus(" ").plus(it1?.deliveryUOM)
                    binding.tvstoValue.text = vm.dispatchWh.delivery
                }
                saveWeighBridgeInfo()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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


    private fun fetchLotDetails(lotId: String, whId: String) {
        binding.clLotSummary.etEnterContainer.hideKeyboard()
        binding.clLotSummary.etEnterContainer.setText("")
        showLoading()
        if (AppUtils.isOnline()) vm.getLotDetails(lotId, getMaterialList(), whId)
    }


    private fun enableAddLot(value: String) {
        when {
            value.isNotEmpty() -> ViewCompat.setBackgroundTintList(
                binding.clLotSummary.btAdd,
                ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.green)
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

    fun updateLotList(list: ArrayList<VegaCocoaDispatchLots>) {
        if (vm.dispatchWh.startTime.isEmpty()) {
            startTime = System.currentTimeMillis()
            vm.dispatchWh.startTime = startTime.toString()
            updateStartTime()
        }
        vm.lotList.clear()
        vm.addLoTInDB(list)
        vm.lotList.addAll(list)
        adapter.removeData()
        adapter.addAllLots(vm.lotList)
        enableProceed()
        calculateWtp()
        enableLotAddFeatures()
    }

    private fun enableProceed() {
        val enabled = vm.lotList.isNotEmpty() && binding.tvWhValue.text.toString().isNotEmpty()
        if (enabled)
            binding.btnProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        else {
            binding.btnProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
        binding.btnProceed.isEnabled = enabled
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lotList.filter { !it.isLowerWeight }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight?.length == 0 }
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
        adapter.isEdit = isEditableLot
    }

    private fun calculateWtp() {
        weightToProcess = 0.0
        vm.lotList.forEach {
            weightToProcess += if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble() ?: 0.0
        }
        weightToProcess.toString().toDouble().formatThreeDigits().plus(" ")
            .plus(if (vm.lotList.size > 0) vm.lotList[0].unitOfMeasure else "MT")
    }


    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCocoaDispatchLots>>>) {
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
                updateAdapter(lots[index])
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(vegaCocoaDispatchLots: VegaCocoaDispatchLots) {
        adapter.addLotData(vegaCocoaDispatchLots)
        binding.clLotSummary.etEnterContainer.setText("")
        vegaCocoaDispatchLots.weighBridgeId = vm.dispatchWh.weighBridgeId
        vm.lotList.add(vegaCocoaDispatchLots)
        vm.addLoTInDB(vegaCocoaDispatchLots)
    }

    private fun refreshAdapter(list: List<VegaCocoaDispatchLots>) {
        adapter.addAllLots(list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    fetchLotDetails(it, vm.dispatchWh.plantId ?: "")
                }
            }
        }
    }

    private fun initAdapter() {
        adapter =
            VegaNigeriaCocoaMtntLotAdapter(
                ArrayList(),
                true,
                true, this
            )
        val manager = LinearLayoutManager(activity)
        manager.orientation = LinearLayoutManager.VERTICAL
        binding.clLotSummary.rvList.layoutManager = manager
        binding.clLotSummary.rvList.adapter = adapter
    }

    private fun saveWeighBridgeInfo() {
        vm.saveWeighBridgeDetails()
    }


    private fun updateStartTime() {
        vm.updateStartLoading()
    }

    private fun saveLotList(list: ArrayList<VegaCocoaDispatchLots>) {
        vm.addLoTInDB(list)
    }

    fun saveLotDetails() {
        if (vm.dispatchWh.purchaseDocNum != "")
            vm.saveWeighBridgeAndLotDetails()
    }

    private fun enableLotAddFeatures() {
        val enableAll = vm.lotList.isEmpty()
        binding.clLotSummary.clScan.isClickable = enableAll
        binding.clLotSummary.clScan.isEnabled = enableAll
        binding.clLotSummary.etEnterContainer.isEnabled = enableAll
    }

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    showRemarkDialog()
                } else Toast.makeText(
                    activity,
                    getString(R.string.lot_more_weight).plus(" - ").plus(moreWeightBatches),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                activity,
                getString(R.string.less_weight_error),
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

    private fun getMaterialList(): ArrayList<String> {
        val material = ArrayList<String>()
        material.add(model?.materialCode ?: "")
        return material
    }

    override fun itemRemoved(item: VegaCocoaDispatchLots) {
        vm.lotList.remove(item)
        vm.removeLotFromList(item.batchNumber)
        enableLotAddFeatures()
    }

    private fun showRemarkDialog() {

        showDialog(getString(R.string.end_load_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    dismissKeyboard(activity!!)
                    endTime = System.currentTimeMillis()
                    vm.dispatchWh.endTime = endTime.toString()
                    val duration = endTime?.minus(startTime ?: 0)
                    vm.dispatchWh.remarks = remark
                    vm.dispatchWh.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
                    vm.saveWeighBridgeAndLotDetails()
                    callback.replaceFragment(WEIGHBRIDGE_SUMMARY, vm.dispatchWh)
                }
            }

        }, true, vm.dispatchWh.remarks)
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
}

