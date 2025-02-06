package com.olam.warehouse.vegax.offloadingcameroon.ui.mtnr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeReceivingMtnrWithLots
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.formatNDigits
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadingcameroon.R
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadingcameroon.data.domain.usecase.model.VegaCameroonOffloadingPostRequest
import com.olam.warehouse.vegax.offloadingcameroon.databinding.FragmentCameroonMtnrWsSummaryBinding
import com.olam.warehouse.vegax.offloadingcameroon.databinding.ItemCameroonMtnrWeighscaleLotCardLayoutBinding
import com.olam.warehouse.vegax.offloadingcameroon.databinding.OffloadingCameroonCustomEditTextBinding
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadingcameroon.ui.VegaCameroonOffloadingViewModel
import com.olam.warehouse.vegax.offloadingcameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList


class VegaCameroonMtnrWeighScaleSummaryFragment : BaseFragment() {
    override val layoutResourceId: Int = R.layout.fragment_cameroon_mtnr_ws_summary
    private lateinit var binding: FragmentCameroonMtnrWsSummaryBinding
    private var callBack: VegaCameroonOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private val vm: VegaCameroonOffloadingViewModel by viewModel()
    private var vegaCoffeeReceivingData = VegaCoffeeReceiving()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()


    companion object {
        fun newInstance(data: VegaCoffeeReceiving) =
            VegaCameroonMtnrWeighScaleSummaryFragment().putArgs {
                putParcelable("summaryData", data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCameroonOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentCameroonMtnrWsSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        binding.ivEdit.setOnClickListener { showRemarkDialog() }
        binding.btProceed.setOnClickListener { showConformationDialog() }
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
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("offloadingcameroon/ui/mtnr/VegaCameroonMtnrWeighScaleSummaryFragment")
            .title("Vega_Cameroon/Mtnr")
            .with(tracker)
    }

    private fun initExtra() {
        vegaCoffeeReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoffeeReceiving()
        binding.tvDestValue.text =
            vegaCoffeeReceivingData.supplierCode.plus(" - ")
                .plus(vegaCoffeeReceivingData.supplierName)
        binding.tvDriverNameValue.text =
            vegaCoffeeReceivingData.storageLocationCode.plus(" - ")
                .plus(vegaCoffeeReceivingData.storageLocationName)
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
            val bindingDia = OffloadingCameroonCustomEditTextBinding.inflate(layoutInflater)
            customView(R.layout.offloading_cameroon_custom_edit_text)
            val editValue = this.getCustomView().findViewById<EditText>(R.id.etValue)
            if (!vegaCoffeeReceivingData.remarks.isNullOrEmpty()) editValue.setText(
                vegaCoffeeReceivingData.remarks
            )
            editValue.hint = getString(com.olam.warehouse.presentation.R.string.enter_remark)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
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
        binding.rvLots.setUpAdapter(
            lots,
            R.layout.item_cameroon_mtnr_weighscale_lot_card_layout,
            ItemCameroonMtnrWeighscaleLotCardLayoutBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvScaleLotValue.text = item.batch
                bindItem.tvStLocationValue.text = item.storageLocationCode
                bindItem.tvScaleWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("KG")
                val dispatchWeight = item.weight?.toDouble()
                bindItem.tvScaleGradeValue.text = item.materialName
                var receivingKg = item.editedWeight
                val editedWeight = if (receivingKg.isNullOrEmpty()) item.weight?.toDouble()
                    ?.formatThreeDigits() else receivingKg.toString().toDouble().formatThreeDigits()

                bindItem.tvScaleDispatchValue.text = editedWeight
                bindItem.tvDispatchUOMValue.text = "KG"
                val receivingWeight = receivingKg?.toDouble()
                val weightLoss = receivingWeight?.let { dispatchWeight?.minus(it) }
                bindItem.tvTotalWeightLossValue.text =
                    weightLoss?.formatThreeDigits()?.plus(" ")?.plus("KG")
                bindItem.ivEdit.visible()
                bindItem.tvAddWeight.visibility = View.GONE
                bindItem.ivEdit.setOnClickListener { showLotEditDialog(item, it) }
            })
    }

    private fun showLotEditDialog(lot: VegaCoffeeReceiveLots, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.edit_lot))
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
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
                getString(R.string.proceed),
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
            VegaCameroonOffloadingPostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                operatorName = "",
                batchNumber = "",
                delFlag = "",
                deliveryDetails = prepareDeliveryList(),
                weighmentType = "",
                vehicleNumber = vegaCoffeeReceivingData.vehicleNumber,
                vehicleType = vegaCoffeeReceivingData.vehicleType,
                contactNumber = vegaCoffeeReceivingData.contactNumber,
                driverName = vegaCoffeeReceivingData.driverName
            )
        )
    }

    private fun prepareDeliveryList(): List<VegaCameroonOffloadingDeliveryDetail> {
        val list = ArrayList<VegaCameroonOffloadingDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in batchList) {
            val deliveryDetail = VegaCameroonOffloadingDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batch }
            deliveryDetail.batchNumber = item.batch
            deliveryDetail.materialCode = item.materialNumber
            deliveryDetail.remarks = vegaCoffeeReceivingData.remarks
            when(item.uom){
                "KG" ->
                    deliveryDetail.netWeight = (item.editedWeight)?.trim() ?: "0.0"
                "MT" ->
                    deliveryDetail.netWeight = convertKgToMT(item.editedWeight)?.trim() ?: "0.0"
            }
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
            deliveryDetail.unitsOfMeasure = item.uom
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
                bagTareWeight = bagTareWeight.plus(item1.tareWeight?.toDouble()?.times(item1.bagCount.toDouble())!!)
                item1.noOfPallet = if (item1.noOfPallet?.toInt() ?: 0 <= 0) "0" else item1.noOfPallet
            }
            when(item.uom){
                "KG" ->
                    deliveryDetail.grossWeight = (grossWeight.toString()).trim()
                "MT" ->
                    deliveryDetail.grossWeight = convertKgToMT(grossWeight.toString())?.trim()

            }
            deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
            if (bagItems.isNotEmpty()) {
                val bagSort = mutableListOf<VegaCoffeeOffloadingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                bagItems.forEach{
                    when (item.uom) {
                        "KG" -> {
                            it.grossWeight = (it.grossWeight)
                            it.netWeight = (it.netWeight)
                            var totalTareWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                            it.tareWeight = totalTareWeight?.formatNDigits(6)
                            it.unitsOfMeasure = "KG"
                            it.palletWeight = (it.palletWeight)!!
                            it.palletAverage = (it.palletAverage)!!
                        }
                        "MT" -> {
                            it.grossWeight = convertKgToMT(it.grossWeight)!!
                            it.netWeight = convertKgToMT(it.netWeight)!!
                            var totalTareWeight = it.tareWeight?.toDouble()?.times(it.bagCount.toDouble())
                            it.tareWeight = totalTareWeight?.div(1000)?.formatNDigits(6)
                            it.unitsOfMeasure = "MT"
                            it.palletWeight = convertKgToMT(it.palletWeight)!!
                            it.palletAverage = convertKgToMT(it.palletAverage)!!
                        }
                    }

                }
                when(item.uom){
                    "KG" ->{
                        deliveryDetail.huno = "KG"
                        deliveryDetail.huwt = (bagTareWeight.toString()).toDouble().formatThreeDigits().trim()
                        deliveryDetail.huno2 = "KG"
                    }
                    "MT" -> {
                        deliveryDetail.huno = "MT"
                        deliveryDetail.huwt = convertKgToMT(bagTareWeight.toString())?.toDouble()?.formatThreeDigits()?.trim()
                        deliveryDetail.huno2 = "MT"
                    }
                }

                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.bagList = bagItems
            }
            else{
                var listB = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
                var bag = VegaCoffeeOffloadingBagMaterial()
                bag.bagCount = "0"
                bag.bagMaterialCode = ""
                bag.bagType = ""
                bag.grossWeight = convertMtToKg(deliveryDetail.grossWeight!!).trim()
                bag.netWeight = convertMtToKg(deliveryDetail.netWeight!!).trim()
                bag.noOfPallet = "0"
                bag.tareWeight = "0"
                bag.unitsOfMeasure = deliveryDetail.unitsOfMeasure
                bag.palletWeight = "0"
                bag.palletAverage = "0"
                listB.add(bag)
                deliveryDetail.bagList = listB
            }

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
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.wb_id_is, data.wbId))
        intent.putExtra(
            AppUtils.PRINT_ENABLE,
            true
        )
        val tallyKeys = ArrayList<String>()
        tallyKeys.add(data.encodedImageContent ?: "")
        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        startActivity(intent)
        requireActivity().finish()
    }
}
