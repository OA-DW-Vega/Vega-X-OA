package com.olam.warehouse.vegax.localsalesnigeria.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesLotWithbags
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.localsalesnigeria.R
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.VegaNigeriaSalesOrderModel
import com.olam.warehouse.vegax.localsalesnigeria.data.domain.model.materialList
import com.olam.warehouse.vegax.localsalesnigeria.databinding.FragmentNigeriaSalesAddLotBinding
import com.olam.warehouse.vegax.localsalesnigeria.databinding.ItemNigeriaSalesLotBinding
import com.olam.warehouse.vegax.localsalesnigeria.databinding.ItemNigeriaSalesMaterialListBinding
import com.olam.warehouse.vegax.localsalesnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaNigeriaSalesAddLotFragment : BaseFragment(), NigeriaRecyclerViewItemClickListener {

    private var callBack: CallBack? = null
    private val vm: VegaNigeriaSalesViewModel by viewModel()
    private var dispatchType = ""
    private var salesOrderList = mutableListOf<VegaNigeriaSalesOrderModel>()
    private var materialCode: String = ""
    private var materialList = ArrayList<String>()
    private var materialListItems = ArrayList<materialList>()
    private var soNumbers = ArrayList<String>()
    private var isEditableLot = true
    private var isMultipleLot = false
    private var isAlreadyLoading = false
    private var isReceived = false
    private var addWeightPosition = 0
    private var customDialog: CustomNigeriaSingleSelectDialog? = null
    private var materialNameList = ArrayList<String>()
    private var editLotId: String = ""
    private var editLotMaterial: String = ""
    var isMissedPallet = false
    private var isCompliantMaterial: Boolean = false
    private var productList = emptyList<VegaMaterial>()

    override val layoutResourceId: Int = R.layout.fragment_nigeria_sales_add_lot
    private lateinit var binding: FragmentNigeriaSalesAddLotBinding

    companion object {
        fun newInstance(dispatch: VegaCoffeeSalesOrder, dispatchType: String) =
            VegaNigeriaSalesAddLotFragment().putArgs {
                putParcelable(SALES_ITEM, dispatch)
                putString(SALES_TYPE, dispatchType)
            }
    }

    interface CallBack {
        fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, salesType: String)
        fun replaceFragment(fragment: String, lot: VegaCoffeeSalesLots)
        fun replaceFragment(fragment: String, model: VegaCoffeeSalesOrder, materialList: ArrayList<String>)
        fun replaceFragmentSummary(
            fragment: String,
            model: VegaCoffeeSalesOrder,
            materialItems: ArrayList<materialList>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentNigeriaSalesAddLotBinding.inflate(inflater)
        initExtra()
        initUI()
        /*listener = this*/
        return binding.root
    }


    private fun initExtra() {
        //enableEnterLotId(false)
        vm.salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALES_ITEM) as VegaCoffeeSalesOrder
        dispatchType = arguments?.getString(SALES_TYPE) ?: ""
        vm.salesOrder.salesType = dispatchType
        vm.currentTempId = vm.salesOrder.salesTempId
        binding.tvstoValue.isEnabled = vm.currentTempId.isEmpty()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("localsalesNigeria/ui/VegaNigeriaSalesAddLotFragment")
            .title("Vega_Nigeria/Local Sales").with(tracker)
    }

    private fun initUI() {
        showTTDialog()
        disableAddLotsButtons()
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        //vm.getPurchaseOrder()
        vm.dispatchSalesItem.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateSalesItem(it) else updateSalesItem(VegaCoffeeSalesOrderWithLots()) })

        var titleString = ""
        when (dispatchType) {
            SALES_TYPE_WEIGHSCALE -> titleString = getString(R.string.weighscale)
            SALES_TYPE_ANTICIPATED -> titleString = getString(R.string.anticipated)
            SALES_TYPE_WEIGHBRIDGE -> titleString = getString(R.string.weighbridge_weighscale)
        }
        binding.tvTitle.text = getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(titleString)
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so)) }
        binding.clInventory.setOnClickListener {
            callBack?.replaceFragment(
                INVENTORY_FRAG,
                vm.salesOrder,
                materialList
            )
        }
        vm.allProduct.observe(
            viewLifecycleOwner,
            Observer {
                if (it != null) {
                    productList = it.filter { if(isCompliantMaterial) it.complainceFlag.equals(Constants.COMPLAINT) else it.complainceFlag.equals(Constants.NON_COMPLAINT) }
                }
            })
        binding.btProceed.setOnClickListener {
            if (vm.lots.isNotEmpty())
                if (validateLotWeightWithMaterialWeight()) {
                    if (validateLotWeight()) {
                        if (validateZeroWeight()) {
                            if (editLotId.isEmpty() && dispatchType.equals(SALES_TYPE_WEIGHSCALE)) {
                                if (!isMissedPallet) {
                                    showRemarkDialog()
                                } else {
                                    activity?.toast(getString(R.string.weighment_mismatch))
                                }
                            } else {
                                if (dispatchType.equals(SALES_TYPE_WEIGHSCALE) && !isMissedPallet) {
                                    moveToSummary("")
                                } else if (dispatchType.equals(SALES_TYPE_ANTICIPATED)) {
                                    moveToSummary("")
                                } else {
                                    activity?.toast(getString(R.string.weighment_mismatch))
                                }
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
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.less_weight_for_so_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            else Toast.makeText(
                activity,
                getString(R.string.prceed_error),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.btAdd.setOnClickListener {
            vm.currentScanLot = binding.etEnterContainer.text.toString()
            vm.validateLot(binding.etEnterContainer.text.toString())
            isReceived = false
        }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (!isAlreadyLoading) {
                isAlreadyLoading = true
                if (it != null) {
                    showLotAlreadyExistDialog(true)
                    binding.etEnterContainer.setText("")
                    isAlreadyLoading = false
                } else
                    fetchLotDetails(
                        vm.currentScanLot,
                        materialList, vm.salesOrder.plantId ?: ""
                    )
            }
        })


        /*vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
            initAdapter()
        })*/
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so)) }
        when {
            dispatchType.equals(SALES_TYPE_WEIGHSCALE, true) -> {
                binding.btSave.visibility = View.VISIBLE
                binding.ivProceed.visibility = View.VISIBLE
            }
            else -> {
                binding.btSave.visibility = View.GONE
                binding.ivProceed.visibility = View.GONE
            }
        }
        binding.btSave.setOnClickListener {
            showConfirmSaveDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    /*private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            if (dispatchType.equals(SALES_TYPE_ANTICIPATED))
                title(R.string.enter_remark)
            else
                title(R.string.stop_loding_confirmation)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.custom_edit_text)
            *//*input(waitForPositiveButton = false, hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)) { dialog, text ->
                val inputField = dialog.getInputField()
                val isValid = text.isNotEmpty()
                remark = text.toString()
                inputField.error = if (isValid) null else getString(com.olam.warehouse.presentation.R.string.enter_remark)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }*//*
            val editValue = this.getCustomView().etValue
            if (!vm.salesOrder.remarks.isNullOrEmpty()) editValue.setText(vm.salesOrder.remarks)
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.confirm),
                    true
                )
            ) {
                if (!editValue.text.isNullOrEmpty()) {
                    moveToSummary(editValue.text.toString())
                } else {
                    editValue.error = getString(com.olam.warehouse.presentation.R.string.enter_remark)
                }
            }
            negativeButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.cancel),
                    false
                )
            ) {
                dismiss()
            }
        }
    }*/

    private fun showRemarkDialog() {

        showDialog(getString(R.string.stop_loding_confirmation), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    vm.salesOrder.remarks = remark
                    moveToSummary(remark)
                }
            }

        }, true, vm.salesOrder.remarks.toString())
    }

    private fun moveToSummary(remark: String) {
        vm.salesOrder.salesType = dispatchType
        if (remark.isNotEmpty()) vm.salesOrder.remarks = remark
        vm.saveWeighBridgeAndLotDetails()
        vm.salesOrder.lotList = vm.lots
        getCurrentDate("END")
        var duration = 0L
        if ((vm.salesOrder.endTime).isNullOrEmpty() || (vm.salesOrder.startTime).isNullOrEmpty()) {
            duration = 0L
        } else {
            duration = ((vm.salesOrder.endTime?.toLong() ?: 0).minus(
                vm.salesOrder.startTime?.toLong() ?: 0
            ))
        }
        vm.salesOrder.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration).toString()
        callBack?.replaceFragmentSummary(SUMMARY, vm.salesOrder, materialListItems)
    }

    private fun showConfirmSaveDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (vm.salesOrder.salesTempId.isBlank()) {
                        val randomDouble = "TMP".plus(Random.nextLong().toString())
                        vm.salesOrder.salesTempId = randomDouble
                    }
                    vm.saveWeighBridgeAndLotDetails()
                    Toast.makeText(activity, getString(R.string.local_save_msg), Toast.LENGTH_SHORT).show()
                    activity?.finish()
                },
                { dismiss() })
        }
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaNigeriaSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    salesOrderList = it.data?.data?.filter { productList.map { it.materialCode }.contains(if(it.salesOrderList.isNotEmpty())it.salesOrderList.get(0).materialNumber?.takeLast(12) else "" )  } as MutableList<VegaNigeriaSalesOrderModel>
                    soNumbers.clear()
                    soNumbers.addAll(salesOrderList.map { it.salesOrderId })
                    if (vm.salesOrder.salesTempId.isNotEmpty()) clickOnItem(vm.salesOrder.saleOrderId, true)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun disableAddLotsButtons() {
        binding.clScan.isEnabled = false
        binding.clInventory.isEnabled = false
        binding.etEnterContainer.isEnabled = false
        ViewCompat.setBackgroundTintList(
            binding.clScan,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.grey
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.grey
            )
        )
    }

    private fun enableAddLotButtons() {
        binding.clScan.isEnabled = true
        binding.clInventory.isEnabled = true
        binding.etEnterContainer.isEnabled = true
        ViewCompat.setBackgroundTintList(
            binding.clScan,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.dark_green_1
            )
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(
                requireActivity(),
                com.olam.warehouse.presentation.R.color.dark_green_1
            )
        )
    }


    private fun updateSalesItem(it: VegaCoffeeSalesOrderWithLots) {
        if (it.lineItems.size > 0) {
            var data = mutableListOf<VegaCoffeeSalesLotWithbags>()
            if (vm.salesOrder.salesTempId.isNotEmpty()) {
                data =
                    it.lineItems.filter { it1 -> it1.salesLot.salesTempId.equals(vm.salesOrder.salesTempId) } as MutableList<VegaCoffeeSalesLotWithbags>
            } else {
                if (it.salesOrder.salesType.equals(SALES_TYPE_ANTICIPATED)) {
                    vm.salesOrder = it.salesOrder
                    data =
                        it.lineItems.filter { it2 -> it2.salesLot.salesType.equals(SALES_TYPE_ANTICIPATED) } as MutableList<VegaCoffeeSalesLotWithbags>
                } else if (it.salesOrder.salesType.equals(SALES_TYPE_WEIGHSCALE)) {
                    val data1 = it.lineItems.filter { it2 -> it2.salesLot.salesType.equals(SALES_TYPE_WEIGHSCALE) }
                        .filter { it.salesLot.saleOrderId.equals(binding.tvstoValue.text.toString()) }
                    val tempIds = data1.map { it.salesLot.salesTempId }

                    if (tempIds.size == 1) {
                        vm.salesOrder.salesTempId = tempIds[0]
                        data = data1 as MutableList<VegaCoffeeSalesLotWithbags>
                    } else {
                        if (vm.currentTempId.isNotEmpty()) {
                            vm.salesOrder.salesTempId = vm.currentTempId
                            data =
                                data1.filter { it.salesLot.salesTempId.equals(vm.currentTempId) } as MutableList<VegaCoffeeSalesLotWithbags>
                        } else data = mutableListOf()
                    }
                } else
                    data = mutableListOf()
            }
            setUpAdapter(data)
        } else setUpAdapter(mutableListOf())
    }


    private fun getCurrentDate(type: String) {
        if (SALES_TYPE_WEIGHBRIDGE != dispatchType) {
            val stamp = Timestamp(System.currentTimeMillis())
            val date = Date(stamp.time)
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val dat = dateFormat.format(date)
            when (type) {
                "START" -> vm.salesOrder.startTime = getCurrentTimeInMills().toString()
                "END" -> vm.salesOrder.endTime = getCurrentTimeInMills().toString()
            }
        }
    }

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (!isReceived) {
                        isReceived = true
                        isAlreadyLoading = false
                        response.data?.data?.let { it1 ->
                            when (it1.size == 1) {
                                true -> updateAdapter(it1 as ArrayList<VegaCoffeeSalesLots>)
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
                        showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    }
                }
            }
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String) {
        customDialog =
            CustomNigeriaSingleSelectDialog(title, isWh, soNumbers, requireActivity(), this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean) {
        vm.salesOrder.salesTempId = ""
        enableProceed(false)
        customDialog?.dismiss()
        val selectedSales = salesOrderList.filter { it.salesOrderId == data } as ArrayList
        binding.tvstoValue.text = data
        vm.salesOrder.saleOrderId = data
        vm.getDispatchSalesItem(data, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
        updateSODetails(selectedSales[0])
    }

    private fun updateSODetails(vegaCoffeeSalesOrderModel: VegaNigeriaSalesOrderModel) {
        val selectedSales = vegaCoffeeSalesOrderModel.salesOrderList
        materialListItems.clear()
        materialListItems.addAll(selectedSales)
        if (selectedSales.size > 0) {
            binding.tvCustomerValue.text = selectedSales[0].soldToPartyName
            vm.salesOrder.customerId = selectedSales[0].soldToPartyCode
            vm.salesOrder.customerName = selectedSales[0].soldToPartyName
            vm.salesOrder.createdDate = selectedSales[0].createdDate
            vm.salesOrder.salesItem = selectedSales[0].salesItemNum
            enableSave(true)
            setUpMaterialAdapter(selectedSales)
            enableAddLotButtons()
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<materialList>) {
        materialList.clear()
        materialList.addAll(selectedSales.map { it.materialNumber.toString() })
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<materialList>,
            R.layout.item_nigeria_sales_material_list,
            ItemNigeriaSalesMaterialListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialNew.text = it.materialDesc
                bindItem.tvSoWeight.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ").plus(it.meins)
                when (it.meins) {
                    "KG" -> {
                        bindItem.tvSoWeight.text =
                            it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(it.meins)
                    }
                    "MT" -> {
                        bindItem.tvSoWeight.text = (it.openQuantity.toString()).toDouble()
                            .formatThreeDigits()
                            .plus(" ").plus("MT")
                    }
                }
            })
    }

    private fun chooseOneLotDialog(lots: List<VegaCoffeeSalesLots>) {
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
                updateAdapter(arrayListOf(lots[index]))
            }
            positiveButton(text = UIUtils.getSpannedText(getString(com.olam.warehouse.login.R.string.ok), true))
        }
    }

    private fun updateAdapter(vegaCoffeeSalesLots: ArrayList<VegaCoffeeSalesLots>) {
        if (vm.salesOrder.salesTempId.isBlank()) {
            val randomDouble = "TMP".plus(Random.nextLong().toString())
            vm.salesOrder.salesTempId = randomDouble
        }
        vegaCoffeeSalesLots.forEachIndexed { index, it ->
            it.slPostion = if (it.slPostion == 0) index else it.slPostion
            it.saleOrderId = vm.salesOrder.saleOrderId
            it.salesTempId = vm.salesOrder.salesTempId
            it.salesType = vm.salesOrder.salesType
            if (!vm.lots.any { item -> item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode) }) vm.lots.add(
                it
            )
        }

        if (binding.rvLots.adapter?.itemCount == 0) getCurrentDate("START")
        vm.saveWeighBridgeAndLotDetails()
        binding.etEnterContainer.setText("")
        enableProceed(true)
        //enableEnterLotId(isMultipleLot)
        //setUpAdapter(vm.lots)
    }

    private fun setUpAdapter(lineItems: MutableList<VegaCoffeeSalesLotWithbags>) {
        vm.lots = lineItems.map { it.salesLot } as java.util.ArrayList<VegaCoffeeSalesLots>
        if (vm.lots.size > 0) enableProceed(true)
        var lineItems1 = mutableListOf<VegaCoffeeSalesLotWithbags>()
        if (editLotId.isNotEmpty()) {
            binding.tvstoValue.isEnabled = false
            disableAddLotsButtons()
            lineItems1 =
                lineItems.filter {
                    it.salesLot.batchNumber.equals(editLotId) && it.salesLot.materialCode.equals(
                        editLotMaterial
                    )
                } as MutableList<VegaCoffeeSalesLotWithbags>
        } else {
            if (vm.currentTempId.isEmpty()) binding.tvstoValue.isEnabled = true
            enableAddLotButtons()
            lineItems1 = lineItems
        }
        val bagItems = lineItems1.map { it.lineItems }
        isMissedPallet = false
        bagItems.forEachIndexed { index, it ->
            val weighment = it.size
            val palletCount = if (weighment > 0) it[0].noOfPallet?.toInt() else 0
            if (palletCount != 0 && weighment != palletCount) isMissedPallet = true
        }
        binding.rvLots.setUpAdapter(
            lineItems1.sortedByDescending { it.salesLot.slPostion }.toMutableList(),
            R.layout.item_nigeria_sales_lot,
            ItemNigeriaSalesLotBinding::inflate,
            { it, pos, bindItem ->
                val lot = it.salesLot

                when (lot.salesType) {
                    SALES_TYPE_WEIGHSCALE -> {
                        val bagList = it.lineItems.filter {
                            it.batchNumber.equals(lot.batchNumber) && it.materialCode.equals(lot.materialCode) && it.salesTempId.equals(
                                lot.salesTempId
                            )
                        }
                        if (bagList.size > 0) {
                            var editedWt = ""
                            if (lot.unitOfMeasure.equals("MT"))
                                editedWt = bagList.sumByDouble { it.netWeight.toDouble() }.div(1000)
                                    .formatThreeDigits()
                            else
                                editedWt = bagList.sumByDouble { it.netWeight.toDouble() }
                                    .formatThreeDigits()
                            lot.editedWeight = editedWt
                        } else lot.editedWeight = "0"
                        bindItem.flSalesLot.visible()
                        bindItem.flAnticipatedSales.gone()
                        bindItem.tvScaleLotValue.text = lot.batchNumber
                        bindItem.tvScaleGradeValue.text = lot.materialName
                        bindItem.tvStLocationValue.text = lot.storageLocationCode
                        // Display the Values in KG only
                        when (lot.unitOfMeasure) {
                            "KG" -> {
                                bindItem.tvScaleDispatchValue.text =
                                    lot.editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                                        .plus(lot.unitOfMeasure)
                                bindItem.tvScaleWeightValue.text =
                                    lot.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                        .plus(lot.unitOfMeasure)
                            }
                            "MT" -> {
                                bindItem.tvScaleDispatchValue.text =
                                    bagList.sumByDouble { it.netWeight.toDouble() }
                                        .formatThreeDigits()
                                        .plus(" ").plus("MT")
                                bindItem.tvScaleWeightValue.text =
                                    (lot.weight.toString()).toDouble().formatThreeDigits()
                                        .plus(" ").plus("MT")
                            }
                        }

//                    tvScaleDispatchValue.text =
//                        lot.editedWeight?.toDouble()?.formatThreeDigits().plus(" ").plus(lot.unitOfMeasure)
//                    tvScaleWeightValue.text =
//                        lot.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(lot.unitOfMeasure)
                        bindItem.tvAddWeight.setOnClickListener { view ->
                            callBack?.replaceFragment(ADD_WEIGHT, lot)
                            //binding.rvLots.adapter?.notifyItemChanged(pos)
                            //updateWeight()
                        }
                        if (editLotId.isNotEmpty()) bindItem.ivScaleClose.gone() else bindItem.ivScaleClose.visible()
                        bindItem.ivScaleClose.setOnClickListener { view ->
                            showConformationDialog(
                                lot,
                                view
                            )
                        }
                        bindItem.cbScaleStorageLoss.isChecked = lot.endLotFlag ?: false
                        bindItem.clEndLot.setOnClickListener { view ->
                            lot.endLotFlag = !lot.endLotFlag!!
                            bindItem.cbScaleStorageLoss.isChecked = lot.endLotFlag ?: false
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                            vm.saveWeighBridgeAndLotDetails()
                        }
                        vm.lots.remove(lot)
                        vm.lots.add(lot)
                    }
                    SALES_TYPE_ANTICIPATED -> {
                        bindItem.flSalesLot.gone()
                        bindItem.flAnticipatedSales.visible()
                        bindItem.tvLotId.text = lot.batchNumber
                        bindItem.tvStLocationValue1.text = lot.storageLocationCode
                        bindItem.tvGradeValue.text = lot.materialName
                        bindItem.tvWeightValue.text =
                            lot.weight?.toDouble()?.formatThreeDigits().plus(" ")
                                .plus(lot.unitOfMeasure)
                        bindItem.cbSelectAll.isChecked = lot.isChecked ?: false
                        bindItem.etWeight.setText(lot.editedWeight)
                        bindItem.llSelectAll.setOnClickListener { view ->
                            lot.isChecked = !lot.isChecked!!
                            if (lot.isChecked!!) {
                                bindItem.cbSelectAll.isChecked = true
                                lot.editedWeight = lot.weight?.toDouble()?.formatThreeDigits()

                            } else {
                                bindItem.cbSelectAll.isChecked = false
                                lot.editedWeight = "0"
                            }
                            bindItem.etWeight.setText(lot.editedWeight)
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                        }
                        bindItem.cbStorageLoss.isChecked = lot.endLotFlag ?: false
                        bindItem.llEndLot.setOnClickListener { view ->
                            lot.endLotFlag = !lot.endLotFlag!!
                            bindItem.cbStorageLoss.isChecked = lot.endLotFlag ?: false
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                            vm.saveWeighBridgeAndLotDetails()
                        }

                        /* cbSelectAll.setOnCheckedChangeListener { it, isChecked ->
                         // etWeight.isEnabled = !isChecked
                         lot.isChecked = !isChecked
                         if (isChecked) {
                             lot.editedWeight = lot.weight?.toDouble()?.formatThreeDigits()
                         }
                         else {
                             lot.editedWeight = "0"
                         }
                         etWeight.setText(lot.editedWeight)
                         vm.lots.remove(lot)
                         vm.lots.add(lot)
                     }*/
                        if (editLotId.isNotEmpty()) bindItem.ivClose.gone() else bindItem.ivClose.visible()
                        bindItem.ivClose.setOnClickListener { view ->
                            showConformationDialog(
                                lot,
                                view
                            )
                        }
                        bindItem.etWeight.onChange {
                            if (it.isNotEmpty()) {
                                val editVal: String?
                                val dot = it.get(0).toString()
                                editVal = if (dot == ".") {
                                    if (it.length == 1) "0.0" else "0".plus(it)
                                } else it

                                lot.editedWeight = editVal
                                val come: Int? =
                                    editVal.toDouble().compareTo(lot.weight?.toDouble() ?: 0.0)
                                if (come ?: 0 <= 0) {
                                    lot.isLowerWeight = true
                                    bindItem.etWeight.error = null
                                    vm.lots.forEach { item ->
                                        if (lot.batchNumber.equals(item.batchNumber) && lot.materialCode.equals(
                                                item.materialCode
                                            )
                                        ) item.editedWeight =
                                            editVal
                                    }
                                } else {
                                    lot.isLowerWeight = false
                                    bindItem.etWeight.error =
                                        bindItem.etWeight.context.getString(R.string.less_weight_error)
                                }
                            } else {
                                lot.editedWeight = "0"
                            }
                            vm.lots.remove(lot)
                            vm.lots.add(lot)
                        }
                    }
                }

            }, itemClick = {

            })
    }


    fun weightUpdated(item: VegaCoffeeSalesLots) {
        //vm.updateLotWeightInfo(item)
    }

    private fun showConformationDialog(lot: VegaCoffeeSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.lots.remove(lot)
                    vm.removeLotDetails(lot)
                    vm.saveWeighBridgeAndLotDetails()
                },
                { dismiss() })
        }
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
                //vm.lots.forEach { vm.removeLotFromList(it.batchNumber) }
                dismiss()
            }
        }
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

    private fun enableEnterLotId(enable: Boolean) {
        binding.etEnterContainer.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
        binding.clScan.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
    }

    private fun updateUIWithLocalData() {
        /* binding.tvCustomerValue.text = vm.dispatchWh.customerName
         binding.tvStoWeightValue.text = vm.dispatchWh.soWeight
         binding.tvstoValue.text = vm.dispatchWh.saleOrderId
         binding.tvMaterialName.text = vm.dispatchWh.materialName
         binding.etOperatorName.setText(vm.dispatchWh.operatorName.toString())*/
    }

    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        return selected.isEmpty()
    }

    private fun validateLotWeightWithMaterialWeight(): Boolean {
        var valueExceed = false
        var materialUOM = ""
        var lotsUOM = "MT"
        materialListItems.forEach { mat ->
            materialUOM = mat.meins.toString()
            val weight = vm.lots.filter { it.materialCode.equals(mat.materialNumber) }
            if (weight.size > 0) lotsUOM = weight[0].unitOfMeasure.toString()
            when {
                materialUOM.equals("MT") && lotsUOM.equals("MT") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
                materialUOM.equals("MT") && lotsUOM.equals("KG") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    }.div(1000) > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
                materialUOM.equals("KG") && lotsUOM.equals("MT") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble()?.div(1000) ?: 0.0) valueExceed = true
                materialUOM.equals("KG") && lotsUOM.equals("KG") -> if (weight.sumByDouble {
                        it.editedWeight.toString().toDouble()
                    } > mat.openQuantity?.toDouble() ?: 0.0) valueExceed = true
            }

        }
        return !valueExceed
    }

    private fun fetchLotDetails(lotId: String, materialList: List<String>, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        showLoading()
        vm.getLotDetails(lotId, materialList, whId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    vm.currentScanLot = it
                    vm.validateLot(it)
                    // fetchLotDetails(it, materialList, vm.salesOrder.plantId ?: "")
                }
            }
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
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btProceed,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    private fun enableSave(enable: Boolean) {
        binding.btSave.isEnabled = enable
        when (enable) {
            true -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(
                    requireActivity(),
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btSave,
                ContextCompat.getColorStateList(requireActivity(), android.R.color.darker_gray)
            )
        }
    }

    fun updateLotList(lots: ArrayList<VegaCoffeeSalesLots>) {
        updateAdapter(lots)
    }


    private fun validateZeroWeight(): Boolean {
        val selected = vm.lots.filter { (it.editedWeight ?: "0").toDouble() <= 0 }
        return selected.isEmpty()
    }

    fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        editLotId = lotId.batchNumber
        editLotMaterial = lotId.materialCode
        vm.getDispatchSalesItem(model.saleOrderId, model.salesType, model.salesTempId)
    }

    fun getBack() {
        editLotId = ""
        editLotMaterial = ""
        vm.getDispatchSalesItem(vm.salesOrder.saleOrderId, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
    }

    private fun showTTDialog() {
        MaterialDialog(requireContext()).show {
            cancelOnTouchOutside(false)
            message(com.olam.warehouse.login.R.string.select_procurement_type)
            UIUtils.getTTDialogOnlyForMaterial(
                this,
                "",
                "",

                {
                        isComplaint, isThirdParty,isFarmerLessTransaction ->  if(isComplaint) isCompliantMaterial = true else false
                    vm.getAllProduct()
                    vm.getPurchaseOrder()
                },
                { dismiss() },
                false)
        }

    }
}
