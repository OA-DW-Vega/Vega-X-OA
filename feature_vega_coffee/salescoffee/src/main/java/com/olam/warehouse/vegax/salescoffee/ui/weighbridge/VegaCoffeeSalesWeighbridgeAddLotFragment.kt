package com.olam.warehouse.vegax.salescoffee.ui.weighbridge

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
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesOrder
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesLotWithbags
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeSalesOrderWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getCurrentTimeInMills
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.salescoffee.R
import com.olam.warehouse.vegax.salescoffee.data.domain.model.VegaCoffeeSalesOrderModel
import com.olam.warehouse.vegax.salescoffee.data.domain.model.materialList
import com.olam.warehouse.vegax.salescoffee.databinding.FragmentCoffeeSalesWbAddLotBinding
import com.olam.warehouse.vegax.salescoffee.ui.CustomCoffeeSingleSelectDialog
import com.olam.warehouse.vegax.salescoffee.ui.RecyclerViewItemClickListener
import com.olam.warehouse.vegax.salescoffee.ui.VegaCoffeeSalesViewModel
import com.olam.warehouse.vegax.salescoffee.utils.*
import kotlinx.android.synthetic.main.item_coffee_sales_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random

class VegaCoffeeSalesWeighbridgeAddLotFragment : BaseFragment(), RecyclerViewItemClickListener {

    private var callBack: CallBack? = null
    private val vm: VegaCoffeeSalesViewModel by viewModel()
    private var dispatchType = ""
    private var salesOrderList = mutableListOf<VegaCoffeeSalesOrderModel>()
    private var salesOrderCompleteList = mutableListOf<materialList>()
    private var materialFilterlist = ArrayList<materialList>()
    private var materialCode: String = ""
    private var materialList = ArrayList<String>()
    private var materialListItems = ArrayList<materialList>()
    private var soNumbers = ArrayList<String>()
    private var isEditableLot = true
    private var isMultipleLot = false
    private var isAlreadyLoading = false
    private var isReceived = false
    private var customDialog: CustomCoffeeSingleSelectDialog? = null
    private var editLotId: String = ""
    private var editLotMaterial: String = ""


    override val layoutResourceId: Int = R.layout.fragment_coffee_sales_wb_add_lot
    private lateinit var binding: FragmentCoffeeSalesWbAddLotBinding

