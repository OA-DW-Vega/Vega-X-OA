package com.olam.warehouse.vegax.pilesesame.ui

import android.annotation.SuppressLint
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
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.master.veganicaragua.model.VegaNicaPileDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.pilesesame.R
import com.olam.warehouse.vegax.pilesesame.data.domain.model.*
import com.olam.warehouse.vegax.pilesesame.databinding.FragmentPileManagementSummarySesameBinding
import com.olam.warehouse.vegax.pilesesame.databinding.ItemPileManagementSummarySesameBinding
import com.olam.warehouse.vegax.pilesesame.utils.MATERIAL_LIST
import com.olam.warehouse.vegax.pilesesame.utils.PILE_LIST
import com.olam.warehouse.vegax.pilesesame.utils.PILE_MANAGEMENT_SUMMARY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

class VegaSesamePileManagementSummaryFragment : BaseFragment() {

    private var pileNo: String = ""
    override val layoutResourceId = R.layout.fragment_pile_management_summary_sesame
    private lateinit var binding: FragmentPileManagementSummarySesameBinding
    private var callBack: CallBack? = null
    private var alreadySelected = arrayListOf<VegaCocoaDispatchLots>()
    private val vm: VegaSesamePileManagementViewModel by viewModel()
    private var materialList = arrayListOf<VegaCocoaDispatchLots>()
    private lateinit var vegaPileSelectionModel: VegaCocoaDispatchLots
    private var pileSelection = VegaCocoaDispatchLots()
    private var storageLocationCode: String? = ""
    private var materialCode: String? = ""
    private var plantId: String? = ""
    private var totalweight: Double = 0.0
    var isMillingPlant = false
    var isDryingPlant = false
    var isThirdPartyPant = false
    var isPilePostSuccess = false
    var ticketNumber = ""
    var qualityDetail = VegaQualityDetail()
    var qualityDetailList = ArrayList<VegaQualityDetail>()
    var ticketDetailList = ArrayList<VegaQualityDetail>()
    private var qualityGradeWithDescList = java.util.ArrayList<QualitativeParams>()


    interface CallBack {
        fun moveToLotEdit(list: kotlin.collections.ArrayList<VegaCocoaDispatchLots>)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }


