package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.model.VegaMtntResponse
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCoffeeReceiveLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeOffloadingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeReceiving
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingDeliveryDetail
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.data.domain.usecase.model.VegaGhanaOffloadingPostRequest
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.FragmentGhanaCocoaMtnrWsSummaryBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadReplaceFragmentCallback
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.VegaGhanaCocoaOffloadingViewModel
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.*
import kotlinx.android.synthetic.main.item_ghana_cocoa_mtnr_weighscale_reject_card_layout.view.*
import kotlinx.android.synthetic.main.offloading_ghana_cocoa_custom_edit_text.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

class VegaGhanaCocoaMtnrWeighScaleSummaryFragment : BaseFragment() {
    private var rejectList = HashMap<String, String>()
    private var acceptedBagCount = ""
    override val layoutResourceId: Int = R.layout.fragment_ghana_cocoa_mtnr_ws_summary
    private lateinit var binding: FragmentGhanaCocoaMtnrWsSummaryBinding
    private var callBack: VegaGhanaCocoaOffloadReplaceFragmentCallback? = null
    private var bagList = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
    private val vm: VegaGhanaCocoaOffloadingViewModel by viewModel()
    private var vegaCoffeeReceivingData = VegaCoffeeReceiving()
    private var batchList = mutableListOf<VegaCoffeeReceiveLots>()
    private var wbDetails = VegaQualityWBDetails()
    private var customLocationList = mutableListOf<VegaCustomStLocation>()
    private var uomDetails = ArrayList<VegaUomDetails>()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()

    companion object {
        fun newInstance(
            data: VegaCoffeeReceiving,
            wbDetails: VegaQualityWBDetails,
            rejectList: HashMap<String, String>,
            acceptedBagCount: String
        ) =
            VegaGhanaCocoaMtnrWeighScaleSummaryFragment()
                .putArgs {
                    putParcelable("summaryData", data)
                    putParcelable(WB_DATA, wbDetails)
                    putSerializable("rejectData", rejectList)
                    putString("acceptedBagCount", acceptedBagCount)
                }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaGhanaCocoaOffloadReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentGhanaCocoaMtnrWsSummaryBinding.inflate(inflater)
        initExtra()
        initUI()
        return binding.root
    }

    private fun initUI() {
        binding.tvTitle.text = getString(R.string.mtnr_weighbridge_summary)
        binding.btProceed.setOnClickListener { showConformationDialog() }


//        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })
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
        vegaCoffeeReceivingData = arguments?.getParcelable("summaryData") ?: VegaCoffeeReceiving()
        rejectList = arguments?.getSerializable("rejectData") as HashMap<String, String>
        acceptedBagCount = arguments?.getString("acceptedBagCount") as String

        vm.custonLocation.observe(viewLifecycleOwner, Observer {
            customLocationList = it.toMutableList()
            if (customLocationList.size > 0)
                binding.tvDriverNameValue.text =
                    customLocationList.single().procureLocationCode.plus("-")
                        .plus(customLocationList.single().procureLocationName)
        })

        vm.getCustomLocations()

        vm.uomDetail.observe(viewLifecycleOwner, Observer {
            uomDetails = it as ArrayList<VegaUomDetails>
        })
        vm.getUomDetails()

        vm.material.observe(viewLifecycleOwner, Observer {
            bagTypeList = it.toMutableList()
        })
        vm.getMaterials()

        binding.tvDestValue.text =
            vegaCoffeeReceivingData.plantId.plus("-")
                .plus(vegaCoffeeReceivingData.storageLocationCode)
        binding.tvStoNumberValue.text = vegaCoffeeReceivingData.delivery
        binding.tvTruckNoValue.text = wbDetails.vehicleNumber
        binding.tvAcceptedBagCount.text = acceptedBagCount
        var pendingRejectReasonBagCount = 0L
        rejectList.values.forEach {
            if (it.contains(":")) {
                pendingRejectReasonBagCount =
                    it.split(":")[1].toLong() + pendingRejectReasonBagCount
            }
        }
        binding.tvRejectedBagCount.text = pendingRejectReasonBagCount.toString()

        updateOBDDetails()
//        vm.offloadingMtnr.observe(viewLifecycleOwner, Observer { updateOBDDetails(it) })
//        vm.getOBDDetails(vegaCoffeeReceivingData.delivery)

        vm.offloadingPost.observe(viewLifecycleOwner, Observer { updateUI(it) })

    }

    private fun updateOBDDetails() {
        batchList.clear()
        setUpAdapter(rejectList)

    }

