package com.olam.warehouse.vegax.thirdpartysalescoffee.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaDeliveryPostResponse
import com.olam.warehouse.master.common.model.VegaReceivingPost
import com.olam.warehouse.master.common.model.VegaReceivingResponse
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSweepingBagMaterial
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeThirdPartyRequestModel
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeThirdPartyModelWithLots
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaGrnPriceDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.thirdpartysalescoffee.R
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryDetail
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.data.domain.model.VegaNicaraguaCoffeeTPDeliveryPost
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.FragmentThirdPartyOwnershipSummaryBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.databinding.ItemThirdPartyAddLotBinding
import com.olam.warehouse.vegax.thirdpartysalescoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaCoffeeThirdPartyOwnershipSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_third_party_ownership_summary
    private lateinit var binding: FragmentThirdPartyOwnershipSummaryBinding
    private var callBack: VegaCoffeeThirdPartyReplaceFragmentCallback? = null
    private val vm: VegaCoffeeThirdPartyViewModel by viewModel()
    private lateinit var model: VegaCoffeeThirdPartyRequestModel
    private lateinit var vegaCoffeeDeliveryPost: VegaCoffeeTPDeliveryPost
    private var dispatchLotsList = mutableListOf<VegaCocoaDispatchLots>()
    private lateinit var vegaCoffeeThirdPartyModelWithLots: VegaCoffeeThirdPartyModelWithLots
    private var bagList = arrayListOf<VegaCocoaSweepingBagMaterial>()
    private var thirdPartyMaterials: List<VegaMaterial> = mutableListOf()
    private var weighBridgeModel: VegaCocoaDispatchWB? = null
    private var delivery = ""
    private lateinit var vegaNicCoffeeDeliveryPost: VegaNicaraguaCoffeeTPDeliveryPost
    var exchangeRate: String? = ""
    private var priceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()
    private var gradeMappingDescription: String? = ""
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var gradeCode: String? = ""
    private var receivingData = VegaReceiving()
    private var materialNo: String? = ""
    private var intent = Intent()
    private var isSuccess = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeThirdPartyReplaceFragmentCallback
    }

    companion object {
        fun newInstance(model: VegaCoffeeThirdPartyRequestModel, wbModel: VegaCocoaDispatchWB?) =
            VegaCoffeeThirdPartyOwnershipSummaryFragment().putArgs {
                putParcelable("model", model)
                putParcelable("wbModel", wbModel)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentThirdPartyOwnershipSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("thirdpartysalescoffee/ui/VegaThirdPartyOwnershipSummaryFragment")
            .title("IVC/Coffee/Third Party Sales/Ownership Summary").with(tracker)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initExtra()
        initUI()
        vm.deliveryPost.observe(viewLifecycleOwner, Observer { updateOwnershipUI(it) })
        vm.deliveryWSPost.observe(viewLifecycleOwner, Observer { updateSamePartyUI(it) })
    }

    private fun initExtra() {
        model = arguments?.getParcelable<VegaCoffeeThirdPartyRequestModel>("model") as VegaCoffeeThirdPartyRequestModel
        weighBridgeModel = arguments?.getParcelable("wbModel")
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                TP_TO_OLAM,
                true
            )
        ) {
            vm.getGrnPriceDetails()
            vm.getExchangeRate()
            fetchingGrnPriceDetails()
        }
        fetchLocalDBData()

    }

    private fun initUI() {
        model.grnPrice = when{
            model.procureType.contains("Fixed", true)-> model.poDetails?.unitPrice
            model.procureType.contains("Spot", true)-> model.grnPrice
            else-> model.grnPrice
        }
        if (model.transferType.equals(SAME_TP, true)) {
            binding.tvMaterial.text = getString(R.string.vendor)
            binding.tvStage.visibility = View.GONE
            binding.tvStageValue.visibility = View.GONE

        } else if (model.transferType.equals(TP_TO_OLAM, true)) {
            binding.tvMaterial.text = getString(R.string.vendor)
            binding.tvStage.visibility = View.GONE
            binding.tvStageValue.visibility = View.GONE
            binding.tvGrnPrice.visibility = View.VISIBLE
            binding.tvTotalGrnPrice.visibility = View.VISIBLE
            binding.tvTotalGrnValue.visibility = View.VISIBLE
            binding.tvGrnValue.visibility = View.VISIBLE
            binding.tvGrnValue.text = model.grnPrice
        } else if (model.transferType == WEIGHBRIDGE) {

        }
        vm.thirdPartyInfo.observe(viewLifecycleOwner, Observer { if (it != null) updateLocalDBData(it) })
        binding.btnProceed.setOnClickListener { showConformationDialog() }

        vm.getThirdPartyMaterials()
        vm.thirdPartyMaterial.observe(viewLifecycleOwner, Observer {
            thirdPartyMaterials = it
        })

        vm.receive.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(data: Resource<GenericReqAndResp<VegaReceivingResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    moveToSuccessPage(data.data?.data?.wbId)
                    vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                    //prepareErrorData(data.data?.data?.wbId, false, it.error.toString())
                }
            }
        }
    }

    private fun fetchLocalDBData() {
        vm.getThirdPartyLocalData(model.vendorWithTransferType)
        vm.bagItems.observe(viewLifecycleOwner, androidx.lifecycle.Observer { getBagList(it) })
        vm.getBagItems()
    }

    private fun getBagList(bagItems: List<VegaCocoaSweepingBagMaterial>) {
        bagList.clear()
        val lotIds = dispatchLotsList.map { it.batchNumber }
        bagItems.forEach {
            if (lotIds.contains(it.batchNumber)) bagList.add(it)
        }
    }

    private fun updateLocalDBData(data: VegaCoffeeThirdPartyModelWithLots) {
        vegaCoffeeThirdPartyModelWithLots = data
        binding.tvMaterialValue.text = data.model.fromVendorCode.plus("-").plus(data.model.fromVendorName)
        binding.tvStageValue.text = data.model.toVendorCode.plus("-").plus(data.model.toVendorName)
        binding.tvProceesValue.text = data.model.createDate
        binding.tvBomValue.text = data.model.materialName
        val lotSequence = PreferenceHelper.get(Constants.LOT_SEQUENCE, "0")
        data.lots?.forEachIndexed { index, it->
            if(index==0)
                it.oldBatchNumber = vm.generateBatchNumber(lotSequence)
            else{
                val userIndicator = lotSequence.substring(0, 1)
                val lotSequence1 = lotSequence.takeLast(5).toInt().plus(index)
                it.oldBatchNumber = vm.generateBatchNumber(lotSequence1.toString().plus(userIndicator))
            }

        }
        data.lots?.let { dispatchLotsList.addAll(it) }
        vm.lotList = dispatchLotsList as ArrayList<VegaCocoaDispatchLots>

        setUpAdapter(vm.lotList)
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                TP_TO_OLAM,
                true
            )
        ) {
            dispatchLotsList.forEach {
                materialNo =
                    if (!it.materialCode.length.equals(18))
                        "000000".plus(it.materialCode)
                    else it.materialCode
                vm.getPreSamplingQualitydata(it.batchNumber.trim(), materialNo?.trim()!!)
            }
            vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        }
        if (model.transferType.equals(TP_TO_OLAM, true))
            binding.tvTotalGrnValue.text =
                vm.getTotalGrnPriceValue(model.grnPrice ?: "0", vm.getTotalTransferWeight())

    }

    private fun setUpAdapter(list: ArrayList<VegaCocoaDispatchLots>) {
        binding.rvLotList.setUpAdapter(
            list,
            R.layout.item_third_party_add_lot,
            ItemThirdPartyAddLotBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvLotId.text = item.batchNumber
                bindItem.tvStLocationValue1.text = item.storageLocationCode
                bindItem.tvWeightValue.text =
                    item.weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                        ?.plus(item.unitOfMeasure)
                bindItem.tvGradeValue.text = item.materialName
                if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                        TP_TO_OLAM,
                        true
                    )
                ) {
                   bindItem.tvOlamLot.visible()
                   bindItem.tvOlamLotValue.visible()
                    bindItem.tvOlamLotValue.text = item.oldBatchNumber
                }
                if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                        SAME_TP,
                        true
                    )
                ) {
                    bindItem.tfd.visibility = View.GONE
                    bindItem.tvEditedWeight.visibility = View.GONE
                    bindItem.etWeight.visibility = View.GONE
                    bindItem.cbSelectAll.visibility = View.GONE
                    bindItem.tvSelectAll.visibility = View.GONE
                    bindItem.tvStorageLoss.visibility = View.VISIBLE
                    val editedWeigh =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.tvStLocation.text = editedWeigh.plus(" ")
                    bindItem.tvStLocation.visible()
                    val lossvalue =
                        calculateStorageLoss(item.weight.toString(), item.editedWeight.toString())
                    if (lossvalue.equals("0") || covertToDouble(lossvalue) == 0.0) {
                        dispatchLotsList[0].storageLossFlag = true
                    } else if (covertToDouble(lossvalue) > 0.00) {
                        dispatchLotsList[0].storageLossFlag = false
                    }
                    bindItem.tvStLoss.text = lossvalue.plus(" ").plus(item.unitOfMeasure)
                    bindItem.tvStLoss.visible()
                } else {
                    val editedWeight =
                        if (item.editedWeight.isNullOrEmpty()) "0.0" else item.editedWeight.toString()
                            .toDouble()
                            .formatThreeDigits().plus(" ").plus(item.weightToDispatchUOM)
                    bindItem.tvEditedWeight.text = editedWeight.plus(" ")
                    bindItem.tvEditedWeight.visibility = View.VISIBLE
                    bindItem.etWeight.visibility = View.GONE
                    bindItem.cbSelectAll.visibility = View.GONE
                    bindItem.tvSelectAll.visibility = View.GONE
                }
                bindItem.ivClose.setImageDrawable(bindItem.ivClose.context.getDrawable(R.drawable.ic_coffee_tp_edit_gray))
                bindItem.ivClose.setOnClickListener { itemRemoved(item) }
            })
    }

    private fun postDelivery() {
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                TP_TO_OLAM,
                true
            )
        ) {
            vegaNicCoffeeDeliveryPost = VegaNicaraguaCoffeeTPDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                "",
                model.grnPrice.toString(),
                binding.tvTotalGrnValue.text.toString(),
                prepareDeliveryList(),
                exchangeRate,
                priceDetails,
                "ON",receivingData.certificate,
                receivingData.cascara,
                receivingData.humedad,
                receivingData.rendimientoBruto,
                procureType = model.procureType,
                poNumber = model.poDetails?.poId,
                poDetails = model.poDetails

            )
            vm.postNicDeliveryDetailForWeighScale(vegaNicCoffeeDeliveryPost, model.transferType)
        } else {
            vegaCoffeeDeliveryPost = VegaCoffeeTPDeliveryPost(
                getCurrentKey(),
                getPlantDetails(),
                "",
                model.grnPrice.toString(),
                binding.tvTotalGrnValue.text.toString(),
                prepareDeliveryList()
            )

            if (model.transferType.equals(TP_TO_TP, true))
                vm.postDeliveryDetail(vegaCoffeeDeliveryPost, model.transferType)
            else if (model.transferType.equals(WEIGHBRIDGE)) {
                vm.postSalesTruckInData(
                    VegaReceivingPost(
                        getCurrentKey(),
                        getPlantDetails(),
                        prepareWeighbridgePostData()
                    )
                )
            } else
                vm.postDeliveryDetailForWeighScale(vegaCoffeeDeliveryPost, model.transferType)
        }
    }



    private fun prepareWeighbridgePostData(): ArrayList<VegaReceiving> {
        val list = ArrayList<VegaReceiving>()
        if (dispatchLotsList.isNotEmpty()) {
            val receiving = VegaReceiving()
            receiving.weighBridgeId = weighBridgeModel?.weighBridgeId ?: ""
            receiving.netWeight = dispatchLotsList[0].editedWeight ?: ""
            receiving.batchNumber = dispatchLotsList[0].batchNumber
            receiving.plantName = weighBridgeModel?.plantName
            receiving.plantId = weighBridgeModel?.plantId
            receiving.materialCode = weighBridgeModel?.materialName
            receiving.materialCode = weighBridgeModel?.materialCode
            receiving.tareWeight = weighBridgeModel?.grossWeight
            receiving.storageLocationName = weighBridgeModel?.storageLocationName
            receiving.storageLocationCode = weighBridgeModel?.storageLocationCode
            receiving.weighBridgeType = "SALES"
            receiving.wsGate = "WB01"
            receiving.direction = "IN"/*weighBridgeModel?.direction*/
            receiving.approximateWeight = weighBridgeModel?.grossWeight
            receiving.unitsOfMeasure = weighBridgeModel?.unitsOfMeasure ?: ""
            receiving.weighMethod = "WB"
            receiving.vehicleNumber = weighBridgeModel?.vehicleNumber
            receiving.erdat = weighBridgeModel?.erdat
            receiving.supplierCode = model.fromVendorCode
            receiving.supplierName = model.fromVendorName
            receiving.truckDirection = "IN"
            receiving.tareWeight = weighBridgeModel?.tareWeight
            receiving.item = weighBridgeModel?.item
            list.add(receiving)
        }
        return list
    }

    private fun prepareDeliveryList(): List<VegaCoffeeTPDeliveryDetail> {
        val list = ArrayList<VegaCoffeeTPDeliveryDetail>()
        val year: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (item in dispatchLotsList) {
            val deliveryDetail = VegaCoffeeTPDeliveryDetail()
            var bagItems = bagList.filter { it.batchNumber == item.batchNumber && it.baseMaterial == item.materialCode }
            deliveryDetail.batchNumber = item.batchNumber
            if (model.transferType.equals(TP_TO_OLAM, true)) {
                val thMat = thirdPartyMaterials.filter { item.materialCode.contains(it.materialCode) }
                if (thMat.size > 0 && thMat[0].thirdPartyFlag.equals("X/", true))
                    deliveryDetail.materialCode = thMat[0].thirdPartyMaterialCode
                else
                    deliveryDetail.materialCode = item.materialCode
            } else
                deliveryDetail.materialCode = item.materialCode
            deliveryDetail.plantId = item.plantId
            deliveryDetail.netWeight = item.editedWeight
            deliveryDetail.recStorageLocationCode = item.storageLocationCode
            deliveryDetail.storageLocationCode = item.storageLocationCode
            deliveryDetail.unitsOfMeasure = item.unitOfMeasure
            deliveryDetail.year = year.toString()
            deliveryDetail.toVendorCode = model.toVendorCode ?: ""
            deliveryDetail.weighBridgeId = item.weighBridgeId
            deliveryDetail.fromVendorCode = model.fromVendorCode
            deliveryDetail.grnPrice = model.grnPrice ?: ""
            deliveryDetail.storageLossFlag = item.storageLossFlag ?: false
            deliveryDetail.storageLossFlag = item.storageLossFlag ?: false
            deliveryDetail.processOrderNo = item.processOrderNo
            deliveryDetail.delivery = item.delivery
            deliveryDetail.newBatchNumber = item.oldBatchNumber
            deliveryDetail.eudrStatus = model.eudrStatus

            //Bag and pallet details
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
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

            if (bagItems.isNotEmpty()) {
                val bagSort = arrayListOf<VegaCocoaSweepingBagMaterial>()
                val dat = bagItems.sortedByDescending { it.createdPosition }
                bagSort.addAll(dat)
                bagItems = bagSort.asReversed()
                deliveryDetail.huno = bagItems[0].unitsOfMeasure
                deliveryDetail.huwt = bagTareWeight.toString().trim()
                deliveryDetail.huno2 = "KG"
                deliveryDetail.huwt2 = bagItems[0].palletAverage
                deliveryDetail.nohu1 = bagItems.sumOf { tar -> tar.bagCount.toInt() }.toString().trim()
                deliveryDetail.nohu2 = bagItems[0].noOfPallet
                deliveryDetail.grossWeight = grossWeight.toString().trim()
                deliveryDetail.bagList = bagItems
            } else {
                deliveryDetail.huno2 = item.unitOfMeasure
                deliveryDetail.grossWeight = grossWeight.toString().trim()
            }
            list.add(deliveryDetail)
        }
        return list
    }


    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                    TP_TO_OLAM,
                    true
                )
            ) {
                message(R.string.confirm_tmpurchase)
            } else
                message(R.string.conform_dispatch)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postDelivery()
                },
                { dismiss() })
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun updateOwnershipUI(response: Resource<GenericReqAndResp<VegaDeliveryPostResponse>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    isSuccess =true
                    val batch = response.data?.data?.batchNumber
                    moveToSuccessPage(batch, response.data?.message.toString())
                    vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
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

    private fun updateSamePartyUI(response: Resource<GenericReqAndResp<List<VegaDeliveryPostResponse>>>) {
        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true) {
                    isSuccess =true
                    var list = response.data?.data
                    list?.forEach { delivery = it.delivery ?: "" }
                    moveToSuccessPage(response.data?.message, response.data?.message.toString())
                    vm.updateSyncStatus(vegaCoffeeThirdPartyModelWithLots)
                } else {
                    showErrorDialogWithFAQLink(requireContext(), response.data?.message ?: "")
                }
                updateLotSequnce()
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                updateLotSequnce()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
        }
    }

    private fun updateLotSequnce() {
        try{
            val seq = vm.lotList.map { it.oldBatchNumber?.substring((it.oldBatchNumber?.length?:0) - 5) }.maxByOrNull { it.toString() }.toString()
            val currentBatch = PreferenceHelper.get(Constants.LOT_SEQUENCE, "")
            val userIndicator = currentBatch.substring(0, 1)
            val fullSeq = userIndicator.plus(seq)
            saveLotSequence(fullSeq)
            postUpdateLotSequence(fullSeq)
        }catch (e:StringIndexOutOfBoundsException){e.printStackTrace()}
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(
            AppUtils.TITLE,
            getString(R.string.success_truck_in)
        )
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id_is).plus(wbId))
        startActivity(intent)
        requireActivity().finish()
    }


    private fun moveToSuccessPage(deliveryId: String?, message: String) {
        val grnMsg = message.split("with Doc")
        val otherMsg = message.split("successfully")
        //val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                SAME_TP,
                true
            )
        ) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.sales_success))
            // intent.putExtra(UIUtils.NICARAGUA_MTNT_OBD_NUMBER, delivery)
            // intent.putExtra(UIUtils.NICARAGUA_MTNT_SCAN_OBD_NUMBER, true)

        } else if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                TP_TO_OLAM,
                true
            )
        ) {
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.purchase_success)
            )
        } else
            intent.putExtra(
                AppUtils.TITLE,
                getString(R.string.success_dispatch)
            )
        if (TP_TO_TP.equals(model.transferType, true)) {
            intent.putExtra(AppUtils.PRINT_ENABLE, true)
            intent.putExtra("fromcoffee", true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, prepareLotCard())
        }
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(
                TP_TO_OLAM,
                true
            )
        ) {
            var msg = message.replace("\n\n","\n").split("\n")
            if(msg.isNotEmpty() && msg.size == 4) {
                val sucMsg = prepareMessage(msg)
                intent.putExtra(
                    AppUtils.SUB_TITLE, sucMsg
                )
            } else
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.tp_owner_success_grn, grnMsg[1].replace("\n\n", "\n"))
            )

        } else {
            if (grnMsg.size > 1)
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.tp_owner_success_grn, grnMsg[1].replace("\n\n", "\n"))
                )

            else {
                if (otherMsg.size > 1)
                    intent.putExtra(
                        AppUtils.SUB_TITLE,
                        getString(
                            R.string.tp_owner_success_material,
                            otherMsg[0].replace("1.", "").replace("Material Document", "", true).replace(
                                "posted",
                                ""
                            ).replace("created", "")
                        )
                    )
                else
                    intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.tp_owner_success, deliveryId))
            }
        }
        if (getCurrentKey().split("_")[1].contains("NI") && model.transferType.equals(TP_TO_OLAM,true )){
            /*startActivity(intent)
            requireActivity().finish()*/
        }else{
            startActivity(intent)
            requireActivity().finish()
        }

    }

    private fun prepareMessage(msg: List<String>): String {
       var message = msg
        var sucMsg = message[0].split(":")
        var quaMsg = message[1].split(":")
        var success = getString(R.string.tp_owner_success_weighbridge, sucMsg[1]).plus("\n").plus(getString(R.string.tp_owner_success, quaMsg[1])).plus("\n").plus(quaMsg[0]).plus("\n").plus(message[2]).plus("\n").plus(message[3])
        return success
    }

    private fun prepareLotCard(): ArrayList<VegaCoffeeSalesLots> {
        val lotList = arrayListOf<VegaCoffeeSalesLots>()
        vm.lotList.forEach {
            val lot = VegaCoffeeSalesLots()
            lot.batchNumber = it.batchNumber
            lot.editedWeight = it.editedWeight
            lot.unitOfMeasure = it.unitOfMeasure
            lot.materialName = it.materialName
            lot.materialCode = it.materialCode
            lot.storageLocationCode = it.storageLocationCode
            lotList.add(lot)
        }
        return lotList
    }


    private fun itemRemoved(item: VegaCocoaDispatchLots) {
        activity?.onBackPressed()
    }

    private fun getFilteredPriceDetails(priceDetails: ArrayList<VegaNicaraguaGrnPriceDetails>): ArrayList<VegaNicaraguaGrnPriceDetails> {
        var filteredPriceDetails = arrayListOf<VegaNicaraguaGrnPriceDetails>()

        priceDetails.forEach {
            if (it.fieldName.equals("LOWGRD01", true)) {
                filteredPriceDetails.add(it)
            } else if ("LOWGRD02".equals(it.fieldName, true)) {
                filteredPriceDetails.add(it)
            } else if ("LOWGRD03".equals(it.fieldName, true)) {
                filteredPriceDetails.add(it)
            } else if (gradeMappingDescription.equals(it.description)) {
                filteredPriceDetails.add(it)
            }
        }
        return filteredPriceDetails
    }
    private fun fetchingExchangeRate() {
        vm.exchangeRate.observe(viewLifecycleOwner, Observer { response ->
            response?.let {

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.data.let {
                            exchangeRate = it?.exchangeRate
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })
    }
    private fun fetchingGrnPriceDetails() {
        vm.grnPriceDetails.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        when (it.data?.success) {
                            true -> {
                                priceDetails.clear()
                                val dataValue = it.data?.data!!
                                priceDetails = prepareGrnPriceDetailsList(dataValue)
                                priceDetails = getFilteredPriceDetails(priceDetails)
                                if (priceDetails.size > 0)
                                    fetchingExchangeRate()
                            }
                            else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        requireContext().toast(it.error.toString())
                    }
                }
            }
        })
    }
    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            preQualityList.forEach { item1 ->
                                if (item1.sapQCName!!.contains("NIFG0014"))
                                    receivingData.certificate = item1.satNam
                                else if (item1.sapQCName!!.contains("NIRM0010")) {
                                    receivingData.rendimientoBruto = item1.satNam?.replace("%", "")?.trim()
                                    receivingData.yieldPercentage =  item1.satNam?.replace("%", "")?.trim()
                                } else if (item1.sapQCName!!.contains("NIEXPOP"))
                                    receivingData.exportablePercentage =
                                        item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NICASCAB"))
                                    receivingData.cascara = item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NIRM0003"))
                                    receivingData.humedad = item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NIDESA"))
                                    receivingData.DESMA = item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NIDESC"))
                                    receivingData.DESMC = item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NIDESD"))
                                    receivingData.DESMD = item1.satNam?.replace("%", "")?.trim()
                                else if (item1.sapQCName!!.contains("NIPOSITI")) {
                                 receivingData.grade = item1.satNam?.trim()
                                    gradeCode = receivingData.grade?.substring(receivingData.grade!!.length - 4)
                                    println("hhhhhhhh $gradeCode")
                                    vm.getGradeMapping(gradeCode.toString())
                                    vm.gradeMapping.observe(viewLifecycleOwner, Observer {
                                        gradeMappingDescription = it?.description ?: ""
                                    })
                                    fetchingGrnPriceDetails()
                                }
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun postUpdateLotSequence(batchNumber: String?) {
        val input = workDataOf(UIUtils.LOT_DETAIL to batchNumber)
        val worker = getThirdPartyLotSequnceOneTimeRequestWorker(input)
        enQueueWorker(worker, requireContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            if(isSuccess){
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            if(isSuccess){
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                        WorkInfo.State.RUNNING -> {
                        }
                        else -> {}
                    }
                }

            })
    }

}