    companion object {
        fun newInstance(
            materialList: ArrayList<String>, alreadySelected: ArrayList<VegaCocoaDispatchLots>,
            pileSelectionList: VegaCocoaDispatchLots
        ) =
            VegaSesamePileManagementSummaryFragment().putArgs {
                putStringArrayList(MATERIAL_LIST, materialList)
                putParcelableArrayList(PILE_MANAGEMENT_SUMMARY, alreadySelected)
                putParcelable(PILE_LIST, pileSelectionList)
            }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPileManagementSummarySesameBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("pilemanagement/ui//VegaCoffeePileManagementSummaryFragment").title("PileMangement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }

        if (getCurrentKey().contains("VEGA_NI")) {
            vm.getConfigItems(UserRoles.PROCESSING.role)
            vm.getQualityGradesListWithDesc()
        }

        vm.getQualityGradeDescList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            qualityGradeWithDescList = java.util.ArrayList(it)
        })


        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })

        vm.postPile.observe(viewLifecycleOwner, Observer { updateSuccessUI(it) })
        pileSelection =
            arguments?.getParcelable<VegaCocoaDispatchLots>(PILE_LIST) as VegaCocoaDispatchLots
        setupPileCardView(pileSelection)
        alreadySelected =
            arguments?.getParcelableArrayList<VegaCocoaDispatchLots>(PILE_MANAGEMENT_SUMMARY) as ArrayList
        alreadySelected.forEach {
            storageLocationCode = it.storageLocationCode
            materialCode = it.materialCode
            plantId = it.plantId
            totalweight += it.editedWeight?.toDouble()!!
        }
        materialList = arguments?.getStringArrayList(MATERIAL_LIST) as ArrayList<VegaCocoaDispatchLots>
        setupAdapter(alreadySelected)

        binding.btnProceed.setOnClickListener {
            showConformationDialog()
        }
        if (getCurrentKey().contains("VEGA_NI")) {
            if (alreadySelected.isNotEmpty()) {
                val mergedWeight = alreadySelected.sumOf { it.editedWeight?.toDouble() ?: 0.0 }.formatTwoDigits()
                binding.tvTotalGrnPrice.visible()
                binding.tvTotalGrnValue.visible()
                binding.tvTotalGrnPrice.text = getString(R.string.merged_weight)
                binding.tvTotalGrnValue.text = "${mergedWeight.toString().plus(" ")}${alreadySelected[0].unitOfMeasure}"
            }

        }

        vm.updatePileSequence.observe(viewLifecycleOwner, { UpdateBatch(it) })
        vm.lotQuality.observe(viewLifecycleOwner, Observer {
            lotQualityDetails(it)
        })

        vm.wsWeightedAvgPost.observe(viewLifecycleOwner,{ updatedWeightedAvgDetail(it)})

    }



    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val dryingPlant = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        val millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }
        val thirdPartyPalnts = configItems.filter { it.process.equals(ConfigItems.THIRD_PARTY_PLANT.item) }
        isDryingPlant = dryingPlant.isNotEmpty()
        isMillingPlant = millingPlants.isNotEmpty()
        isThirdPartyPant = thirdPartyPalnts.isNotEmpty()

        createQualityDetail()
    }


    private fun showConformationDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_piles)
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

    private fun updatedWeightedAvgDetail(response: Resource<GenericReqAndResp<VegaNigeriaSesameWeightedAverageResponse>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    vm.getLotQualityParams(pileSelection.batchNumber, pileSelection.materialCode)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error} /n ${getString(R.string.weighted_avg_error)}")
                }
                else -> {}
            }
        }

    }

    private fun lotQualityDetails(response: Resource<GenericReqAndResp<List<VegaDispatchLotQuality>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (response.data?.data?.isNotEmpty() == true) {
                        val qualityList = response.data?.data!![0].qualityParameters

                        if (qualityList.isNotEmpty() && !isPilePostSuccess) {
                            qualityDetailList.clear()
                            qualityList.forEach {
                                val qualityDetail = VegaQualityDetail()
                                qualityDetail.nameChar = it.sapQCName.toString()
                                qualityDetail.qualityParameterValue = it.satNam.toString()
                                qualityDetailList.add(qualityDetail)

                            }

                        }


                        if (isPilePostSuccess) {
                            if(qualityList.isNotEmpty()){
                                qualityDetailList.clear()
                                qualityList.forEach {
                                    val qualityDetail = VegaQualityDetail()
                                    qualityDetail.nameChar = it.sapQCName.toString()
                                    qualityDetail.qualityParameterValue = it.satNam.toString()
                                    qualityDetailList.add(qualityDetail)
                                }
                            }
                                updatePileAndTicketSequence()
                            }
                    }

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    @SuppressLint("SuspiciousIndentation")
    private fun createQualityDetail() {
        if (pileSelection.isNewLot) {
            qualityDetailList.clear()
            ticketDetailList.clear()
            if (isDryingPlant) {
                ticketNumber = vm.generatePileTicketNumberUsingTallySeq()
            } else {
                ticketNumber = vm.generatePileTicketNumberUsingFGRNSeq()
            }

            val ticketDetail = VegaQualityDetail()
            ticketDetail.nameChar = "NICERTI"
            ticketDetail.qualityParameterValue = ticketNumber
            ticketDetailList.add(ticketDetail)

            if (alreadySelected.size == 1) {
                vm.getLotQualityParams(alreadySelected.get(0).batchNumber, alreadySelected.get(0).materialCode)
            }
        }

    }

    private fun postDelivery() {
        if (getCurrentKey().contains("VEGA_NI")) qualityDetailList.add(qualityDetail)
        if (pileSelection.batchNumber.length == 6) {
            pileSelection.storageLocationCode = storageLocationCode
            pileSelection.weight = totalweight.toString()
            pileSelection.materialCode = materialCode!!
            pileSelection.plantId = plantId
        }
        alreadySelected.forEach {
            it.weight = it.editedWeight
            it.editedWeight = it.weight
        }

        //val count= PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
        var count = 0
        if (PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").isNullOrEmpty()) {
            count = 0
        } else {
            count = PreferenceHelper.get(Constants.SAP_CLOSURE_DAY_COUNT, "0").toInt()
        }

        var postPileQualityDetail = mutableListOf<VegaQualityDetail>()

        if (getCurrentKey().contains("VEGA_NI")) {
            if (pileSelection.isNewLot && alreadySelected.size ==1 ) {
                postPileQualityDetail = qualityDetailList // all quality params of the lot id with new ticket number will post
            }
        }

        if (count != 0 && DateUtils.isFirstDaysOfMonth(requireContext(), count)) {
            UIUtils.showPostingDateDialog(requireContext(), count, object : UIUtils.DialogClick {
                override fun onPositive(date: String) {
                    var postingDate =
                        DateUtils.oneFormatToOtherFormat(date, "dd-MMM-yyyy", "yyyy-MM-dd'T'HH:mm")
                    pileSelection.postingDate = postingDate
                    val postPileRequest = VegaSesamePilePostRequest(
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lots = alreadySelected,
                        pileDetails = pileSelection,
                        qualityDetails = postPileQualityDetail
                    )
                    vm.getPostPile(postPileRequest)
                }
            })
        } else {
            val postPileRequest = VegaSesamePilePostRequest(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                lots = alreadySelected,
                pileDetails = pileSelection,
                qualityDetails = postPileQualityDetail
            )
            vm.getPostPile(postPileRequest)
        }
    }

    private fun updateSuccessUI(response: Resource<GenericReqAndResp<VegaSesamePileSuccessResponse>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    when (it.data?.success) {
                        true -> {
                            isPilePostSuccess = true
                            pileNo = it.data?.data?.pileNo ?: ""

                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                if (pileSelection.isNewLot ) {
                                    //TODO CALL WEIGHTEDAVG API
                                    weightedAvgCall()
                                } else {
                                    vm.getLotQualityParams(pileSelection.batchNumber, pileSelection.materialCode)
                                }
                            } else movetoSuccess(pileNo)

                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    if (getCurrentKey().split("_")[1].contains("NI") && it.error?.contains("Inconsistent characteristic value assignment") == true) {
                        updatePileAndTicketSequence()
                    }
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun movetoSuccess(pileNo: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.pile_created))
        intent.putExtra(AppUtils.SUB_TITLE, "Pile No: ".plus(pileNo))
        if (getCurrentKey().contains("VEGA_NI")) {
            val pileDetails = VegaNicaPileDetails()
            pileDetails.pileNumber = pileNo
            pileDetails.storageLocation = pileSelection.storageLocationCode
            if (qualityDetailList.isNotEmpty()) {
                if (qualityDetailList.filter { it.nameChar == "NIPOSITI" }.isNotEmpty()) {
                    pileDetails.gradeDetails =
                        qualityDetailList.filter { it.nameChar == "NIPOSITI" }.get(0).qualityParameterValue
                    if (qualityGradeWithDescList.isNotEmpty()) {
                       var  pileGradeDetail = pileDetails.gradeDetails.toString().replace("\\s+".toRegex(), " ")
                        qualityGradeWithDescList =
                            qualityGradeWithDescList.filter { it.paramName.replace("\\s+".toRegex(), " ").equals(
                                pileGradeDetail) } as java.util.ArrayList<QualitativeParams>
                        if (qualityGradeWithDescList.isNotEmpty()) pileDetails.gradeDesc =
                            qualityGradeWithDescList[0].paramDesc.toString()
                    }
                }
                if (qualityDetailList.filter { it.nameChar == "NICERTI" }.isNotEmpty()) {
                    pileDetails.ticketNumber =
                        qualityDetailList.filter { it.nameChar == "NICERTI" }.get(0).qualityParameterValue
                }

                pileDetails.receivingDate = DateUtils.getCurrentDate()
                pileDetails.netWeight = totalweight.toString()
                pileDetails.unitsOfMeasure = alreadySelected.get(0).unitOfMeasure
                if (qualityDetailList.filter { it.nameChar == "NIFG0014" }.isNotEmpty()) {
                    pileDetails.certificate =
                        qualityDetailList.filter { it.nameChar == "NIFG0014" }.get(0).qualityParameterValue
                }
                if (qualityDetailList.filter { it.nameChar == "NISACOS" }.isNotEmpty()) {
                    pileDetails.bagCount =
                        qualityDetailList.filter { it.nameChar == "NISACOS" }.get(0).qualityParameterValue
                }

            }
            pileDetails.materialName= pileSelection.materialName
            pileDetails.vendor= alreadySelected.get(0).vendorName
            intent.putExtra(UIUtils.PILE_PRINT_DETAILS, pileDetails)
            intent.putExtra(UIUtils.PRINT_FGRN_TALLYSHEET, true)

        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupAdapter(data: MutableList<VegaCocoaDispatchLots>) {

        binding.rvLotList.setUpAdapter(
            data,
            R.layout.item_pile_management_summary_sesame,
            ItemPileManagementSummarySesameBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvLotId.text = it.batchNumber
                bindItem.tvStLocation.text = it.materialName
                bindItem.tvGradeValue.text = it.storageLocationCode
                bindItem.tvWeightValue.text =
                    it.weight?.toDouble()?.formatThreeDigits().plus(" ").plus(it.unitOfMeasure)
                bindItem.tvWeightToProcessValue.text =
                    it.editedWeight.plus(" ").plus(it.unitOfMeasure)
                bindItem.ivSelect.setOnClickListener {
                    if (getCurrentKey().contains("VEGA_NI")) callBack?.moveToLotEdit(alreadySelected)
                }
            })
    }

    private fun setupPileCardView(data: VegaCocoaDispatchLots) {
        vegaPileSelectionModel = data
        if (data.batchNumber.length == 6) {
            binding.tvMaterialValue.text = "-"
            binding.tvStageValue.text = "-"
            binding.tvProceesValue.text = "-"
            binding.tvBomValue.text = "-"
        } else {
            binding.tvMaterialValue.text = data.batchNumber
            binding.tvStageValue.text = data.vendor
            binding.tvProceesValue.text = data.weight
            binding.tvBomValue.text = data.materialName
        }
        if (binding.tvStageValue.text.isEmpty()) binding.tvStage.gone()
        if (binding.tvProceesValue.text.isEmpty()) binding.tvProcees.gone()
    }

    private fun UpdateBatch(response: Resource<GenericReqAndResp<NicaraguaUpdatePileSequence>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                when (response.data?.success) {
                    true -> {
                        val batchSequence = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")
                        savePileSequence(batchSequence)
                        if (isMillingPlant || isThirdPartyPant) {
                            val batchSequence = PreferenceHelper.get(Constants.FGRN_TALLY_SEQUENCE, "")
                            saveFGRNTallySequence(batchSequence)
                        } else if (isDryingPlant) {
                            val batchSequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
                            saveTallySequence(batchSequence)
                        }

                    }
                    else -> {
                        showErrorDialogWithFAQLink(requireContext(), "${response.data?.message}")
                    }
                }
                movetoSuccess(pileNo)
            }
            Resource.Status.ERROR -> {
                movetoSuccess(pileNo)
            }
            else -> {

            }

        }

    }



    private fun updatePileAndTicketSequence() {
        var isTallySeqeuenceValue = ""
        var isFGRNticketSeqeuenceValue = ""
        var fgrnTicketSequence=""
        var tallySequence=""

        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH) + 1).toString()
        var currentyear = rightNow.get(Calendar.YEAR)

        val year = if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12"))
            (currentyear + 1).toString() else currentyear.toString()

        val prefix1 = ""
        val prefix3 = ""

        var pileBatchSequence = PreferenceHelper.get(Constants.PILE_SEQUENCE, "")
        var pilesequence = ""

        if (pileBatchSequence.isNotEmpty()) {
            val pileSeq = pileBatchSequence.substring(pileBatchSequence.length - 4)
            if (pileSeq.isNotEmpty()) pilesequence = (pileSeq.toInt() - 1).toString()
        }
        when (pilesequence.length) {
            1 -> pileBatchSequence = "000".plus(pilesequence)
            2 -> pileBatchSequence = "00".plus(pilesequence)
            3 -> pileBatchSequence = "0".plus(pilesequence)
            4 -> pileBatchSequence = pilesequence
        }

        if(pileSelection.isNewLot) {

            fgrnTicketSequence = PreferenceHelper.get(Constants.FGRN_TALLY_SEQUENCE, "")
            var fgrnsequence = ""
            if (fgrnTicketSequence.isNotEmpty()) {
                val fgrnSeq = fgrnTicketSequence.substring(fgrnTicketSequence.length - 5)
                if (fgrnSeq.isNotEmpty()) fgrnsequence = (fgrnSeq.toInt()).toString()
            }
            when (fgrnsequence.length) {
                1 -> fgrnTicketSequence = "0000".plus(fgrnsequence)
                2 -> fgrnTicketSequence = "000".plus(fgrnsequence)
                3 -> fgrnTicketSequence = "00".plus(fgrnsequence)
                4 -> fgrnTicketSequence = "0".plus(fgrnsequence)
                5 -> fgrnTicketSequence = fgrnsequence
            }


            tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
            var tallysequence = ""

            if (tallySequence.isNotEmpty()) {
                val talSeq = tallySequence.substring(tallySequence.length - 5)
                if (talSeq.isNotEmpty()) tallysequence = (talSeq.toInt() - 1).toString()
            }
            when (tallysequence.length) {
                1 -> tallySequence = "0000".plus(tallysequence)
                2 -> tallySequence = "000".plus(tallysequence)
                3 -> tallySequence = "00".plus(tallysequence)
                4 -> tallySequence = "0".plus(tallysequence)
                5 -> tallysequence
            }

            if (isDryingPlant) {
                isTallySeqeuenceValue = "Y"
                fgrnTicketSequence = ""
            } else if (isMillingPlant || isThirdPartyPant) {
                tallySequence = ""
                isFGRNticketSeqeuenceValue = "Y"
            }
        }

        val postData = NicaraguaUpdatePileSequence(
            getPlantDetails(),
            prefix1,
            year,
            "",
            tallySequence,
            fgrnTicketSequence,
            pileBatchSequence,
            "Y",
            "",
            "",
            "",
            "",
            "N",
            "N",
            "N",
            isTallySeqeuenceValue,
            isFGRNticketSeqeuenceValue,
            prefix3
        )
        vm.updatePileSequence(postData)

    }

    private fun weightedAvgCall(){
        val list = ArrayList<VegaNigeriaSesameWeightedAverageDeliveryDetail>()

        val qualityList = ArrayList<VegaNigeriaSesameQualityDetails>()
        ticketDetailList.forEach {
            val item = VegaNigeriaSesameQualityDetails()
            item.sapQCName = it.nameChar
            item.sapQCDesc = it.descrChar
            item.satNam = it.qualityParameterValue
            qualityList.add(item)
        }
            val deliveryDetail = VegaNigeriaSesameWeightedAverageDeliveryDetail()
            deliveryDetail.batchNumber = pileNo
            deliveryDetail.materialCode = pileSelection.materialCode
            deliveryDetail.qualityDetails = qualityList
            list.add(deliveryDetail)


        vm.postWeightedAverage(
            (VegaNigeriaSesameWeightedAveragePost(
                key = getCurrentKey(),
                batchUpdateFlag = true,
                weightedAvgFlag = false,
                plant = getPlantDetails(),
                deliveryDetails = list
            ))
        )

    }


}





