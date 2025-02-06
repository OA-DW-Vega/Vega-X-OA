package com.olam.warehouse.vegax.salescocoa.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesDeliveryDetail
import com.olam.warehouse.vegax.salescocoa.data.domain.model.VegaCocoaSalesPostRequest
import com.olam.warehouse.vegax.salescocoa.databinding.FragmentCocoaSaleSummaryBinding
import com.olam.warehouse.vegax.salescocoa.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class VegaCocoaSalesSummaryFragment : BaseFragment(), ItemRemoveListener {

    private var dispatchLotsList = mutableListOf<VegaCocoaSalesLots>()
    override val layoutResourceId: Int = R.layout.fragment_cocoa_sale_summary
    private lateinit var binding: FragmentCocoaSaleSummaryBinding
    private var summaryObj: VegaCocoaSalesWB? = null
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private val vm: VegaCocoaSalesViewModel by viewModel()
    private var isRoundOff = false
    private var isSplit = false
    private var isBinFormation: Boolean = false

    companion object {
        fun newInstance(data: VegaCocoaSalesWB, isRounoff: Boolean) = VegaCocoaSalesSummaryFragment().putArgs {
            putParcelable(BUNDLE_MODEL, data)
            putBoolean(ROUND_OFF, isRounoff)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentCocoaSaleSummaryBinding.inflate(inflater)
        initExtra()
        initUi()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.deliverySuccessPost.observe(viewLifecycleOwner, Observer { updateAnticipatedUI(it) })
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("salescocoa/ui/VegaCocoaSalesSummaryFragment")
            .title("Sales Cocoa")
            .with(tracker)
    }

    private fun initExtra() {
        summaryObj = arguments?.getParcelable(BUNDLE_MODEL)
        isRoundOff = arguments?.getBoolean(ROUND_OFF) ?: false
        vm.getConfigItems(UserRoles.SALES.role)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
    }

    private fun initUi() {
        binding.tvTruckIDNo.visibility =
            if (SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) View.VISIBLE else View.GONE
        binding.tvTruckValue.visibility =
            if (SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) View.VISIBLE else View.GONE
        if (SALES_TYPE_WEIGHSCALE == summaryObj?.salesType) {
            binding.tvOperatorName.visible()
            binding.tvOperatorNameValue.visible()
            binding.tvOperatorNameValue.text = summaryObj?.operatorName
        }
        binding.tvTruckValue.text = summaryObj?.weighBridgeId
        binding.tvDestValue.text = summaryObj?.saleOrderId
        binding.tvTitle.text =
            getString(com.olam.warehouse.login.R.string.local_sales).plus(" - ")
                .plus(getTitle(summaryObj?.salesType ?: ""))
        if (summaryObj?.erdat != "") {
            val times = summaryObj?.erdat?.split('(', ')')
            binding.tvDateValue.text = times?.get(1).let { it1 ->
                it1?.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
        } else binding.tvDateValue.text = summaryObj?.ertim
        binding.tvStoNoValue.text = summaryObj?.soWeight
        binding.tvMaterialName.text = summaryObj?.materialName
        binding.tvStoWeightValue.text = summaryObj?.customerName
        binding.btProceed.setOnClickListener {
            if (isBinFormation && SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) {
                if (dispatchLotsList.size == 1) {
                    dispatchLotsList.get(0).let {
                        if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                            if (validate(binding.etBatchNo.text.toString())) showConformationDialog()
                            else showSnack(getString(R.string.batch_no_empty))
                        } else {
                            showConformationDialog()
                        }
                    }
                } else if (dispatchLotsList.size > 1) {
                    if (validate(binding.etBatchNo.text.toString())) showConformationDialog()
                    else showSnack(getString(R.string.batch_no_empty))
                } else {
                    showConformationDialog()
                }
            } else
                showConformationDialog()
        }
        val linearLayoutManager = activity?.let { LinearLayoutManager(it) }
        linearLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        binding.rvLots.layoutManager = linearLayoutManager
        dispatchLotsList = summaryObj?.lotList ?: ArrayList()
        vm.bagItems.observe(viewLifecycleOwner, Observer { getBagList(it) })
        vm.getBagItems("")

        if (isBinFormation && SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) {
            if (dispatchLotsList.size == 1) {
                dispatchLotsList.get(0).let {
                    if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                        binding.llBatchNo.visible()
                        isSplit = true
                    } else {
                        binding.llBatchNo.gone()
                        isSplit = false
                    }
                }
            } else if (dispatchLotsList.size > 1) {
                isSplit = false
                binding.llBatchNo.visible()
            } else {
                isSplit = false
                binding.llBatchNo.gone()
            }
        }
        setUpAdapter()
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun setUpAdapter() {
        val adapter =
            VegaCocoaSalesLotAdapter(
                dispatchLotsList as ArrayList<VegaCocoaSalesLots>,
                listener = this,
                isEdit = true,
                isScale = false, isWtPUOM = SALES_TYPE_WEIGHSCALE == summaryObj?.salesType, isRounoff = isRoundOff
            )
        binding.rvLots.adapter = adapter
    }

    private fun postDelivery() {
        var batchNumber = ""

        if(dispatchLotsList.size==1) {
            dispatchLotsList.get(0).let {
                if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                    batchNumber=binding.etBatchNo.text.toString().toUpperCase()
                }

            }
        }
        else if (dispatchLotsList.size > 1) {
            batchNumber = binding.etBatchNo.text.toString().toUpperCase()
        }
        else
        {

        }

        val request = VegaCocoaSalesPostRequest(
            key = getCurrentKey(),
            plant = getPlantDetails(),
            operatorName = summaryObj?.operatorName,
            batchNumber = batchNumber,
            delFlag = "",
                splitFlag = isSplit,
            deliveryDetails = prepareDeliveryList(),
            weighmentType = (if (summaryObj?.salesType.equals(
                    SALES_TYPE_WEIGHSCALE,
                    true
                )
            ) "WeighScale" else summaryObj?.salesType), data = emptyList()
        )
        if (SALES_TYPE_ANTICIPATED_VIRTUAL == summaryObj?.salesType) vm.postAnticipatedDeliveryDetail(request) else
            vm.postDeliveryDetail(request)
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                view.context.getString(R.string.proceed),
                view.context.getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun prepareSuccessMessage(data: List<VegaCocoaSalesDeliveryDetail>?): String {

        if (!data.isNullOrEmpty()) {
            var message = ""
            data.forEach {
                message =
                    "".plus(" " + it.weighBridgeId).plus(" ").plus(message.plus(it.msgList[0]).plus("\n"))
            }
            return message
        }
        return ""
    }

    private fun updateAnticipatedUI(response: Resource<GenericReqAndResp<List<VegaCocoaSalesDeliveryDetail>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    val docNo: List<String> = response.data?.data?.filter { !it.salesOrderNum.isNullOrEmpty() }
                        ?.map { it.salesOrderNum.toString() }
                        ?: listOf()
                    moveToSuccessFromAnticipated(
                        prepareSuccessMessage(response.data?.data),
                        docNo,
                        preparePhysicalInventoryId(response.data?.data)
                    )
                    vm.updateSuccessStatus(
                        summaryObj ?: VegaCocoaSalesWB(),
                        true,
                        4,
                        response.data?.message ?: "",
                        summaryObj?.lotList ?: ArrayList()
                    )
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                    val success = response.data?.data ?: ArrayList()
                    for (item in success) {
                        dispatchLotsList.single { it.batchNumber == item.batchNumber }.apply {
                            pickingFlag = item.pickingFlag
                            deliveryFlag = item.deliveryFlag
                            deliveryItem = item.deliveryItem
                            delivery = item.delivery
                            pgiFlag = item.pgiFlag
                            inventoryFlag = item.inventoryFlag
                        }
                    }
                    vm.lots.clear()
                    vm.dispatchWh = summaryObj!!
                    vm.lots.addAll(dispatchLotsList)
                    vm.saveWeighBridgeAndLotDetails()
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                moveToSuccessPage(response.data?.data)
                vm.updateSuccessStatus(
                    summaryObj ?: VegaCocoaSalesWB(),
                    true,
                    4,
                    response.data?.data?.msg ?: "",
                    summaryObj?.lotList ?: ArrayList()
                )
                vm.updateBagSuccessStatus(summaryObj?.lotList?.get(0)?.batchNumber ?: "", bagList)
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun moveToSuccessFromAnticipated(deliveryId: String, docNo: List<String>, materialDocNo: String) {
        val documentNo = if (docNo.isNotEmpty()) docNo.toString().replace("[", "").replace("]", "") else ""
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_dispatch))
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra("fromcoffee", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
        if (!documentNo.isEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.mtnr_ws_success, deliveryId).plus(
                    getString(
                        R.string.mtnr_ws_success_docu_no,
                        materialDocNo
                    )
                )
            )
        else
            intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.mtnr_ws_success, deliveryId))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun preparePhysicalInventoryId(data: List<VegaCocoaSalesDeliveryDetail>?): String {
        return if (data.isNullOrEmpty()) "" else data[0].documentNum ?: ""
    }

    private fun moveToSuccessPage(deliveryId: VegaDeliveryPostResponse?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_dispatch))
            if (summaryObj?.salesType == SALES_TYPE_WEIGHSCALE) {
                intent.putExtra(
                    AppUtils.PRINT_ENABLE,
                    (deliveryId?.encodedImageContent != null && deliveryId.encodedImageContent != "")
                )
                val tallyKeys = ArrayList<String>()
                tallyKeys.add(deliveryId?.encodedImageContent ?: "")
                intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            } else if (SALES_TYPE_ANTICIPATED_VIRTUAL == summaryObj?.salesType) {
                intent.putExtra(AppUtils.PRINT_ENABLE, true)
                intent.putExtra("fromcoffee", true)
                intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
            }
        } else intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_dispatch_offline)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.delivery_no).plus(deliveryId?.delivery))
        startActivity(intent)
        requireActivity().finish()
    }


    private fun prepareDeliveryList(): List<VegaCocoaSalesDeliveryDetail> {
        val list = ArrayList<VegaCocoaSalesDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCocoaSalesDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber }
            deliveryDetail.batchNumber = item.batchNumber
            deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = convertWeight(
                item.editedWeight?.trim() ?: "0.0",
                summaryObj?.unitsOfMeasure ?: "",
                if (summaryObj?.salesType == SALES_TYPE_WEIGHSCALE) item.weightToDispatchUOM
                    ?: "" else item.unitOfMeasure ?: ""
            )
            deliveryDetail.netWeight1 = item.editedWeight
            deliveryDetail.createdDate = summaryObj?.createdDate
            deliveryDetail.salesOrderNum = summaryObj?.saleOrderId
            deliveryDetail.customerId = summaryObj?.customerId
            deliveryDetail.customerName = summaryObj?.customerName
            deliveryDetail.thirdPartyMaterialCode = summaryObj?.thirdPartyMaterialCode ?: ""
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.salesItem = summaryObj?.salesItem
            deliveryDetail.toVendorCode = item.vendor
            deliveryDetail.deliveryItem = item.deliveryItem
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.delivery = item.delivery
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.pgiFlag = item.pgiFlag
            deliveryDetail.inventoryFlag = item.inventoryFlag
            deliveryDetail.toVendorCode = item.vendor
            deliveryDetail.weighBridgeId =
                if (SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) summaryObj?.weighBridgeId else ""
            deliveryDetail.unitsOfMeasure = summaryObj?.unitsOfMeasure
            deliveryDetail.year = year.toString()
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var startTime = ""
            var endTime = ""
            bagItems.forEach { item1 ->
                val palletAvg =
                    if (item1.noOfPallet?.toInt() != 0) item1.palletWeight?.toDouble()
                        ?.div(item1.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item1.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                    .plus(palletAvg!!)
                if (isRoundOff) {
//                    item1.tareWeight = item1.tareWeight?.toDouble()?.roundToInt().toString()
                    item1.netWeight = item.editedWeight.toString()
                }
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                if (item1.startTime?.isNotEmpty()!!) startTime = item1.startTime.toString()
                if (item1.endTime?.isNotEmpty()!!) endTime = item1.endTime.toString()
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet

            }
            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                if (isRoundOff)
                    deliveryDetail.huwt = bagTareWeight.roundToInt().toString().trim()
                else
                    deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.unitsOfMeasure1 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                if (isRoundOff)
                    deliveryDetail.grossWeight = grossWeight.roundToInt().toString().trim()
                else
                    deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.startTime = startTime
                deliveryDetail.endTime = endTime
                deliveryDetail.bagList = bagItems
            } /*else {
                deliveryDetail.huno2 = summaryObj?.unitsOfMeasure
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }*/
            list.add(deliveryDetail)
        }
        return list
    }


    override fun itemRemoved(item: VegaCocoaSalesLots) {
        activity?.onBackPressed()
    }

    override fun weightUpdated(item: VegaCocoaSalesLots) {
        //Nothing do here
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

    private fun getTitle(type: String): String {
        when {
            SALES_TYPE_WEIGHBRIDGE.equals(type, true) -> return getString(R.string.weighbridge)
            SALES_TYPE_WEIGHSCALE.equals(type, true) -> return getString(R.string.weighscale)
            SALES_TYPE_ANTICIPATED.equals(type, true) -> return getString(R.string.anticipated)
            SALES_TYPE_ANTICIPATED_VIRTUAL.equals(type, true) -> return getString(R.string.anticipated)
            else -> return ""
        }
    }

    private fun prepareLotCard(): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        dispatchLotsList.forEach {
            val lot = VegaCoffeeSalesLots()
            lot.batchNumber = it.batchNumber
            lot.editedWeight = it.editedWeight
            lot.unitOfMeasure = it.unitOfMeasure
            lot.materialName = it.materialName
            lot.materialCode = it.materialCode ?: ""
            lot.storageLocationCode = it.storageLocationCode
            lotList.add(lot)
        }
        return lotList
    }

    private fun validate(batchNo: String): Boolean {
        return !batchNo.isEmpty()
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        configItems?.forEach {
            when (it.process) {
                ConfigItems.LOT_SELECTION_MULTI_WEIGHBRIDGE.item -> {
                    when {
                        it.applicable?.contains("Y")!! -> {
                            isBinFormation = true

                            if (dispatchLotsList.size == 1 && SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) {
                                dispatchLotsList.get(0).let {
                                    if (it.weight!!.toDouble() > it.editedWeight!!.toDouble()) {
                                        binding.llBatchNo.visible()
                                        isSplit = true
                                    } else {
                                        binding.llBatchNo.gone()
                                        isSplit = false
                                    }
                                }
                            } else if (dispatchLotsList.size > 1 && SALES_TYPE_WEIGHBRIDGE == summaryObj?.salesType) {
                                isSplit = false
                                binding.llBatchNo.visible()
                            } else {
                                isSplit = false
                                binding.llBatchNo.gone()
                            }
                        }
                        it.applicable?.contains("N")!! -> isBinFormation = false
                    }
                }

            }
        }
    }

}
