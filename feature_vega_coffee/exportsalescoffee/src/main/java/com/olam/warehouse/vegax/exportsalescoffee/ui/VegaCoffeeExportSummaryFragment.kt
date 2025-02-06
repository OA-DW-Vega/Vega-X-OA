package com.olam.warehouse.vegax.exportsalescoffee.ui

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
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.exportsalescoffee.R
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.VegaCoffeeExportSalesDeliveryDetail
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.VegaCoffeeExportSalesPostRequest
import com.olam.warehouse.vegax.exportsalescoffee.data.domain.model.materialList
import com.olam.warehouse.vegax.exportsalescoffee.databinding.FragmentExportCoffeeSummaryBinding
import com.olam.warehouse.vegax.exportsalescoffee.databinding.ItemExportCoffeeSummaryBinding
import com.olam.warehouse.vegax.exportsalescoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 9/1/2020.
 */
class VegaCoffeeExportSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_export_coffee_summary
    private val vm: VegaCoffeeExportSalesViewModel by viewModel()
    private var callback: CallBack? = null
    private lateinit var binding: FragmentExportCoffeeSummaryBinding
    private var salesOderId = ""
    private var materialUOM: String = ""
    private var materialList = ArrayList<materialList>()

    companion object {
        fun newInstance(
            salesOrderId: String,
            materialList: ArrayList<materialList>
        ) = VegaCoffeeExportSummaryFragment().putArgs {
            putString(SALES_ID, salesOrderId)
            putParcelableArrayList(MATERIAL_LIST, materialList)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? CallBack
    }

    interface CallBack {
        fun editLotDetails(container: VegaCoffeeExportSalesContainer)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("exportsalescoffee/ui/VegaCoffeeExportSummaryFragment").title("IVC/Coffee/Export Sales/ExportSummary")
            .with(tracker)
        initExtra()
        initUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentExportCoffeeSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun initExtra() {
        salesOderId = arguments?.getString(SALES_ID) ?: ""
        materialList = arguments?.getParcelableArrayList<materialList>(MATERIAL_LIST) ?: ArrayList<materialList>()
        if (materialList.size > 0) materialUOM = materialList[0].meins.toString()
    }

    private fun initUI() {
        vm.otContainer.observe(viewLifecycleOwner, Observer { updateContainer(it) })
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        if (salesOderId.isNotEmpty()) vm.getOTWithContainer(salesOderId)
        binding.btProceed.setOnClickListener { showConformationDialog() }
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
            R.layout.item_export_coffee_summary,
            ItemExportCoffeeSummaryBinding::inflate,
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
                bindItem.tvWeightValue.text = totalWeight.toString().plus(" ").plus(uom)
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
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
            //val totallotsWeight = it.lots.sumByDouble { it.editedWeight?.toDouble() ?: 0.0 }
            it.lots.forEach { item ->
                val totallotsWeight = convertWeight(
                    item.editedWeight?.trim() ?: "0.0",
                    materialUOM, item.unitOfMeasure ?: ""
                )
                totalWeight = totalWeight.plus(totallotsWeight.toDouble())
            }
            totalLots = totalLots.plus(lotCount)
        }
        binding.tvStoNo.text = salesOderId
        binding.tvTotalWeight.text = totalWeight.formatThreeDigits().plus(" ").plus(materialUOM)
        binding.tvTotalLots.text = totalLots.toString()
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_dispatch)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    private fun postDelivery() {
        vm.postDeliveryDetail(
            VegaCoffeeExportSalesPostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                operatorName = "",
                batchNumber = "",
                delFlag = "",
                deliveryDetails = prepareDeliveryList(),
                weighmentType = ""
            )
        )
    }

    private fun prepareDeliveryList(): List<VegaCoffeeExportSalesDeliveryDetail> {
        val list = ArrayList<VegaCoffeeExportSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        vm.containerList.forEach { con ->
            con.lots.forEach { item ->
                val deliveryDetail = VegaCoffeeExportSalesDeliveryDetail()
                deliveryDetail.batchNumber = item.batchNumber
                deliveryDetail.materialCode = item.materialCode
                deliveryDetail.plantId = item.plantId
                deliveryDetail.netWeight = convertWeight(
                    item.editedWeight?.trim() ?: "0.0",
                    materialUOM, item.unitOfMeasure ?: ""
                )
                deliveryDetail.salesOrderNum = salesOderId
                deliveryDetail.recStorageLocationCode = item.storageLocationCode
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
                deliveryDetail.unitsOfMeasure = materialUOM
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

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoffeeExportSalesPostRequest>>) {
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

    private fun moveToSuccessPage(data: List<VegaCoffeeExportSalesDeliveryDetail>?) {
        val deveryIds = arrayListOf<String>()
        data?.forEach {
            it.msgList?.toList()?.let { it1 -> deveryIds.addAll(it1) }
        }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_dispatch))
        intent.putExtra(AppUtils.SUB_TITLE, deveryIds.toString().replace("[", "").replace("]", "").replace(",", "\n"))
        startActivity(intent)
        requireActivity().finish()
    }
}
