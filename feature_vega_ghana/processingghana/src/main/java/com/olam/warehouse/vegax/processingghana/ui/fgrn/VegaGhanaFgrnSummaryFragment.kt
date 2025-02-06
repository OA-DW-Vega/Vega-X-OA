package com.olam.warehouse.vegax.processingghana.ui.fgrn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.master.vegaecuador.entity.VegaEcuadorDispatchStocks
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaOfflineFgrnProcessLotDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingghana.data.domain.model.VegaGhanaProcessingFgrnResponse
import com.olam.warehouse.vegax.processingghana.databinding.FragmentVegaGhanaFgrnSummaryBinding
import com.olam.warehouse.vegax.processingghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaGhanaFgrnSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_fgrn_summary
    private lateinit var binding: FragmentVegaGhanaFgrnSummaryBinding
    private val vm: VegaGhanaFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var bagItem = VegaCoffeeFgrnGradesMatrialWeights()
    private var fgrnItemWithGrades = VegaCoffeeFgrnItemWithGrades()

    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaGhanaFgrnBagMaterialWithId>()
    private var poList = ArrayList<VegaFgrnProcessingOrder>()
    private var rmin = ArrayList<VegaProcessingList>()
    private var totalBagConsumed: ArrayList<String>? = null
    private var stageFevor: String = ""
    private var cfgNo: String? = ""
    private var auartNo: String? = ""
    private var processOrder: String? = ""
    private var bagMaterialCode: String = ""
    private var bagConsumptionQuantity: String = ""
    private var bagNetWeight: String = ""
    private var standardWeight: String = ""
    private var bagType: String = ""
    private var batchNo: String = ""
    private var totalBagCount: Int = 0
    private var newBagValue: String = ""
    private var materialName: String = ""
    private var dryLoss: String = ""
    private var temRminId: String = ""
    private var weight: String = ""
    private var lotWeight: String = ""
    private var vegaStage = VegaProcessingStage()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    val conData = arrayListOf<VegaCocoaFgrnBagCosumption>()
    private var stockList = VegaEcuadorDispatchStocks()
    private var workFlowData: WorkflowFields? = null



    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String,
            model: VegaCoffeeFgrnItems,
            id: String,
            vegaStage: VegaProcessingStage
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems, vegaStage: VegaProcessingStage, bagItem: VegaCoffeeFgrnGradesMatrialWeights) = VegaGhanaFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putParcelable(VEGA_STAGE, vegaStage)
            putParcelable(BAG_ITEM, bagItem)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnSummaryFragment").title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "24")
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()
        bagItem = arguments?.getParcelable(BAG_ITEM) ?: VegaCoffeeFgrnGradesMatrialWeights()
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        if (!isOnline()) {
            binding.llrminQty.isVisible = true
            binding.rminQty.text = fgrnItem.rminQty
        }
        vm.fgrnItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateItems(it)
            }
        })
        vm.getFgrnItems(fgrnItem.fgrnId)

        vm.stockLot.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                stockList = it
            }
        })
        binding.tvWeightData.text = bagItem.netWeight.plus(" MT")
        weight = bagItem.netWeight!!
        binding.tvNoOfBag.text = bagItem.palletWeight
        binding.tvBagCountValue.text = bagItem.bagCount
        binding.tvBagTypeValue.text = bagItem.bagType
        processOrder = fgrnItem.processOrderNo
        binding.tvLotNo.text = fgrnItem.processOrderNo
        var rminWeight = 0.0
        var fgrnWeight = 0.0
        var totalRminWeight = 0.0
        rminWeight = fgrnItem.rminTotal!!.toDouble()
        fgrnWeight = bagItem.netWeight!!.toDouble()
        totalRminWeight = rminWeight - fgrnWeight
        dryLoss = totalRminWeight.formatThreeDigits()
        binding.tvDryloss.text = totalRminWeight.formatThreeDigits().plus(" MT")
