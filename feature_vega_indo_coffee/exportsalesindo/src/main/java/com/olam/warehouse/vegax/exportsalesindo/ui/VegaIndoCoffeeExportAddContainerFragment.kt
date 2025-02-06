package com.olam.warehouse.vegax.exportsalesindo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegaindocoffee.entity.IndoExporSalesMaterialList
import com.olam.warehouse.master.vegaindocoffee.entity.VegaIndoCoffeeExportSalesOrder
import com.olam.warehouse.master.vegaindocoffee.model.IndoContainerWithLots
import com.olam.warehouse.master.vegaindocoffee.model.VegaIndoCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalesindo.R
import com.olam.warehouse.vegax.exportsalesindo.data.domain.model.VegaIndoCoffeeExportSalesOrderModel
import com.olam.warehouse.vegax.exportsalesindo.databinding.FragmentIndoCoffeeSalesAddContainerBinding
import com.olam.warehouse.vegax.exportsalesindo.databinding.ItemIndoCoffeeExportSalesLotBinding
import com.olam.warehouse.vegax.exportsalesindo.databinding.ItemIndoCoffeeSalesMaterialListBinding
import com.olam.warehouse.vegax.exportsalesindo.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/9/2021.
 */
class VegaIndoCoffeeExportAddContainerFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_indo_coffee_sales_add_container
    private val vm: VegaIndoCoffeeExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentIndoCoffeeSalesAddContainerBinding
    private var salesOrderList = listOf<VegaIndoCoffeeExportSalesOrderModel>()
    private var materialList = ArrayList<String>()
    private var materialListItems = ArrayList<IndoExporSalesMaterialList>()
    private var soNumbers = ArrayList<String>()
    private var editContainerId: String = ""
    private var saleOrderTrans = VegaIndoCoffeeExportSalesOrder()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(
            fragment: String,
            data: Any,
            materialList: ArrayList<String>
        )

        fun replaceSummaryFragment(
            fragment: String,
            data: Any,
            data1: Any,
            materialList: ArrayList<IndoExporSalesMaterialList>
        )
    }

    companion object {
        fun newInstance(it: VegaIndoCoffeeExportSalesOrder) = VegaIndoCoffeeExportAddContainerFragment().putArgs {
            putParcelable(SALES_ORDER, it)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentIndoCoffeeSalesAddContainerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescoffee/ui/VegaCoffeeExportAddContainerFragment").title("Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    private fun initExtra() {
        saleOrderTrans = arguments?.getParcelable(SALES_ORDER) ?: VegaIndoCoffeeExportSalesOrder()
    }

    private fun initUI() {
        enableProceed(false)
        vm.dispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrder(it) })
        vm.offlineDispatchSalesOrderModel.observe(viewLifecycleOwner, Observer { updateSalesOrderOffline(it) })
        if (isOnline()) vm.getPurchaseOrder() else vm.getOfflinePurchaseOrder()
        binding.etEnterContainer.onChange { enableAddLot(it) }
        binding.clScan.setOnClickListener { moveToScan() }
        binding.btAdd.setOnClickListener {
            validateLot(binding.etEnterContainer.text.toString())
        }
        binding.btProceed.setOnClickListener {
            var isEmptyWeight = false
            vm.containerList.forEach {
                if (it.lots.size == 0 || it.lots.any { it.editedWeight?.toDouble() ?: 0.0 <= 0 }) isEmptyWeight = true
            }
            if (!isEmptyWeight)
                callback?.replaceSummaryFragment(MOVE_SUMMARY, vm.currentOT, vm.currentTmpId, materialListItems)
            else
                activity?.toast(getString(R.string.add_weights))
        }

        if (saleOrderTrans.saleOrderId?.isNotEmpty() == true && saleOrderTrans.tmpId.isNotEmpty()) {
            vm.currentTmpId = saleOrderTrans.tmpId
            vm.currentOT = saleOrderTrans.saleOrderId.toString()
            vm.currentSaleOrder = prepareSalesOrderToMaterialList(saleOrderTrans)
            vm.containerList = ArrayList()
            setUpMaterialAdapter(prepareSalesOrderToMaterialList(saleOrderTrans))
            vm.getOTWithContainer(vm.currentTmpId)
        }

        vm.otContainer.observe(viewLifecycleOwner, Observer { updateContainer(it) })
        vm.validateContainer.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (vm.containerList.any { it1 -> it1.container.containerNumber.equals(it.containerNumber) })
                    activity?.toast(getString(R.string.conatiner_already_added))
                else {
                    activity?.toast(getString(R.string.conatiner_already_added_other))
                }
                binding.etEnterContainer.setText("")
                vm.currentOldContainer = ""
                vm.currentNewContainer = ""
            } else {
                if (vm.currentNewContainer.isEmpty() && vm.currentOldContainer.isEmpty()) {
                    val container = VegaCoffeeExportSalesContainer()
                    container.saleOrderId = vm.currentOT
                    container.containerNumber = vm.currentContainer
                    container.tmpId = getTmpId()
                    vm.currentTmpId = container.tmpId.toString()
                    val currentSO = arrayListOf<IndoExporSalesMaterialList>()
                    currentSO.addAll(vm.currentSaleOrder)
                    vm.saveContainer(container, currentSO)
                } else {
                    vm.updateContainerId(vm.currentOldContainer, vm.currentNewContainer)
                    vm.currentOldContainer = ""
                    vm.currentNewContainer = ""
                }
                vm.getOTWithContainer(vm.currentTmpId)
                /*if (vm.containerList.size > 0) {
                    if (vm.containerList.any { it.container.containerNumber.equals(vm.currentContainer) })
                        activity?.toast(getString(R.string.conatiner_already_added))
                    else {
                        container.containerNumber = vm.currentContainer
                        vm.saveContainer(container)
                    }
                } else {
                    container.containerNumber = vm.currentContainer
                    vm.saveContainer(container)
                }*/
            }

        })
    }

    private fun validateLot(containerId: String) {
        vm.currentContainer = containerId
        vm.validateContainer(containerId)
    }

    private fun updateContainer(containerList: VegaIndoCoffeeExportOTWithContainer?) {
        if (containerList != null) {
            vm.currentTmpId = containerList.salesOrder.tmpId
            updateContainerAdapter(containerList)
        } else
            updateContainerAdapter(VegaIndoCoffeeExportOTWithContainer())

    }

    private fun updateContainerAdapter(data: VegaIndoCoffeeExportOTWithContainer) {
        binding.etEnterContainer.setText("")
        val containerList = mutableListOf<IndoContainerWithLots>()
        if (data.lineItems.size > 0) {
            if (editContainerId.isNotEmpty()) {
                enableItems(false)
                containerList.addAll(data.lineItems.filter { it.container.containerNumber.equals(editContainerId) })
            } else {
                enableItems(true)
                containerList.addAll(data.lineItems)
            }
            binding.rvLots.visible()
            binding.tvNoData.gone()
            vm.containerList = data.lineItems as ArrayList<IndoContainerWithLots>
            enableProceed(true)
        } else {
            binding.rvLots.gone()
            binding.tvNoData.visible()
            enableProceed(false)
        }
        binding.rvLots.setUpAdapter(
            containerList,
            R.layout.item_indo_coffee_export_sales_lot,
            ItemIndoCoffeeExportSalesLotBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvContainerId.text = it.container.containerNumber
                val lotCount = it.lots.size
                val uom = if (lotCount > 0) it.lots[0].unitOfMeasure else ""
                val materialList = it.lots.map { it.materialName.toString() }.distinct()
                val totalWeight = it.lots.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }
                bindItem.tvLotsCount.text = if (lotCount != 0) lotCount.toString() else "-"
                bindItem.tvGradeValue.text =
                    if (materialList.size > 0) materialList.toString().replace("[", "")
                        .replace("]", "").replace(
                            ",",
                            "\n"
                        ) else "-"
                bindItem.tvWeightValue.text =
                    if (!totalWeight.equals(0.0)) totalWeight.toString().plus(" ")
                        .plus(uom) else "-"
                if (editContainerId.isNotEmpty()) bindItem.ivClose.gone() else bindItem.ivClose.visible()
                bindItem.ivClose.setOnClickListener { view -> showDeleteDialog(it, view) }
                bindItem.ivEdit.setOnClickListener { view ->
                    showContainerEditDialog(
                        it.container.containerNumber,
                        view
                    )
                }
            },
            {
                val item = this
                callback?.replaceFragment(MOVE_ADD_LOT, item.container, materialList)
            })
    }

    private fun showContainerEditDialog(
        oldContainerId: String,
        view: View
    ) {
        MaterialDialog(view.context).show {
            // message((R.string.edit_container_id))
            var containerId = ""
            input(
                waitForPositiveButton = false,
                hint = getString(R.string.enter_container_id)
            ) { dialog, text ->
                val inputField = dialog.getInputField()
                val isValid = text.isNotEmpty()
                containerId = text.toString()
                inputField.error = if (isValid) null else getString(R.string.enter_container_id)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.currentOldContainer = oldContainerId
                    vm.currentNewContainer = containerId
                    if (editContainerId.isNotEmpty()) editContainerId = containerId
                    vm.validateContainer(containerId)
                    if (this.isShowing) dismiss()
                    //vm.updateContainerId(oldContainerId, containerId)
                },
                { dismiss() })
        }
    }


    private fun showDeleteDialog(container: IndoContainerWithLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove_container))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.containerList.remove(container)
                    vm.removeConatinerWitfLots(container.container.containerNumber)
                    if (this.isShowing) dismiss()
                },
                { dismiss() })
        }
    }

    fun enableItems(flag: Boolean) {
        binding.spStage.isEnabled = flag
        binding.etEnterContainer.isEnabled = flag
        binding.btAdd.isEnabled = flag
        binding.clScan.isEnabled = flag
    }

    private fun updateSalesOrder(response: Resource<GenericReqAndResp<List<VegaIndoCoffeeExportSalesOrderModel>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.let { it1 ->
                        salesOrderList = it1
                        updageSalesOrderUI(salesOrderList)
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

    private fun updateSalesOrderOffline(it: List<IndoExporSalesMaterialList>?) {
        it?.let {
            salesOrderList = preparePurchaseOrder(it)
            updageSalesOrderUI(salesOrderList)
        }
    }

    private fun updageSalesOrderUI(soList: List<VegaIndoCoffeeExportSalesOrderModel>) {
        val saleOrder = ArrayList<VegaIndoCoffeeExportSalesOrderModel>()
        val stageInitItem = VegaIndoCoffeeExportSalesOrderModel()
        stageInitItem.salesOrderId = getString(R.string.select_ot)
        saleOrder.add(stageInitItem)
        saleOrder.addAll(soList)
        val soListData = saleOrder.map { data -> data.salesOrderId }
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_indo_coffee_export_grade, soListData)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spStage.adapter = stageAdapter
        binding.spStage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    val data = saleOrder[position]
                    vm.currentOT = data.salesOrderId
                    vm.currentSaleOrder = data.salesOrderList as MutableList<IndoExporSalesMaterialList>
                    vm.containerList = ArrayList()
                    //vm.getOTWithContainer(data.salesOrderId)
                    setUpMaterialAdapter(data.salesOrderList)
                }
            }
        }
        if (saleOrderTrans.saleOrderId?.isNotEmpty() == true) {
            var pos = 0
            soListData.forEachIndexed { index, s -> if (saleOrderTrans.saleOrderId.equals(s)) pos = index }
            binding.spStage.setSelection(pos)
            if (pos > 0) binding.spStage.isEnabled = false
        }
    }

    private fun setUpMaterialAdapter(selectedSales: List<IndoExporSalesMaterialList>) {
        materialList.clear()
        materialListItems.clear()
        materialListItems.addAll(selectedSales)
        materialList.addAll(selectedSales.map { it.materialNumber.toString() })
        binding.rvMaterialList.setUpAdapter(
            selectedSales as MutableList<IndoExporSalesMaterialList>,
            R.layout.item_indo_coffee_sales_material_list,
            ItemIndoCoffeeSalesMaterialListBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterial.text = it.materialDesc
                bindItem.tvSoWeight.text =
                    it.openQuantity?.toDouble()?.formatThreeDigits().plus(" ").plus(it.meins)
            })
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
                    validateLot(it)
                }
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

    fun getBack() {
        editContainerId = ""
        vm.getOTWithContainer(vm.currentTmpId)
    }

    fun editLotDetails(container: VegaCoffeeExportSalesContainer) {
        editContainerId = container.containerNumber
        vm.getOTWithContainer(container.tmpId.toString())
    }

}

