package com.olam.warehouse.vegax.exportsalescameroon.ui

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
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalescameroon.R
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.VegaCameroonExportSalesWSBagModel
import com.olam.warehouse.vegax.exportsalescameroon.databinding.FragmentExportSalesCameroonAddLotBinding
import com.olam.warehouse.vegax.exportsalescameroon.databinding.ItemExportCameroonSalesAddLotBinding
import com.olam.warehouse.vegax.exportsalescameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCameroonExportAddLotFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_export_sales_cameroon_add_lot
    private val vm: VegaCameroonExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentExportSalesCameroonAddLotBinding
    private var container = VegaCoffeeExportSalesContainer()
    private var materialList = ArrayList<String>()
    private var lotList = mutableListOf<VegaCoffeeExportSalesLots>()
    private var localAddedLots = listOf<VegaCoffeeExportSalesLots>()
    private var addWeightPosition = 0
    private var moreWeightBatches = ""
    private var palletCountNotMatchBatches = ""
    private var currentKey = getCurrentKey()

    companion object {
        fun newInstance(container: VegaCoffeeExportSalesContainer, materialList: ArrayList<String>) =
            VegaCameroonExportAddLotFragment().putArgs {
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
        fun replaceFragment(
            flag:String,
            data:Any
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescameroon/ui/VegaCameroonExportAddLotFragment")
            .title("Vega_Cameroon/Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentExportSalesCameroonAddLotBinding.inflate(layoutInflater)

        return binding.root
    }

    private fun initExtra() {
        container = arguments?.getParcelable(CONTAINER_DATA) ?: VegaCoffeeExportSalesContainer()
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<String>

    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        enableProceed(false)
        binding.tvContainer.text =
            getString(R.string.container_no).plus(" : ").plus(container.containerNumber)
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

        binding.btProceed.setOnClickListener {
            validateProceed()
        }

        vm.containerLots.observe(viewLifecycleOwner, Observer { updateContainerWithLotInfo(it) })
        vm.getContainerWithLots(container.containerNumber)
        vm.addedLots.observe(viewLifecycleOwner, Observer { localAddedLots = it })
        vm.getAddedLotList()
    }

    private fun validateProceed() {
        if (vm.lotList.isNotEmpty()) {
            if (validateEmptyWeight()) {
                if (validateLotWeight()) {
                    if (validatePalletCount())
                        showConfirmSaveDialog()
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
    private fun validateLotWeight(): Boolean {
        val selected = vm.lotList.filter { !it.isLowerWeight!! }
        moreWeightBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validatePalletCount(): Boolean {
        val selected = vm.lotList.filter { !it.isBagCountMatched!! }
        palletCountNotMatchBatches = selected.map { it.batchNumber }.toString().replace("[", "").replace("]", "")
        return selected.isEmpty()
    }

    private fun validateEmptyWeight(): Boolean {
        val emptyWeight =
            vm.lotList.filter { it.editedWeight.equals("0.0") || it.editedWeight.equals("0") || it.editedWeight.equals("") }
        return emptyWeight.isEmpty()
    }

    private fun showConfirmSaveDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.lotList.forEachIndexed { index, it ->
                        it.slPostion = if (it.slPostion == 0) index else it.slPostion
                        it.saleOrderId = container.saleOrderId
                        it.containerNumber = container.containerNumber
                    }
                    vm.saveLotDetails(vm.lotList as ArrayList<VegaCoffeeExportSalesLots>)
                    activity?.onBackPressed()
                },
                { dismiss() })
        }
    }

    private fun updateContainerWithLotInfo(containerWithLots: ContainerWithLots) {
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
            if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains("COFF")) {
                if (vm.lotList.size == 1) binding.clInventory.isEnabled = false
            }
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
            enableProceed(false)
        }

        binding.rvLots.setUpAdapter(
            lotList.toMutableList(),
            R.layout.item_export_cameroon_sales_add_lot,
            ItemExportCameroonSalesAddLotBinding::inflate,
            { it, pos, bindItem ->
                val lot = it
                bindItem.tvLotId.text = lot.batchNumber
                bindItem.tvStLocationValue1.text = lot.storageLocationCode
                bindItem.tvGradeValue.text = lot.materialName
                var lotWeightValue = ""
                when (lot.unitOfMeasure) {
                    "KG" -> {
                        lotWeightValue = lot.weight.toString()
                    }
                    "MT" -> {
                        lotWeightValue = convertMtToKg(lot.weight.toString())
                    }
                }
                bindItem.tvWeightValue.text =
                    lotWeightValue.toDouble().formatThreeDigits().plus(" ").plus("KG")
                val editedWeight =
                    if (lot.editedWeight.isNullOrEmpty()) "0.0" else lot.editedWeight.toString()
                        .toDouble()
                        .formatThreeDigits()
                bindItem.etWeight.text = editedWeight.plus(" ${lot.weightToDispatchUOM}")
                bindItem.ivClose.setOnClickListener { view -> showConformationDialog(lot, view) }

                bindItem.tvAddWeight.setOnClickListener {
                    addWeightPosition = pos
                    callback?.replaceFragment(ADD_WEIGHT, lot)
                }
                bindItem.cbEndLot.isChecked = lot.isChecked ?: false
                bindItem.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
                    lot.isChecked = isChecked
                }


            })
    }

    private fun showConformationDialog(lot: VegaCoffeeExportSalesLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.lotList.remove(lot)
                    vm.removeLotDetails(lot)
                    vm.lotList.forEachIndexed { index, it ->
                        it.slPostion = if (it.slPostion == 0) index else it.slPostion
                        it.saleOrderId = container.saleOrderId
                        it.containerNumber = container.containerNumber
                    }
                    if (currentKey.split("_")[1].contains("NI") && currentKey.split("_")[2].contains(
                            "COFF"
                        )
                    ) {
                        if (vm.lotList.size == 0) binding.clInventory.isEnabled = true
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
                    vm.currentScanLot = it
                    vm.validateLot(it)
                }
            }
        }
    }

    fun updateAddWeight(weight: VegaCameroonExportSalesWSBagModel) {
        val split = weight.weight.split(" ")
        vm.lotList[addWeightPosition].editedWeight = split[0]
        vm.lotList[addWeightPosition].weightToDispatchUOM = split[1]
        binding.rvLots.adapter?.notifyItemChanged(addWeightPosition)
        var come :Int? = 0
        when(vm.lotList[addWeightPosition].unitOfMeasure){
            "KG" -> {
                come = split[0].toDouble().compareTo(vm.lotList[addWeightPosition].weight?.toDouble() ?: 0.0)
            }
            "MT" -> {
                come = split[0].toDouble().compareTo(
                    com.olam.warehouse.master.work.convertMtToKg(vm.lotList[addWeightPosition].weight.toString())
                        .toDouble()
                )
            }
        }
        vm.lotList[addWeightPosition].isBagCountMatched = weight.isPalletMatched
        vm.lotList[addWeightPosition].isLowerWeight = come ?: 0 <= 0
        vm.saveLotDetails(vm.lotList as ArrayList<VegaCoffeeExportSalesLots>)

        calculateAndUpdateWeightToDispatch()
    }

    private fun calculateAndUpdateWeightToDispatch() {
        val materialWeightMap = HashMap<String, String>()
        vm.lotList.forEach {
            val data = materialWeightMap[it.materialCode]
            val editWeight =
                if (it.editedWeight.isNullOrEmpty()) 0.0 else it.editedWeight?.toDouble()
            if (data != null) {
                val sum = data.toDouble().plus(editWeight!!)
                materialWeightMap[it.materialCode] = sum.toString()
            } else materialWeightMap[it.materialCode] = editWeight.toString()
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
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateAdapter(vegaCoffeeSalesLots: ArrayList<VegaCoffeeExportSalesLots>) {
        vegaCoffeeSalesLots.forEachIndexed { index, it ->
            it.slPostion = if (it.slPostion == 0) index else it.slPostion
            it.saleOrderId = container.saleOrderId
            it.containerNumber = container.containerNumber
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

    fun updateLotList(lots: ArrayList<VegaCoffeeExportSalesLots>) {
        val alreadyLots = arrayListOf<String>()
        val needToAddLots = arrayListOf<VegaCoffeeExportSalesLots>()
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