//        binding.tvShift.text = getString(R.string.shift).plus(": ").plus(fgrnItem.shiftSelection)
//        binding.tvOperatorName.text = fgrnItem.remarks
//        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
//        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
//        vm.fetchStages()
//        vm.poDetailListFgrn.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaGhanaProcessingFgrnResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            updateFgrnStatus(success?.get(0)?.messages?.get(0)?.message, 4)
                            moveToSuccessPage(success)
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    updateFgrnStatus(it.error.toString(), 3)
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }
    private fun updageStageUI(stageList: List<VegaProcessingStage>) {
        val stage = ArrayList<VegaProcessingStage>()
        val stageInitItem = VegaProcessingStage()
        stage.add(stageInitItem)
        stage.addAll(stageList)
        stage.forEachIndexed { index, vegaProcessingStage ->
            if (vegaStage.processName == vegaProcessingStage.processName) {
                stageFevor = vegaProcessingStage.auart.toString()
                cfgNo = vegaProcessingStage.cfgNo
                auartNo = vegaProcessingStage.fevor
                vm.fetchFgrnPoDetailsListSummary(stageFevor, cfgNo!!, auartNo!!)
            }
        }
    }

    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                poList.clear()
                                poList.addAll(it)
                                poList.forEachIndexed { index, s ->
                                    poList[index].netWeight = poList[index].netWeight.toString()
                                    if(poList[index].processOrderNo == processOrder)
                                    {
                                        rmin = poList[index].rmin as ArrayList<VegaProcessingList>
//                                        calculateBagConsumption(fgrnItemWithGrades)
                                    }
                                }
                            }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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

    private fun updateFgrnStatus(message: String?, status: Int) {
        message?.let { vm.updateFgrnStatus(it, status, fgrnItem.fgrnId) }
    }

    private fun moveToSuccessPage(success: List<VegaGhanaProcessingFgrnResponse>?) {
        //LotCard
        vm.deleteLotDetails()
        var lotList = arrayListOf<VegaCoffeeSalesLots>()
        fgrnItemWithGrades.gradeItems?.forEach {
            val lot = VegaCoffeeSalesLots()
            var grossWeight = 0.0
            var tareWeight = 0.0
            var bagTareWeight = 0.0
            var netWeight = 0.0
            it.bagItems?.forEach { item ->
                val palletAvg =
                    if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                        ?.div(item.noOfPallet?.toInt()!!) else 0.0
                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
                    .plus(palletAvg!!)
                bagTareWeight =
                    bagTareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
            }
            when (it.fgrnGrades.meins) {
                "MT" -> netWeight = (grossWeight.minus(tareWeight))
                "KG" -> netWeight = grossWeight.minus(tareWeight)
                else -> netWeight = grossWeight.minus(tareWeight)
            }
            lot.batchNumber = success?.get(0)?.batchNumber!!
            val netwg = success.get(0).netWeight?.toDouble()
            lot.editedWeight = netwg?.formatThreeDigits()
            lot.unitOfMeasure = it.fgrnGrades.meins
            lot.materialName = it.fgrnGrades.materialName
            lot.materialCode = it.fgrnGrades.materialCode
            lotList.add(lot)
        }

        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val  title_msg = if (success?.get(0)?.msg.isNullOrEmpty()) requireContext()?.resources?.getString(R.string.fgrn_success) else success?.get(0)?.msg
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, title_msg)
//        intent.putExtra("fromsesameprocessing", true)
//        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotList)
//        intent.putExtra(AppUtils.PRINT_ENABLE, true)
//        val tallyKeys = ArrayList<String>()
//        tallyKeys.add(success?.get(0)?.encodedImageContent ?: "")
//        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
        startActivity(intent)
        requireActivity().finish()
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    fgrnItemWithGrades.gradeItems?.forEach {
                        it.bagItems?.forEach { item ->
                            item.bagMaterialCode = bagItem.bagMaterialCode
                            item.bagType = bagItem.bagType
                            item.netWeight = bagItem.netWeight
                            item.bagCount = bagItem.bagCount
                        }
                    }
                    val postReq = prepareFgrnPostRequest(fgrnItem, fgrnItemWithGrades.gradeItems,dryLoss,notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                        nextWorkFlowRole = workFlowData?.workflowModule,
                        navId = workFlowData?.workflowId,
                        currentWorkFlowRole = workFlowData?.module)

                    var qualityLotDetails = VegaGhanaOfflineFgrnProcessLotDetails()
                    var qualityDetails =
                        prepareOfflineFgrnPostRequest(
                            postReq,
                            fgrnItem.fgrnId,
                            fgrnItem.rminId!!,
                            fgrnItem.rminQty
                        )
                    postReq.processingLotDtls.forEach {
                        qualityLotDetails = prepareOfflineFgrnProcessLotDetails(
                            it, fgrnItem.fgrnId, fgrnItem.processOrderNo,
                            fgrnItem.stageFevor!!, materialName, fgrnItem.rminId!!
                        )
                    }

                    if (isOnline()) {
                        vm.postFgrn(postReq)
                    } else {
                        vm.saveOfflineFgrnDetails(qualityDetails)
                        vm.saveOfflineFgrnLotDetails(qualityLotDetails)
                        moveToOfflineSuccessPage()
                    }
                },
                { dismiss() })
        }
    }

    private fun moveToOfflineSuccessPage() {
        if (stockList.batchNumber != "") {
            var updatedWeight = stockList.weight?.toDouble()!! + weight.toDouble()
            vm.updateStockDetails(updatedWeight.toString(), batchNo)
        }
        var updatedTotal = fgrnItem.rfgrnTotal?.toDouble()!! + weight.toDouble()
        vm.updateFgrnTotal(updatedTotal.toString(), fgrnItem.processOrderNo)
        vm.deleteLotDetails()
        vm.deleteAllFgrnItems()
        vm.updateFgrnStatus(true, fgrnItem.rminId!!)
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, "Offline FGRN Successfully Completed")
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateItems(data: VegaCoffeeFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        data.gradeItems?.let { updateAdapter(it) }
    }


    private fun updateAdapter(gradeItems: List<VegaCoffeeFgrnGradesWithBagItems>) {
        gradeItems.forEach {
            binding.tvMaterialName.text = it.fgrnGrades.materialName
            materialName = it.fgrnGrades.materialName
            binding.tvLotId.text = it.fgrnGrades.batchNumber
            batchNo = it.fgrnGrades.batchNumber
            lotWeight = it.fgrnGrades.netWeight!!
            binding.tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
        }
        if (!isOnline()) {
            vm.getStockDetailsByBatchNo(batchNo)
        }
//        binding.rvFgrnSummary.setUp(gradeItems as MutableList, R.layout.item_vega_ghana_fgrn_summary, { it, pos ->
//            tv_material_name.text = it.fgrnGrades.materialName
//            var grossWeight = 0.0
//            var tareWeight = 0.0
//            val netWeight: Double
//            it.bagItems?.forEach { item ->
//                val palletAvg =
//                    if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
//                        ?.div(item.noOfPallet?.toInt()!!) else 0.0
//                grossWeight = grossWeight.plus(item.grossWeight.toDouble())
//                tareWeight = tareWeight.plus(item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!)
//                    .plus(palletAvg!!)
//            }
//            netWeight = grossWeight.minus(tareWeight)
//            tv_weight_data.text = netWeight.formatThreeDigits().plus(" KG")
//            gradeItems.forEach {
//                if (it.fgrnGrades.isCreateNewLot!!)
//                    tvLotId.text = ""
//                else
//                    tvLotId.text = it.fgrnGrades.batchNumber
//            }
//
//            tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
//            tvNoOfBag.text = it.bagItems?.sumBy { it1 -> it1.totalBagCount?.toInt() ?: 0 }.toString()
//            ivEdit.setOnClickListener { view ->
//                val gson = GsonUtils()
//                val poGrade = gson.toJson(listOf(it.fgrnGrades))
//                callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT_EDIT, fgrnItem, poGrade,vegaStage)
//            }
//        })
//
//        calculateBagConsumption(fgrnItemWithGrades)
    }

    private fun updateBagConsumptionAdapter(bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>,data: VegaCoffeeFgrnItemWithGrades) {

        if (bagConsumpList.size > 0) {
//            binding.rvBagConsumption.visible()
//            binding.tvBagConsumption.visible()
//            binding.tvBagConsumptionValue.visible()
        } else {
//            binding.rvBagConsumption.gone()
//            binding.tvBagConsumption.gone()
//            binding.tvBagConsumptionValue.gone()
        }

        updateTotalBagCount()

//        binding.rvBagConsumption.setUp(bagConsumpList, R.layout.item_vega_ghana_bag_consump_summary, { item, pos ->
//            tvBagTypeValue.text = item.bagType
//            tvBagCountValue.setText(item.bagCount)
//            tvBatchValue.text = item.batchNumber
//            tvBagCountValue.onChange {
//                item.bagCount = it
//                updateTotalBagCount()
//            }
//
//        })
    }

    private fun updateTotalBagCount() {
        totalBagCount = 0
        conData.forEach {
            if (it.bagCount == "")
                it.bagCount = 0.toString()
            else {
                var bagcount = it.bagCount.toInt()
                totalBagCount += bagcount
            }
        }
        binding.tvBagConsumptionValue.text = totalBagCount.toString()
    }

}