    private fun showRemarkDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_remark)
            message(R.string.remark)
            cancelOnTouchOutside(false)
            var remark = ""
            customView(R.layout.offloading_ghana_cocoa_custom_edit_text)
            val editValue = this.getCustomView().etValue
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
                        vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
                    } else {
                        editValue.error =
                            getString(com.olam.warehouse.presentation.R.string.enter_remark)
                    }
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(list: HashMap<String, String>) {
        val rejection = list.values.toMutableList()
        binding.rvLots.setUp(
            rejection,
            R.layout.item_ghana_cocoa_mtnr_weighscale_reject_card_layout,
            { item, pos ->
                tvReason.text = item.split(" - ")[0]
                tvReasonSloc.text = item.split(" - ", ":")[1]
                if (item.contains(":")) {
                    tvReasonCount.text = item.split(":")[1].plus(" Bags")
                } else {
                    tvReasonCount.text = "0".plus(" Bags")
                }
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
                    if (AppUtils.isOnline())
                        postDelivery()
                    else
                        moveToOfflineSuccessPage()
                },
                { dismiss() })
        }
    }

    private fun moveToOfflineSuccessPage() {
        vm.updateOBD(vegaCoffeeReceivingData.mtnCode!!, true)
        vegaCoffeeReceivingData.isOnlineData = false
        vegaCoffeeReceivingData.syncStatusMsg = ""
        vegaCoffeeReceivingData.isSynced = false
        vegaCoffeeReceivingData.tempWBId = "TMP_".plus(Random.nextLong().toString())
        vegaCoffeeReceivingData.weighBridgeId = vegaCoffeeReceivingData.tempWBId!!
        for (item in batchList) {
            vegaCoffeeReceivingData.ertim = item.materialNumber
            vegaCoffeeReceivingData.unitsOfMeasure = item.uom
        }
        batchList.forEach { item ->
            vegaCoffeeReceivingData.netWeight = item.editedWeight!!
            vegaCoffeeReceivingData.materialName = item.materialName
            vegaCoffeeReceivingData.grossWeight = item.weight
            var bagItems = bagList.filter { it.batchNumber == item.batch }
            vegaCoffeeReceivingData.bagCount = bagItems.sumBy { tar -> tar.bagCount.toInt() }.toString().trim()
        }
        vm.saveMtnrReceivingLots(vegaCoffeeReceivingData, VegaCoffeeReceiveLots())
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.success_offloading_offline))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.weigh_bridge_id_is).plus(" ").plus(vegaCoffeeReceivingData.tempWBId)
        )
        startActivity(intent)
        requireActivity().finish()
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
                weighmentType = "WS",
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
        for (item in rejectList) {
            val deliveryDetail = VegaGhanaOffloadingDeliveryDetail()
            deliveryDetail.batchNumber = generateRejectBatchId()
            deliveryDetail.materialCode = vegaCoffeeReceivingData.materialCode
            deliveryDetail.netWeight = item.value.split(":")[1]
            deliveryDetail.storageLocationCode =
                "R:".plus(item.value.split("-", ":")[1].replace(" ", ""))
            deliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
            deliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
            deliveryDetail.plantId = vegaCoffeeReceivingData.plantId
            deliveryDetail.delivery = vegaCoffeeReceivingData.delivery
            deliveryDetail.deliveryItem = wbDetails.deliveryItem
            deliveryDetail.unitsOfMeasure = "BAG"
            deliveryDetail.year = year.toString()
            deliveryDetail.grossWeight = item.value.split(":")[1]
            deliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            deliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            deliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime

            var listB = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
            var bag = VegaCoffeeOffloadingBagMaterial()
            bag.bagCount = item.value.split(":")[1]
            if (bagTypeList.size == 1) {
                bag.bagMaterialCode = bagTypeList[0].bagMaterialCode.toString().trim()
                bag.tareWeight = bagTypeList[0].tareWeight.toString().trim()
                bag.bagType = bagTypeList[0].bagType.toString().trim()
            }
            bag.batchNumber = wbDetails.batchNumber.toString()
            var totalweight: Double =
                (calculateWeight(vegaCoffeeReceivingData.materialCode.toString())).toDouble()
            bag.grossWeight = (totalweight * (deliveryDetail.grossWeight!!.toDouble())).toString()
            bag.netWeight = (totalweight * (deliveryDetail.netWeight!!.toDouble())).toString()
            bag.noOfPallet = "0"
            bag.unitsOfMeasure = "KG"
            bag.palletWeight = "0"
            bag.palletAverage = "0"
            listB.add(bag)
            deliveryDetail.bagList = listB
            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var startTime = ""
            var endTime = ""

            list.add(deliveryDetail)
        }
        if(acceptedBagCount.toLong() > 0) {
            val acceptDeliveryDetail = VegaGhanaOffloadingDeliveryDetail()
            acceptDeliveryDetail.batchNumber = generateAcceptBatchId()
            acceptDeliveryDetail.materialCode = vegaCoffeeReceivingData.materialCode
            acceptDeliveryDetail.netWeight = acceptedBagCount
            acceptDeliveryDetail.storageLocationCode =
                "A:".plus(customLocationList.single().procureLocationCode)
            acceptDeliveryDetail.purchaseDocNum = vegaCoffeeReceivingData.purchaseDocNum
            acceptDeliveryDetail.purchaseDocDesc = vegaCoffeeReceivingData.purchaseDocDesc
            acceptDeliveryDetail.plantId = vegaCoffeeReceivingData.plantId
            acceptDeliveryDetail.delivery = vegaCoffeeReceivingData.delivery
            acceptDeliveryDetail.deliveryItem = wbDetails.deliveryItem
            acceptDeliveryDetail.unitsOfMeasure = "BAG"
            acceptDeliveryDetail.year = year.toString()
            acceptDeliveryDetail.grossWeight = acceptedBagCount
            acceptDeliveryDetail.startTime = vegaCoffeeReceivingData.startTime
            acceptDeliveryDetail.endTime = vegaCoffeeReceivingData.endTime
            acceptDeliveryDetail.turnAroundTime = vegaCoffeeReceivingData.turnAroundTime
            var listB = arrayListOf<VegaCoffeeOffloadingBagMaterial>()
            var bag = VegaCoffeeOffloadingBagMaterial()
            bag.bagCount = acceptedBagCount
            if (bagTypeList.size == 1) {
                bag.bagMaterialCode = bagTypeList[0].bagMaterialCode.toString().trim()
                bag.tareWeight = bagTypeList[0].tareWeight.toString().trim()
                bag.bagType = bagTypeList[0].bagType.toString().trim()
            }
            bag.batchNumber = wbDetails.batchNumber.toString()
            var totalweight: Double =
                (calculateWeight(vegaCoffeeReceivingData.materialCode.toString())).toDouble()
            bag.grossWeight = (totalweight * (acceptedBagCount.toDouble())).toString()
            bag.netWeight = (totalweight * (acceptedBagCount.toDouble())).toString()
            bag.noOfPallet = "0"
            bag.unitsOfMeasure = "KG"
            bag.palletWeight = "0"
            bag.palletAverage = "0"
            listB.add(bag)
            acceptDeliveryDetail.bagList = listB
            list.add(acceptDeliveryDetail)
        }

        return list
    }

    private fun generateAcceptBatchId(): String? {
        var batchId = ""
        val yearFormat = SimpleDateFormat("yy") // Just the year, with 2 digits
        val monthFormat = SimpleDateFormat("MM") // Just the month, with 2 digits
        val dateFormat = SimpleDateFormat("dd") // Just the date, with 2 digits

        val formattedDate = dateFormat.format(Calendar.getInstance().time)
        val formattedMonth = monthFormat.format(Calendar.getInstance().time)
        val formattedYear = yearFormat.format(Calendar.getInstance().time)

        batchId = formattedYear.plus(formattedMonth).plus(formattedDate).plus(customLocationList.single().procureLocationCode)
        return batchId
    }

    private fun generateRejectBatchId(): String? {
        var batchId = ""
        val yearFormat = SimpleDateFormat("yy") // Just the year, with 2 digits
        val monthFormat = SimpleDateFormat("MM") // Just the month, with 2 digits
        val dateFormat = SimpleDateFormat("dd") // Just the date, with 2 digits

        val formattedDate = dateFormat.format(Calendar.getInstance().time)
        val formattedMonth = monthFormat.format(Calendar.getInstance().time)
        val formattedYear = yearFormat.format(Calendar.getInstance().time)

        batchId = formattedYear.plus(formattedMonth).plus(formattedDate).plus(vegaCoffeeReceivingData.storageLocationCode)
        return batchId
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
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.wb_id_is, data.wbId).plus("\n")
                .plus(getString(R.string.mtnr_number, data.mtnNumber))
        )
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

    private fun calculateWeight(materialCode: String): String {
        var netWeight = ""
        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode))).equals(materialCode) }).filter {
                it.fromUom.equals(BAG)
            }).single()
        netWeight =
            ((uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))).toString()
        return netWeight
    }

}
