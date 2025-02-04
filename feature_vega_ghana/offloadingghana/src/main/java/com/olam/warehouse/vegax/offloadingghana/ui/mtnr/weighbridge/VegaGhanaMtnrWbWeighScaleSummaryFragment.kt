package com.olam.warehouse.vegax.offloadingghana.ui.mtnr.weighbridge

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.master.work.convertMtToKg
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatNDigits
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.offloadingghana.R
import com.olam.warehouse.vegax.offloadingghana.data.domain.usecase.model.VegaGhanaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingghana.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingghana.databinding.FragmentGhanaMtnrWsSummaryBinding
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingghana.ui.VegaGhanaOffloadingViewModel
import com.olam.warehouse.vegax.offloadingghana.utils.*
import kotlinx.android.synthetic.main.item_ghana_mtnr_weighscale_lot_card_layout.view.*
import kotlinx.android.synthetic.main.offloading_ghana_custom_edit_text.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class VegaGhanaMtnrWbWeighScaleSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_ghana_mtnr_ws_summary
    private lateinit var binding: FragmentGhanaMtnrWsSummaryBinding
    private var callBack: VegaGhanaOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private val vm: VegaGhanaOffloadingViewModel by viewModel()
    private var vegaCoffeeReceivingData = VegaCoffeeReceiving()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var wbDetails = VegaQualityWBDetails()


    companion object {
        fun newInstance(data: VegaCoffeeReceiving, wbDetails: VegaQualityWBDetails) =
            VegaGhanaMtnrWbWeighScaleSummaryFragment().putArgs {
                putParcelable("summaryData", data)
                putParcelable(WB_DATA, wbDetails)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaMtnrWsSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initUI() {
        println("Roshna  gross=> ${wbDetails.grossWeight} ")
        println("Roshna  net=> ${wbDetails.netWeight} ")
        binding.tvTitle.text = "MTNR WeighBridge"
        binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener {
//            activity?.finish()
            showConformationDialog()
        }
        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaMtntResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    response.data?.data?.let { moveToSuccessPage(it) }
                    vm.updateStatus(vegaCoffeeReceivingData.delivery.toString())
                } else {
                    UIUtils.showErrorDialog(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }

    }

    private fun initExtra() {
        wbDetails = arguments?.getParcelable(WB_DATA)!!
        binding.btProceed.text = "OK"
        binding.tvRemark.isVisible = false
        binding.cvRemark.isVisible = false
        vegaCoffeeReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoffeeReceiving()
        binding.tvDestValue.text =
            vegaCoffeeReceivingData.supplierCode.plus(" - ").plus(vegaCoffeeReceivingData.supplierName)
        binding.tvDriverNameValue.text =
            vegaCoffeeReceivingData.storageLocationCode.plus(" - ").plus(vegaCoffeeReceivingData.storageLocationName)
        binding.tvStoNumberValue.text = vegaCoffeeReceivingData.delivery
        binding.tvTruckNoValue.text = vegaCoffeeReceivingData.vehicleNumber
        binding.tvRemarkValue.text = vegaCoffeeReceivingData.remarks

        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
        vm.getOBDDetails(vegaCoffeeReceivingData.delivery)
    }

    private fun updateOBDDetails(data: VegaCoffeeReceivingMtnrWithLots?) {
        batchList.clear()
        if (data != null) {
            if (data.lineItems.size > 0) {
                data.lineItems.forEach {
                    /*var edWeight = 0.0
                    val bags = data.lineItems.filter { it1 -> it1.lots.batch.equals(it.batch) }
                    if (bags.size == 1) {
                        if (vm.vegaCoffeeReceivingData.startTime?.isEmpty() == true) vm.vegaCoffeeReceivingData.startTime =
                            DateUtils.getCurrentTimeInMills().toString()
                    }
                    if (bags.size > 0) edWeight = bags[0].bagItem.sumByDouble { it2 -> it2.netWeight.toDouble() }
                    it.editedWeight = edWeight.toString()*/
                    batchList.add(it.lots)
                    bagList.addAll(it.bagItem.filter { it2 -> it2.mtnNumber.equals(it.lots.mtnNumber) }.filter { it1 ->
                        it1.batchNumber.equals(
                            it.lots.batch
                        )
                    })
                }
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            } else {
                setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
            }
        } else {
            setUpAdapter(batchList as ArrayList<VegaCoffeeReceiveLots>)
        }
    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.offloading_ghana_custom_edit_text)
            val editValue = this.getCustomView().etValue
            if (!vegaCoffeeReceivingData.remarks.isNullOrEmpty()) editValue.setText(
                vegaCoffeeReceivingData.remarks
            )
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    if (!editValue.text.isNullOrEmpty()) {
                        vegaCoffeeReceivingData.remarks = editValue.text.toString()
                        binding.tvRemarkValue.text = editValue.text.toString()
                        vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(list: List<VegaCoffeeReceiveLots>) {
        val lots = list as MutableList
        binding.rvLots.setUp(lots, R.layout.item_ghana_mtnr_weighscale_lot_card_layout, { item, pos ->
            tvScaleLotValue.text = item.batch
            tvStLocationValue.text = item.storageLocationCode
            tvScaleWeightValue.text =
                item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("MT")
            val dispatchWeight = item.weight?.toDouble()
            tvScaleGradeValue.text = item.materialName
            var receivingKg = item.editedWeight
            val editedWeight =
                if (receivingKg.isNullOrEmpty()) "0.0" else receivingKg.toString().toDouble().formatThreeDigits()
            tvScaleDispatchValue.text = editedWeight
            tvDispatchUOMValue.text = "MT"
            val receivingWeight = receivingKg?.toDouble()
            val weightLoss = receivingWeight?.let { dispatchWeight?.minus(it) }
            if (dispatchWeight!! < receivingWeight!!) {
                tvTotalWeightLoss.text = "Weight Gain"
            }
            tvTotalWeightLossValue.text = abs(weightLoss!!).formatThreeDigits().plus(" ").plus("MT")
            ivEdit.visibility = View.GONE
            tv_add_weight.visibility = View.GONE
            ivEdit.setOnClickListener { showLotEditDialog(item, it) }
        })
    }

    private fun showLotEditDialog(lot: VegaCoffeeReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    callBack?.replaceFragment(EDIT_LOT, lot)
                },
                { dismiss() })
        }
    }

    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun postDelivery() {
        vm.postOffloadingDetail(
            VegaGhanaOffloadingPostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                operatorName = "",
                batchNumber = "",
                delFlag = "",
                deliveryDetails = prepareDeliveryList(),
                weighmentType = "WB",
                vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
                vehicleType = vegaCoffeeReceivingData.vehicleType,
                contactNumber = vegaCoffeeReceivingData.contactNumber,
                driverName = vegaCoffeeReceivingData.driverName
            )
        )
    }

    private fun prepareDeliveryList(): List<VegaGhanaOffloadingDeliveryDetail> {
        val list = ArrayList<VegaGhanaOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaGhanaOffloadingDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batch }
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.remarks = vegaCoffeeReceivingData.remarks
//            deliveryDetail.netWeight = convertMtToKg(item.editedWeight!!).trim()
            deliveryDetail.netWeight = convertMtToKg(wbDetails.netWeight!!).trim()
            deliveryDetail.recStorageLocationCode = vegaCoffeeReceivingData.storageLocationCode
            deliveryDetail.storageLocationCode = vegaCoffeeReceivingData.supplierCode
            deliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
            deliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
            deliveryDetail.plantId = vegaCoffeeReceivingData.plantId
            deliveryDetail.recPlantId = vegaCoffeeReceivingData.materialCode
            deliveryDetail.delivery = item.delivery
            deliveryDetail.deliveryItem = item.posnr
            deliveryDetail.deliveryFlag = item.deliveryFlag
            deliveryDetail.pickingFlag = item.pickingFlag
            deliveryDetail.unitsOfMeasure = "KG"
            deliveryDetail.year = year.toString()
            deliveryDetail.huno = "KG"
            deliveryDetail.huno2 = "KG"
            deliveryDetail.weighBridgeId = wbDetails.weighBridgeId
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
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            deliveryDetail.grossWeight = convertMtToKg(wbDetails.grossWeight!!).trim()
//            deliveryDetail.grossWeight = convertMtToKg(item.editedWeight!!).trim()
            deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
            var listB = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
            var bag = VegaCoffeeOffloadingBagMaterial()
            bag.bagCount = "0"
            bag.bagMaterialCode = ""
            bag.bagType = ""
            bag.grossWeight = convertMtToKg(wbDetails.grossWeight!!).trim()
            bag.netWeight = convertMtToKg(wbDetails.netWeight!!).trim()
            bag.noOfPallet = "0"
            bag.tareWeight = "0"
            bag.unitsOfMeasure = "KG"
            bag.palletWeight = "0"
            bag.palletAverage = "0"
            listB.add(bag)
            deliveryDetail.bagList = listB
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                bagItems.forEach {
                    it.grossWeight = it.grossWeight
                    it.netWeight = it.netWeight
                    var totalTareWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                    it.tareWeight = totalTareWeight?.div(1000)?.formatNDigits(6)
                    it.unitsOfMeasure = "KG"
                    it.palletWeight = it.palletWeight!!
                    it.palletAverage = it.palletAverage!!
                }
                deliveryDetail.huno = "KG"
                deliveryDetail.huwt = bagTareWeight.toString().toDouble().formatThreeDigits().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
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

    private fun convertWeight(weight: String, soUom: String, lotUom: String): String? {
        if (soUom == lotUom) {
            return weight
        } else {
            if (soUom == UNIT_MT && lotUom == UNIT_KG) {
                return convertKgToMT(weight)
            } else if (soUom == UNIT_KG && lotUom == UNIT_MT) return convertMtToKg(weight)
        }
        return "0"
    }

    private fun moveToSuccessPage(data: VegaMtntResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(UIUtils.FROM_GHANA, true)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.wb_id_is, wbDetails.weighBridgeId))
        intent.putExtra("Weighbridgetype", "MTNR")
//        intent.putExtra(
//            AppUtils.PRINT_ENABLE,
//            true
//        )
//        val tallyKeys = ArrayList<String>()
//        tallyKeys.add(data.encodedImageContent ?: "")
//        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        startActivity(intent)
        requireActivity().finish()
    }
}
