package com.olam.warehouse.vegax.processingsesame.ui.fgrn

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
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaPackageMaterial
import com.olam.warehouse.master.vega.entity.VegaProcessingList
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnGradesWithBagItems
import com.olam.warehouse.master.vegacoffee.model.VegaCoffeeFgrnItemWithGrades
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.data.domain.model.VegaSesameFgrnBagMaterialWithId
import com.olam.warehouse.vegax.processingsesame.data.domain.model.VegaSesameProcessingFgrnResponse
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameFgrnSummaryBinding
import com.olam.warehouse.vegax.processingsesame.databinding.ItemVegaSesameBagConsumpSummaryBinding
import com.olam.warehouse.vegax.processingsesame.databinding.ItemVegaSesameFgrnSummaryBinding
import com.olam.warehouse.vegax.processingsesame.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.roundToInt

class VegaSesameFgrnSummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_fgrn_summary
    private lateinit var binding: FragmentVegaSesameFgrnSummaryBinding
    private val vm: VegaSesameFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var fgrnItemWithGrades = VegaCoffeeFgrnItemWithGrades()

    private var gradesWithBags = listOf<VegaCoffeeFgrnGradesWithBagItems>()
    private val materialList = arrayListOf<VegaSesameFgrnBagMaterialWithId>()
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
    private var vegaStage = VegaProcessingStage()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    val conData = arrayListOf<VegaCocoaFgrnBagCosumption>()

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
        fun newInstance(model: VegaCoffeeFgrnItems, vegaStage: VegaProcessingStage) =
            VegaSesameFgrnSummaryFragment().putArgs {
                putParcelable(FRAG_ITEM, model)
                putParcelable(VEGA_STAGE, vegaStage)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaSesameFgrnSummaryBinding.inflate(layoutInflater)
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
        context?.let {
            getActionBtnChangedView(binding.tvHeadPoDetail, it, false)
            getActionBtnChangedView(binding.tvConfirm, it, true)
        }
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()
        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()
        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateItems(it) })
        vm.getFgrnItems(fgrnItem.fgrnId)
        processOrder = fgrnItem.processOrderNo
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(": ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.remarks
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.fetchStages()
        vm.poDetailListFgrn.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaSesameProcessingFgrnResponse>>>) {
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
                                    poList[index].netWeight = convertMtToKg(
                                        poList[index].netWeight.toString(),
                                        poList[index].unitsOfMeasure.toString()
                                    )
                                    if (poList[index].processOrderNo == processOrder) {
                                        rmin = poList[index].rmin as ArrayList<VegaProcessingList>
                                        calculateBagConsumption(fgrnItemWithGrades)
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

    private fun moveToSuccessPage(success: List<VegaSesameProcessingFgrnResponse>?) {
        //LotCard
        var lotList = arrayListOf<VegaCoffeeSalesLots>()
        var po = ""
        fgrnItemWithGrades.gradeItems?.forEach {
            if (it.fgrnGrades.xchpf == "X") {
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
                    "MT" -> netWeight = (grossWeight.minus(tareWeight)).div(1000)
                    "KG" -> netWeight = grossWeight.minus(tareWeight)
                    else -> netWeight = grossWeight.minus(tareWeight)
                }
                lot.batchNumber = success?.get(0)?.batchNumber!!
//            lot.batchNumber = it.fgrnGrades.batchNumber
                val netwg = success.get(0).netWeight?.toDouble()
                lot.editedWeight = netwg?.formatThreeDigits()
                if (getCurrentKey().split("_")[2].contains("CASH")) {
                    success.forEach { item ->
                        if (item.batchNumber == it.fgrnGrades.batchNumber) {
                            lot.editedWeight = item.netWeight
                            lot.batchNumber = item.batchNumber!!
                        }

                    }
                }
                lot.unitOfMeasure = it.fgrnGrades.meins
                lot.materialName = it.fgrnGrades.materialName
                lot.materialCode = it.fgrnGrades.materialCode
                lotList.add(lot)
            }
        }

        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item -> if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "") }
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        intent.putExtra("fromsesameprocessing", true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotList)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        val tallyKeys = ArrayList<String>()
        po = fgrnItemWithGrades.fgrnItems.processOrderNo
        if (getCurrentKey().split("_")[2].contains("SESA")) {
            tallyKeys.add(success?.get(0)?.encodedImageContent ?: "")
            intent.putExtra(
                AppUtils.SUB_TITLE,
                message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
            )

        } else {
            success?.forEach { it ->
                tallyKeys.add(it.encodedImageContent ?: "")
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    "Process Order ".plus(po).plus("\n").plus("Batch Number ")
                        .plus(data.toString().replace("[", "").replace("]", ""))
                )
            }
        }

        intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
