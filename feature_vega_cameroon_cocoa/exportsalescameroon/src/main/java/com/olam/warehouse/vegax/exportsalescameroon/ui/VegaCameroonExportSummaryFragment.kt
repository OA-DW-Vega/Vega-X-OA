package com.olam.warehouse.vegax.exportsalescameroon.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesContainer
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeExportSalesLots
import com.olam.warehouse.master.vegacoffee.model.ContainerWithLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeExportOTWithContainer
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalescameroon.R
import com.olam.warehouse.vegax.exportsalescameroon.data.domain.model.*
import com.olam.warehouse.vegax.exportsalescameroon.databinding.FragmentExportCameroonSummaryBinding
import com.olam.warehouse.vegax.exportsalescameroon.databinding.ItemExportCameroonSummaryBinding
import com.olam.warehouse.vegax.exportsalescameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCameroonExportSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_export_cameroon_summary
    private val vm: VegaCameroonExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentExportCameroonSummaryBinding
    private var salesOderId = ""
    private var materialUOM: String = ""
    private var selectedStorageLocation: String = ""
    private var materialList = ArrayList<materialList>()
    private var textdetailList = ArrayList<VegaCameroonExportSalesTextDetail>()
    private var mergedBatchNumbers: List<String?> = ArrayList<String>()
    private var assignLotList = ArrayList<VegaCameroonExportSalesAssignLot>()
    private var isEmptyMergedId: Boolean = false


    companion object {
        fun newInstance(
            salesOrderId: String,
            materialList: ArrayList<materialList>,
            textdetailList: ArrayList<VegaCameroonExportSalesTextDetail>
        ) = VegaCameroonExportSummaryFragment().putArgs {
            putString(SALES_ID, salesOrderId)
            putParcelableArrayList(MATERIAL_LIST, materialList)
            putParcelableArrayList(TEXTDETAIL_LIST, textdetailList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun editLotDetails(container: VegaCoffeeExportSalesContainer)
        fun navigateToAssignLot(
            containerList: ArrayList<ContainerWithLots>,
            materialList: ArrayList<materialList>
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescameroon/ui/VegaCameroonExportSummaryFragment").title("Vega_Cameroon/Export Sales")
            .with(tracker)
        initExtra()
        initUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentExportCameroonSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun initExtra() {
        salesOderId = arguments?.getString(SALES_ID) ?: ""
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST)
            ?: ArrayList<materialList>()
        textdetailList =
            arguments?.getParcelableArrayList<VegaCameroonExportSalesTextDetail>(TEXTDETAIL_LIST)
                ?: ArrayList<VegaCameroonExportSalesTextDetail>()
        if (materialList.size > 0) materialUOM = materialList[0].meins.toString()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        vm.otContainer.observe(viewLifecycleOwner, Observer { updateContainer(it) })
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (salesOderId.isNotEmpty()) vm.getOTWithContainer(salesOderId)
        binding.btProceed.setOnClickListener { validateProceed() }
        binding.btAssignLot.setOnClickListener { moveToAssignLot() }
    }

    private fun validateProceed() {
        when {
            mergedBatchNumbers.isNullOrEmpty() -> showSnack(getString(R.string.msg_assign_before_confirm))
            else -> showConformationDialog()
        }
    }

    private fun moveToAssignLot() {
        callback?.navigateToAssignLot(vm.containerList,materialList)
    }

    private fun updateContainer(containerList: VegaCoffeeExportOTWithContainer?) {
        if (containerList != null)
            updateContainerAdapter(containerList)
        else
            updateContainerAdapter(VegaCoffeeExportOTWithContainer())
    }

    private fun updateContainerAdapter(data: VegaCoffeeExportOTWithContainer) {
        val containerList = mutableListOf<ContainerWithLots>()
        if (data.lineItems.size > 0) {
            containerList.addAll(data.lineItems)
            binding.rvLots.visible()
            vm.containerList = data.lineItems as ArrayList<ContainerWithLots>
            updateHeaderValue()
        } else {
            binding.rvLots.gone()
        }
        binding.rvLots.setUpAdapter(
            containerList,
            R.layout.item_export_cameroon_summary,
            ItemExportCameroonSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvContainerId.text = it.container.containerNumber
                val lotCount = it.lots.size
                val materialList = it.lots.map { it.materialName.toString() }.distinct()
                val totalWeight = it.lots.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }
                bindItem.tvLotsCount.text = if (lotCount != 0) lotCount.toString() else "-"
                bindItem.tvGradeValue.text =
                    if (materialList.size > 0) materialList.toString().replace("[", "")
                        .replace("]", "").replace(
                            ",",
                            "\n"
                        ) else "-"
                bindItem.tvWeightValue.text = totalWeight.toString().plus(" ").plus("KG")
                bindItem.ivSelect.setOnClickListener { view ->
                    showLotEditDialog(
                        it.container,
                        view
                    )
                }
            })
    }

    private fun showLotEditDialog(
        container: VegaCoffeeExportSalesContainer,
        view: View
    ) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    //activity?.onBackPressed()
                    callback?.editLotDetails(container)
                },
                { dismiss() })
        }
    }

    private fun updateHeaderValue() {
        var totalWeight = 0.0
        var totalLots = 0
        vm.containerList.forEach {
            val lotCount = it.lots.size
            it.lots.forEach { item ->
                val totallotsWeight = convertWeight(
                    item.editedWeight?.trim() ?: "0.0",
                    materialUOM, item.unitOfMeasure ?: ""
                )
                totalWeight = it.lots.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }
            }
            totalLots = totalLots.plus(lotCount)
        }
        binding.tvStoNo.text = salesOderId
        binding.tvTotalWeight.text = totalWeight.formatThreeDigits().plus(" ").plus("KG")
        binding.totaldeliveryqtyvalue.text = totalWeight.formatThreeDigits().plus(" ").plus("KG")
        binding.tvTotalLots.text = totalLots.toString()

        var mergedids = ArrayList<String>()
        vm.containerList.forEach {
            it.lots.forEach {
                if(it.mergedLotId == "")
                    isEmptyMergedId = true
                else
                    mergedids.add(it.mergedLotId.toString())
            }
        }
        if(!isEmptyMergedId) {
            binding.tvAssignLot.text =
                "LOT IDs: ".plus(mergedids.distinct().toString().replace("[", "").replace("]", ""))
        }
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        vm.postDeliveryDetail(
            VegaCameroonExportSalesPostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                operatorName = "",
                batchNumber = "",
                delFlag = "",
                deliveryDetails = prepareDeliveryList(),
                textNavListValues = textdetailList,
                weighmentType = ""
            )
        )
    }

    private fun prepareDeliveryList(): List<VegaCameroonExportSalesDeliveryDetail> {
        val list = ArrayList<VegaCameroonExportSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        vm.containerList.forEach { con ->
            con.lots.forEach { item ->
                val deliveryDetail = VegaCameroonExportSalesDeliveryDetail()
                deliveryDetail.batchNumber = item.batchNumber
                deliveryDetail.materialCode = item.materialCode
                deliveryDetail.mergedBatchNumber = item.mergedLotId
                deliveryDetail.recStorageLocationCode =
                    item.receivingStorageLocation // Added for Storage Location field
                deliveryDetail.plantId = item.plantId
                deliveryDetail.recPlantId = item.plantId
                deliveryDetail.storageLossFlag = item.isChecked

                if (getCurrentKey().split("_")[1].contains("NI")) {
                    deliveryDetail.netWeight = convertKgToLB1(item.editedWeight.toString())
                } else {
                    deliveryDetail.netWeight = convertKgToMT(item.editedWeight.toString())
                }

                deliveryDetail.salesOrderNum = salesOderId
                deliveryDetail.storageLocationCode = item.storageLocationCode
                val salesItem = materialList.filter { it.salesOrderId.equals(salesOderId) }
                    .filter { it.materialNumber.equals(item.materialCode) }
                if (salesItem.size > 0) deliveryDetail.salesItem = salesItem[0].salesItemNum
                deliveryDetail.weighBridgeId = item.weighBridgeId
                deliveryDetail.delivery = item.delivery
                deliveryDetail.deliveryItem = item.deliveryItem
                deliveryDetail.deliveryFlag = item.deliveryFlag
                deliveryDetail.pickingFlag = item.pickingFlag
                deliveryDetail.pgiFlag = item.pgiFlag
                deliveryDetail.containerFlag = item.containerFlag
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    deliveryDetail.unitsOfMeasure = "KG"
                } else {
                    deliveryDetail.unitsOfMeasure = materialUOM
                }
                deliveryDetail.year = year.toString()
                deliveryDetail.grossWeight = deliveryDetail.netWeight
                deliveryDetail.grossWeight = item.weight
                deliveryDetail.containerNum = con.container.containerNumber
                deliveryDetail.toVendorCode = item.vendor
                list.add(deliveryDetail)
            }
        }
        return list
    }

    private fun convertWeight(weight: String, soUom: String, lotUom: String): String {
        if (soUom == lotUom) {
            return weight
        } else {
            if (soUom == UNIT_MT && lotUom == UNIT_KG) {
                return convertKgToMT(weight)
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight)
        }
        return "0"
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCameroonExportSalesPostRequest>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                val data = response.data?.data?.deliveryDetails
                val postedLot = arrayListOf<VegaCoffeeExportSalesLots>()
                data?.forEach {
                    val lot = VegaCoffeeExportSalesLots()
                    lot.saleOrderId = salesOderId
                    lot.batchNumber = it.batchNumber.toString()
                    lot.delivery = it.delivery.toString()
                    lot.deliveryItem = it.deliveryItem.toString()
                    lot.weighBridgeId = it.weighBridgeId.toString()
                    lot.deliveryFlag = it.deliveryFlag
                    lot.pickingFlag = it.pickingFlag
                    lot.mergedLotId = it.mergedBatchNumber
                    lot.receivingStorageLocation = it.recStorageLocationCode
                    lot.pgiFlag = it.pgiFlag
                    lot.containerFlag = it.containerFlag
                    postedLot.add(lot)
                }
                vm.updateLotWeightInfo(postedLot)
                if (response.data?.success!!) {
                    moveToSuccessPage(data)
                } else {
                    response.data?.message?.let { showErrorDialogWithFAQLink(requireContext(), it) }
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun moveToSuccessPage(
        data: List<VegaCameroonExportSalesDeliveryDetail>?
    ) {
        val deveryIds = arrayListOf<String>()
        val batchIds = arrayListOf<String>()
        var customMessage = ""
        data?.forEach {
            batchIds.add(it.mergedBatchNumber.toString())
            deveryIds.add(it.delivery.toString())

        }
        customMessage = "OBD Number ".plus(deveryIds.distinct().toString().replace("[", "").replace("]", "")).plus(" created successfully for batch numbers ").plus(batchIds.distinct().toString().replace("[", "").replace("]", ""))
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_dispatch))
        intent.putExtra(AppUtils.SUB_TITLE, customMessage)
        startActivity(intent)
        requireActivity().finish()
    }

    fun updateAssignLotText(
        mergedLotIds: List<String?>,
        list: ArrayList<VegaCameroonExportSalesAssignLot>,
        storageLocation: String
    ) {
        mergedBatchNumbers = mergedLotIds
        selectedStorageLocation = storageLocation
        assignLotList = list
        binding.tvAssignLot.text =
            "LOT IDs: ".plus(mergedLotIds.toString().replace("[", "").replace("]", ""))
        list.forEach { mergedLot ->
            vm.containerList.forEach { item ->
                item.lots.forEach {
                    if (it.containerNumber == mergedLot.containerNumber && it.materialName == mergedLot.materialName) {
                        it.mergedLotId = mergedLot.mergedLotId
                        it.receivingStorageLocation =
                            storageLocation // Added for Storage Location field
                    }
                }
                vm.saveLotDetails(item.lots as ArrayList<VegaCoffeeExportSalesLots>)
            }

        }
    }

}