    companion object {
        fun newInstance(dispatch: VegaCoffeeSalesOrder, dispatchType: String) =
            VegaCoffeeSalesWeighbridgeAddLotFragment().putArgs {
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

        fun replaceFragmentLotList(
            fragment: String,
            model: VegaCoffeeSalesOrder,
            materialList: ArrayList<String>,
            isMultiple: Boolean
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
        binding = FragmentCoffeeSalesWbAddLotBinding.inflate(inflater)
        initExtra()
        initUI()
        /*listener = this*/
        return binding.root
    }


    private fun initExtra() {
        vm.salesOrder = arguments?.getParcelable<VegaCoffeeSalesOrder>(SALES_ITEM) as VegaCoffeeSalesOrder
        dispatchType = arguments?.getString(SALES_TYPE) ?: ""
        vm.salesOrder.salesType = dispatchType
        vm.currentTempId = vm.salesOrder.salesTempId
        binding.tvMaterialValue.text = vm.salesOrder.materialCode.plus("-").plus(vm.salesOrder.materialName)
        materialList.add(vm.salesOrder.materialCode)
    }

    private fun initUI() {
        disableAddLotsButtons()
        enableEnterLotId(false)
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        vm.getPurchaseOrder()
        vm.dispatchSalesItem.observe(
            viewLifecycleOwner,
            Observer { if (it != null) updateSalesItem(it) else updateSalesItem(VegaCoffeeSalesOrderWithLots()) })

        var titleString = ""
        when (dispatchType) {
            SALES_TYPE_WEIGHSCALE -> titleString = getString(R.string.weighscale)
            SALES_TYPE_ANTICIPATED -> titleString = getString(R.string.anticipated)
            SALES_TYPE_WEIGHBRIDGE -> titleString = getString(R.string.weighbridge)
        }
        binding.tvTitle.text = getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ").plus(titleString)
        binding.tvTruckValue.text = vm.salesOrder.truckNo
        binding.tvstoValue.setOnClickListener { showSingleSelectDialog(true, getString(R.string.select_so)) }
        binding.clInventory.setOnClickListener {
            callBack?.replaceFragmentLotList(
                INVENTORY_FRAG,
                vm.salesOrder,
                materialList, false
            )
        }
        binding.btProceed.setOnClickListener {
            if (vm.lots.isNotEmpty())
                if (validateLotWeightWithMaterialWeight()) {
                    if (validateLotWeight()) {
                        if (validateZeroWeight()) {
                            if (editLotId.isEmpty() && (dispatchType.equals(SALES_TYPE_WEIGHSCALE) || dispatchType.equals(
                                    SALES_TYPE_WEIGHBRIDGE
                                ))
                            ) {
                                showRemarkDialog()
                            } else {
                                if (dispatchType.equals(SALES_TYPE_WEIGHSCALE)) {
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
        vm.salesOrder.remarks = remark
        getCurrentDate("END")
        vm.saveWeighBridgeAndLotDetails()
        vm.salesOrder.lotList = vm.lots
        val startTime = vm.salesOrder.startTime?.toLong() ?: 0
        val duration = vm.salesOrder.endTime?.toLong()?.minus(startTime)
        vm.salesOrder.turnAroundTime = TimeUnit.MILLISECONDS.toMinutes(duration ?: 0).toString()
        callBack?.replaceFragmentSummary(SUMMARY, vm.salesOrder, materialListItems)
    }

    private fun showConfirmSaveDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    if (vm.salesOrder.salesTempId.isBlank()) {
                        val randomDouble = "TMP".plus(Random.nextLong().toString())
                        vm.salesOrder.salesTempId = randomDouble
                    }
                    vm.saveWeighBridgeAndLotDetails()
                    Toast.makeText(activity, getString(R.string.local_save_msg), Toast.LENGTH_SHORT)
                        .show()
                    activity?.finish()
                },
                { dismiss() })
        }
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaCoffeeSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    salesOrderList = it.data?.data as MutableList<VegaCoffeeSalesOrderModel>
                    salesOrderCompleteList.clear()
                    //materialFilterlist.clear()
                    salesOrderList.forEach {
                        salesOrderCompleteList.addAll(it.salesOrderList)
                    }
                    materialFilterlist.addAll(salesOrderCompleteList.filter {
                        vm.salesOrder.materialCode.trim().contains(it.materialNumber?.trim() ?: "  ")
                    }.filter { it.openQuantity?.toDouble() ?: 0.0 > 0.0 })
                    soNumbers.clear()
                    soNumbers = materialFilterlist.map { it.salesOrderId } as ArrayList<String>
                    val distinct = soNumbers.distinct()
                    soNumbers.clear()
                    soNumbers.addAll(distinct)
                    //if (vm.salesOrder.salesTempId.isNotEmpty()) clickOnItem(vm.salesOrder.saleOrderId, true)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
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
            ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.grey)
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.grey)
        )
    }

    private fun enableAddLotButtons() {
        binding.clScan.isEnabled = true
        binding.clInventory.isEnabled = true
        binding.etEnterContainer.isEnabled = true
        ViewCompat.setBackgroundTintList(
            binding.clScan,
            ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.dark_green_1)
        )
        ViewCompat.setBackgroundTintList(
            binding.clInventory,
            ContextCompat.getColorStateList(activity!!, com.olam.warehouse.presentation.R.color.dark_green_1)
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
        val stamp = Timestamp(System.currentTimeMillis())
        val date = Date(stamp.time)
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val dat = dateFormat.format(date)
        when (type) {
            "START" -> vm.salesOrder.startTime = getCurrentTimeInMills().toString()
            "END" -> vm.salesOrder.endTime = getCurrentTimeInMills().toString()
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
                        UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    }
                }
            }
        }
    }

    private fun showSingleSelectDialog(isWh: Boolean, title: String) {
        customDialog =
            CustomCoffeeSingleSelectDialog(title, isWh, soNumbers, activity!!, this)
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, isWh: Boolean) {
        enableProceed(false)
        customDialog?.dismiss()
        val selectedSales = salesOrderList.filter { it.salesOrderId == data } as ArrayList

        binding.tvstoValue.text = data
        vm.salesOrder.saleOrderId = data
        enableEnterLotId(true)
        vm.getDispatchSalesItem(data, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
        updateSODetails(selectedSales[0])
    }

    private fun updateSODetails(vegaCoffeeSalesOrderModel: VegaCoffeeSalesOrderModel) {
        val selectedSales = vegaCoffeeSalesOrderModel.salesOrderList
        materialListItems.clear()
        materialListItems.addAll(selectedSales)
        if (selectedSales.isNotEmpty()) {
            binding.tvCustomerValue.text = selectedSales[0].soldToPartyName
            vm.salesOrder.customerId = selectedSales[0].soldToPartyCode
            vm.salesOrder.customerName = selectedSales[0].soldToPartyName
            vm.salesOrder.createdDate = selectedSales[0].createdDate
            vm.salesOrder.salesItem = selectedSales[0].salesItemNum
            enableSave(true)
            binding.tvSoWeightValue.text = selectedSales[0].openQuantity.plus(" ").plus(selectedSales[0].meins)
            //setUpMaterialAdapter(selectedSales)
            enableAddLotButtons()
        }
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
        getCurrentDate("START")
        vegaCoffeeSalesLots.forEachIndexed { index, it ->
            it.slPostion = if (it.slPostion == 0) index else it.slPostion
            it.saleOrderId = vm.salesOrder.saleOrderId
            it.salesTempId = vm.salesOrder.salesTempId
            it.salesType = vm.salesOrder.salesType
            if (!vm.lots.any { item -> item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode) }) vm.lots.add(
                it
            )
        }
        vm.saveWeighBridgeAndLotDetails()
        binding.etEnterContainer.setText("")
        enableProceed(true)
    }

    private fun setUpAdapter(lineItems: MutableList<VegaCoffeeSalesLotWithbags>) {
        if (vm.salesOrder.startTime?.isNullOrBlank() == true)
            vm.salesOrder.startTime = getCurrentTimeInMills().toString()
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
            //if (vm.currentTempId.isEmpty()) binding.tvstoValue.isEnabled = true
            enableAddLotButtons()
            lineItems1 = lineItems
        }
        binding.rvLots.setUp(
            lineItems1.sortedByDescending { it.salesLot.slPostion }.toMutableList(),
            R.layout.item_coffee_sales_lot,
            { it, pos ->
                val lot = it.salesLot
                flSalesLot.gone()
                flAnticipatedSales.visible()
                tvLotId.text = lot.batchNumber
                tvStLocationValue1.text = lot.storageLocationCode
                tvGradeValue.text = lot.materialName
                tvWeightValue.text = lot.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(lot.unitOfMeasure)
                cbSelectAll.isChecked = lot.isChecked ?: false
                etWeight.setText(lot.editedWeight)
                llSelectAll.setOnClickListener { view ->
                    lot.isChecked = !lot.isChecked!!
                    if (lot.isChecked!!) {
                        cbSelectAll.isChecked = true
                        lot.editedWeight = lot.weight?.toDouble()?.formatThreeDigits()

                    } else {
                        cbSelectAll.isChecked = false
                        lot.editedWeight = "0"
                    }
                    etWeight.setText(lot.editedWeight)
                    vm.lots.remove(lot)
                    vm.lots.add(lot)
                }
                cbStorageLoss.isChecked = lot.endLotFlag ?: false
                llEndLot.setOnClickListener { view ->
                    lot.endLotFlag = !lot.endLotFlag!!
                    cbStorageLoss.isChecked = lot.endLotFlag ?: false
                    vm.lots.remove(lot)
                    vm.lots.add(lot)
                    vm.saveWeighBridgeAndLotDetails()
                }

                if (editLotId.isNotEmpty()) ivClose.gone() else ivClose.visible()
                ivClose.setOnClickListener { view -> showConformationDialog(lot, view) }
                etWeight.onChange {
                    if (it.isNotEmpty()) {
                        val editVal: String?
                        val dot = it.get(0).toString()
                        editVal = if (dot == ".") {
                            if (it.length == 1) "0.0" else "0".plus(it)
                        } else it

                        lot.editedWeight = editVal
                        val come: Int? = editVal.toDouble().compareTo(lot.weight?.toDouble() ?: 0.0)
                        if (come ?: 0 <= 0) {
                            lot.isLowerWeight = true
                            etWeight.error = null
                            vm.lots.forEach { item ->
                                if (lot.batchNumber.equals(item.batchNumber) && lot.materialCode.equals(item.materialCode)) item.editedWeight =
                                    editVal
                            }
                        } else {
                            lot.isLowerWeight = false
                            etWeight.error = etWeight.context.getString(R.string.less_weight_error)
                        }
                    } else {
                        lot.editedWeight = "0"
                    }
                    vm.lots.remove(lot)
                    vm.lots.add(lot)
                }

            }, itemClick = {

            })
    }


    /*fun weightUpdated(item: VegaCoffeeSalesLots) {
        //vm.updateLotWeightInfo(item)
    }*/

    private fun showConformationDialog(lot: VegaCoffeeSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
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

    private fun enableEnterLotId(enable: Boolean) {
        binding.etEnterContainer.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
        binding.clScan.isEnabled = ((vm.lots.isEmpty() || isMultipleLot) && enable)
    }

    /*private fun updateUIWithLocalData() {
        *//* binding.tvCustomerValue.text = vm.dispatchWh.customerName
         binding.tvStoWeightValue.text = vm.dispatchWh.soWeight
         binding.tvstoValue.text = vm.dispatchWh.saleOrderId
         binding.tvMaterialName.text = vm.dispatchWh.materialName
         binding.etOperatorName.setText(vm.dispatchWh.operatorName.toString())*//*
    }*/

    private fun validateLotWeight(): Boolean {
        val selected = vm.lots.filter { !it.isLowerWeight }
        return selected.isEmpty()
    }

    private fun validateLotWeightWithMaterialWeight(): Boolean {
        var valueExceed = false
        var materialUOM = ""
        var lotsUOM = "KG"
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
                }
            }
        }
    }

    /*private fun removeScanLot(isRemove: Boolean) {
        binding.clScan.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.tvOr.visibility = if (isRemove) View.GONE else View.VISIBLE
    }

    private fun removeManualLot(isRemove: Boolean) {
        binding.etEnterContainer.visibility = if (isRemove) View.GONE else View.VISIBLE
        binding.btAdd.visibility = if (isRemove) View.GONE else View.VISIBLE
    }*/

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

    fun updateLotList(lots: ArrayList<VegaCoffeeSalesLots>) {
        if (vm.lots.isNotEmpty()) {
            val lot = vm.lots[0]
            vm.removeLotDetails(lot)
            vm.lots.remove(lot)
        }
        updateAdapter(lots)
    }


    private fun validateZeroWeight(): Boolean {
        val selected = vm.lots.filter { (it.editedWeight ?: "0").toDouble() <= 0 }
        return selected.isEmpty()
    }

    /*fun editLotDetails(model: VegaCoffeeSalesOrder, lotId: VegaCoffeeSalesLots) {
        editLotId = lotId.batchNumber
        editLotMaterial = lotId.materialCode
        vm.getDispatchSalesItem(model.saleOrderId, model.salesType, model.salesTempId)
    }

    fun getBack() {
        editLotId = ""
        editLotMaterial = ""
        vm.getDispatchSalesItem(vm.salesOrder.saleOrderId, vm.salesOrder.salesType, vm.salesOrder.salesTempId)
    }*/
}