//        intent.putExtra(AppUtils.SUB_TITLE, message.plus(" ").plus(data.toString().replace("[", "").replace("]", "")))
//        intent.putExtra(AppUtils.SUB_TITLE, "Process Order ".plus(po).plus("\n").plus("Batch Number ").plus(data.toString().replace("[", "").replace("]", "")))
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
                            var selectedBagCode = item.bagMaterialCode
                            if (selectedBagCode.startsWith("0000002")) {
                                conData.forEach {
                                    if (it.batchNumber == item.netWeight) {
                                        item.bagCount = it.bagCount
                                    }
                                }
                            } else
                                item.bagCount = item.totalBagCount!!
                        }
                    }
                    fgrnItemWithGrades.fgrnItems
                    val count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
                    if (count != 0 && DateUtils.isFirstDaysOfMonth(context, count)) {
                        UIUtils.showPostingDateDialog(context, count, object : UIUtils.DialogClick {
                            override fun onPositive(date: String) {
                                var postingDate =
                                    DateUtils.oneFormatToOtherFormat(
                                        date,
                                        "dd-MMM-yyyy",
                                        "yyyy-MM-dd'T'HH:mm:ss"
                                    )
                                val postReq = prepareFgrnPostRequest(
                                    fgrnItem,
                                    fgrnItemWithGrades.gradeItems,
                                    postingDate
                                )
                                vm.postFgrn(postReq)
                            }
                        })
                    } else {

                        val postReq =
                            prepareFgrnPostRequest(fgrnItem, fgrnItemWithGrades.gradeItems, "")
                        vm.postFgrn(postReq)
                    }
                },
                { dismiss() })
        }
    }

    private fun updateItems(data: VegaCoffeeFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        data.gradeItems?.let { updateAdapter(it) }
    }

    private fun calculateBagConsumption(data: VegaCoffeeFgrnItemWithGrades) {
        conData.clear()
        gradesWithBags = data.gradeItems?.toList() ?: emptyList()
        gradesWithBags.forEach {
            it.bagItems?.forEach {
                bagMaterialCode = it.bagMaterialCode
                if (bagMaterialCode.startsWith("0000002")) {
                    bagType = it.bagType
                    batchNo = it.batchNumber
                    bagNetWeight = it.netWeight!!
                    bagTypeList.forEach {
                        if (it.bagMaterialCode == bagMaterialCode) {
                            standardWeight = it.standardWeight!!
                            rmin.forEach {
                                if (it.materialCode == bagMaterialCode) {
                                    var value = bagNetWeight.toDouble() / standardWeight.toDouble()
                                    bagConsumptionQuantity = value.roundToInt().toString()
                                    val dat = VegaCocoaFgrnBagCosumption()
                                    dat.bagCount = bagConsumptionQuantity
                                    dat.bagType = bagType
                                    dat.batchNumber = bagNetWeight
                                    conData.add(dat)
                                }
                            }
                        }
                    }
                }
            }
        }
        updateBagConsumptionAdapter(conData)
    }

    private fun updateAdapter(gradeItems: List<VegaCoffeeFgrnGradesWithBagItems>) {
        binding.rvFgrnSummary.setUpAdapter(
            gradeItems as MutableList,
            R.layout.item_vega_sesame_fgrn_summary,
            ItemVegaSesameFgrnSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMaterialName.text = it.fgrnGrades.materialName
                var grossWeight = 0.0
                var tareWeight = 0.0
                val netWeight: Double
                it.bagItems?.forEach { item ->
                    val palletAvg =
                        if (item.noOfPallet?.toInt() != 0) item.palletWeight?.toDouble()
                            ?.div(item.noOfPallet?.toInt()!!) else 0.0
                    grossWeight = grossWeight.plus(item.grossWeight.toDouble())
                    tareWeight = tareWeight.plus(
                        item.tareWeight?.toDouble()?.times(item.totalBagCount?.toDouble()!!)!!
                    )
                        .plus(palletAvg!!)
                }
                netWeight = grossWeight.minus(tareWeight)
                bindItem.tvWeightData.text = netWeight.formatThreeDigits().plus(" KG")
                gradeItems.forEach {
                    if (it.fgrnGrades.isCreateNewLot!!)
                        bindItem.tvLotId.text = ""
                    else
                        bindItem.tvLotId.text = it.fgrnGrades.batchNumber
                }

                bindItem.tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                bindItem.tvNoOfBag.text =
                    it.bagItems?.sumBy { it1 -> it1.totalBagCount?.toInt() ?: 0 }.toString()
                bindItem.ivEdit.setOnClickListener { view ->
                    val gson = GsonUtils()
                    val poGrade = gson.toJson(listOf(it.fgrnGrades))
                    callBack?.replaceFgrnFragment(
                        FRAG_ADD_WEIGHT_EDIT,
                        fgrnItem,
                        poGrade,
                        vegaStage
                    )
                }
            })

        calculateBagConsumption(fgrnItemWithGrades)
    }

    private fun updateBagConsumptionAdapter(
        bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>
    ) {

        if (bagConsumpList.size > 0) {
            binding.rvBagConsumption.visible()
            binding.tvBagConsumption.visible()
            binding.tvBagConsumptionValue.visible()
        } else {
            binding.rvBagConsumption.gone()
            binding.tvBagConsumption.gone()
            binding.tvBagConsumptionValue.gone()
        }

        updateTotalBagCount()

        binding.rvBagConsumption.setUpAdapter(bagConsumpList,
            R.layout.item_vega_sesame_bag_consump_summary,
            ItemVegaSesameBagConsumpSummaryBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvBagTypeValue.text = item.bagType
                bindItem.tvBagCountValue.setText(item.bagCount)
                bindItem.tvBatchValue.text = item.batchNumber
                bindItem.tvBagCountValue.onChange {
                    item.bagCount = it
                    updateTotalBagCount()
                }

            })
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
