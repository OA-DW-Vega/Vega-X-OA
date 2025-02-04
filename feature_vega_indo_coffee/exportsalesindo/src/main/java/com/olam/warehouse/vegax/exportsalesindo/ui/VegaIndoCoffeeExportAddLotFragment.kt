package com.olam.warehouse.vegax.exportsalesindo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesindo.R
import com.olam.warehouse.vegax.exportsalesindo.databinding.FragmentIndoCoffeeSalesAddLotBinding
import com.olam.warehouse.vegax.exportsalesindo.utils.CONTAINER_DATA
import com.olam.warehouse.vegax.exportsalesindo.utils.INVENTORY_FRAG
import com.olam.warehouse.vegax.exportsalesindo.utils.MATERIAL_LIST
import kotlinx.android.synthetic.main.item_indo_coffee_sales_add_lot.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeExportAddLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_sales_add_lot
    private val vm: VegaIndoCoffeeExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentIndoCoffeeSalesAddLotBinding
    private var container = VegaCoffeeExportSalesContainer()
    private var materialList = ArrayList<String>()
    private var lotList = mutableListOf<VegaCoffeeExportSalesLots>()
    private var localAddedLots = listOf<VegaCoffeeExportSalesLots>()

    companion object {
        fun newInstance(container: VegaCoffeeExportSalesContainer, materialList: ArrayList<String>) =
            VegaIndoCoffeeExportAddLotFragment().putArgs {
                putParcelable(CONTAINER_DATA, container)
                putStringArrayList(MATERIAL_LIST, materialList)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(
            inventoryFrag: String,
            selectedList: ArrayList<String>,
            materialList: java.util.ArrayList<String>
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescoffee/ui/VegaCoffeeExportAddLotFragment").title("Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeSalesAddLotBinding.inflate(layoutInflater)

        return binding.root
    }

    private fun initExtra() {
        container = arguments?.getParcelable(CONTAINER_DATA) ?: VegaCoffeeExportSalesContainer()
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>

    }

    private fun initUI() {
        enableProceed(false)
        binding.tvContainer.text = getString(R.string.container_no).plus(" : ").plus(container.containerNumber)
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateLotInfo(it) })
        binding.btAdd.setOnClickListener {
            vm.currentScanLot = binding.etEnterContainer.text.toString()
            vm.validateLot(binding.etEnterContainer.text.toString())
        }
        binding.clInventory.setOnClickListener {
            val seletecItems = arrayListOf<String>()
            val lots = lotList.map { it.batchNumber }
            seletecItems.addAll(lots)
            vm.lotList.forEachIndexed { index, it ->
                it.slPostion = if (it.slPostion == 0) index else it.slPostion
                it.saleOrderId = container.saleOrderId
                it.containerNumber = container.containerNumber
                it.tmpId = container.tmpId
            }
            vm.saveLotDetails(vm.lotList as ArrayList<VegaCoffeeExportSalesLots>)
            callback?.replaceFragment(
                INVENTORY_FRAG,
                seletecItems,
                materialList
            )
        }

        vm.validateLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showLotAlreadyExistDialog(true)
                binding.etEnterContainer.setText("")
            } else
                fetchLotDetails(
                    vm.currentScanLot,
                    materialList, PreferenceHelper.get(Constants.WERKS, "")
                )
        })

        binding.btProceed.setOnClickListener { showConfirmSaveDialog() }

        vm.containerLots.observe(viewLifecycleOwner, Observer { updateContainerWithLotInfo(it) })
        vm.getContainerWithLots(container.containerNumber)
        vm.addedLots.observe(viewLifecycleOwner, Observer { localAddedLots = it })
        vm.getAddedLotList()
    }

    private fun showConfirmSaveDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.lotList.forEachIndexed { index, it ->
                        it.slPostion = if (it.slPostion == 0) index else it.slPostion
                        it.saleOrderId = container.saleOrderId
                        it.containerNumber = container.containerNumber
                        it.tmpId = container.tmpId
                    }
                    vm.saveLotDetails(vm.lotList as ArrayList<VegaCoffeeExportSalesLots>)
                    activity?.onBackPressed()
                },
                { dismiss() })
        }
    }

    private fun updateContainerWithLotInfo(containerWithLots: IndoContainerWithLots) {
        updateLotAdapter(containerWithLots.lots)
    }

    private fun fetchLotDetails(lotId: String, materialList: List<String>, whId: String) {
        binding.etEnterContainer.hideKeyboard()
        showLoading()
        vm.getLotDetails(lotId, materialList, whId)
    }

    private fun updateLotAdapter(lots: List<VegaCoffeeExportSalesLots>) {
        lotList = mutableListOf<VegaCoffeeExportSalesLots>()
        vm.lotList = lotList
        if (lots.size > 0) {
            lotList.addAll(lots)
            binding.rvLots.visible()
            binding.tvNoData.gone()
            enableProceed(true)
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
            enableProceed(false)
        }
        binding.rvLots.setUp(
            lotList.sortedByDescending { it.slPostion }.toMutableList(),
            R.layout.item_indo_coffee_sales_add_lot,
            { it, pos ->
                val lot = it
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
                    vm.lotList.remove(lot)
                    vm.lotList.add(lot)
                }
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
                            vm.lotList.forEach { item ->
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
                    vm.lotList.remove(lot)
                    vm.lotList.add(lot)
                }

            })
    }

    private fun showConformationDialog(lot: VegaCoffeeExportSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.lotList.remove(lot)
                    vm.removeLotDetails(lot)
                    vm.lotList.forEachIndexed { index, it ->
                        it.slPostion = if (it.slPostion == 0) index else it.slPostion
                        it.saleOrderId = container.saleOrderId
                        it.containerNumber = container.containerNumber
                        it.tmpId = container.tmpId
                    }
                    vm.saveLotDetails(vm.lotList as ArrayList<VegaCoffeeExportSalesLots>)
                },
                { dismiss() })
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

    private fun updateLotInfo(response: Resource<GenericReqAndResp<List<VegaCoffeeExportSalesLots>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        when (it1.size == 1) {
                            true -> updateAdapter(it1 as ArrayList<VegaCoffeeExportSalesLots>)
                            else -> chooseOneLotDialog(it1)
                        }

                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateAdapter(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) {
        vegaCoffeeSalesLots.forEachIndexed { index, it ->
            it.slPostion = if (it.slPostion == 0) index else it.slPostion
            it.saleOrderId = container.saleOrderId
            it.containerNumber = container.containerNumber
            it.tmpId = container.tmpId
        }
        vm.saveLotDetails(vegaCoffeeSalesLots)
        binding.etEnterContainer.setText("")
        enableProceed(true)
    }

    private fun chooseOneLotDialog(lots: List<VegaCoffeeExportSalesLots>) {
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

    fun updateLotList(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        val alreadyLots = arrayListOf<String>()
        val needToAddLots = arrayListOf<VegaCoffeeExportSalesLots>()
        val locaAddedBatch = localAddedLots.map { it.batchNumber }
        lots.forEach {
            if (localAddedLots.any { item -> item.batchNumber.equals(it.batchNumber) && item.materialCode.equals(it.materialCode) })
                alreadyLots.add(it.batchNumber)
            else
                needToAddLots.add(it)
        }
        if (alreadyLots.size == 0)
            updateAdapter(lots)
        else
            showExistLotsDialog(needToAddLots, alreadyLots)
    }

    fun showExistLotsDialog(
        needToAddLots: ArrayList<VegaCoffeeExportSalesLots>,
        alreadyLots: ArrayList<String>
    ) {
        MaterialDialog(requireContext()).show {
            val msg = getString(R.string.already_added_other).plus("\n").plus(alreadyLots)
            message(R.string.already_added_other, msg)
            positiveButton(
                text = UIUtils.getSpannedText(
                    getString(com.olam.warehouse.presentation.R.string.ok),
                    true
                )
            ) {
                updateAdapter(needToAddLots)
                dismiss()
            }
        }
    }
}
