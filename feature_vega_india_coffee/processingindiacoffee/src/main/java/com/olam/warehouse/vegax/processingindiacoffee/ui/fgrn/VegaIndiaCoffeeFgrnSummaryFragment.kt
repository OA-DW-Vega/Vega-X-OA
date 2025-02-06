package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

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
import com.olam.warehouse.master.vega.entity.VegaFeatureMaster
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnBagCosumption
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnGradesMatrialWeights
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.model.*
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.Constants.FGRN_TICKET
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.NicaraguaUpdateFgrnSequencePost
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaIndiaCoffeeFgrnSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemVegaIndiaCoffeeBagConsumpSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemVegaIndiaCoffeeFgrnSummaryBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.math.roundToInt


class VegaIndiaCoffeeFgrnSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_fgrn_summary
    private lateinit var binding: FragmentVegaIndiaCoffeeFgrnSummaryBinding
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var fgrnReq = VegaCocoaProcessingFgrnPost()
    private var fgrnItemWithGrades = VegaCocoaFgrnItemWithGrades()
    private var isRoundOff: Boolean = false
    private var isIndexweighmenttype: Boolean = false
    private var batchNumber: String = ""
    private val plantId = getPlantDetails().plantId
    private var plantList = ArrayList<String>()
    private var rspos = ""
    private var rsnum = ""
    private var phase = ""
    private var xchpf = ""
    private var isMillingPlant = false
    private var isDryingPalnt = false
    var paramsNicaraguaQticketList = ArrayList<VegaNicaraguaFgrnQuality>()
    var fgrnTallySequence: String? = ""
    var intent = Intent()
    private var lotQualityList = mutableListOf<VegaQualityParams>()
    private var qualityGradeWithDescList = ArrayList<QualitativeParams>()
    private var isDirect= false
    private var isInDirect= false
    val sapMaterialList = ArrayList<VegaMaterial>()
   // var ticketNumber=""
    var lotMaterialCode=""


    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String, model: VegaCocoaFgrnItems, id: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems) = VegaIndiaCoffeeFgrnSummaryFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaIndiaCoffeeFgrnSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        intent = Intent(requireContext(), SuccessActivity::class.java)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnSummaryFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()
        fgrnTallySequence = PreferenceHelper.get(Constants.FGRN_TALLY_SEQUENCE, "")
        vm.getFgrnItems(fgrnItem.fgrnId)

        vm.getQualityGradeDescList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            qualityGradeWithDescList = java.util.ArrayList(it)
        })

        vm.getQualityGradesListWithDesc()

        vm.fgrnItem.observe(viewLifecycleOwner, Observer { updateItems(it) })

        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.offlineRminItemLocal.observe(viewLifecycleOwner, Observer {
                if (!it.isNullOrEmpty()) {
                    it.forEach {
                        it.rminLot.forEach {
                            if (it.processOrderNum == fgrnItem.processOrderNo) {
                                rspos = it.rspos.toString()
                                rsnum = it.rsnum.toString()
                                phase = it.phase.toString()
                                xchpf = it.xchpf.toString()
                            }
                        }
                    }
                }
            })
            vm.getOfflineRminItem()
        }
        binding.tvLotNo.text = fgrnItem.processOrderNo
        binding.tvShift.text = getString(R.string.shift).plus(" ").plus(fgrnItem.shiftSelection)
        binding.tvOperatorName.text = fgrnItem.operatorName
        binding.tvShift.setOnClickListener { activity?.onBackPressed() }
        vm.getConfigItems(UserRoles.PROCESSING.role)
        binding.tvConfirm.setOnClickListener { showConfirmDialog() }
        vm.postFgrn.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.updateFgrnSequence.observe(viewLifecycleOwner, { UpdateBatch(it) })
        vm.featureMaster.observe(viewLifecycleOwner, Observer { updateFeatureUI(it) })
        vm.getFeatureMaster(Constants.TRACK_TRACE)


        vm.materials.observe(viewLifecycleOwner, Observer {
            sapMaterialList.clear()
            sapMaterialList.addAll(it)
        })
       // if (getCurrentKey().split("_")[1].contains("NI")) {
          //  vm.qualitylist.observe(viewLifecycleOwner, Observer { getQualityList(it) })
       // }

       // if(getCurrentKey().contains("VEGA") && getCurrentKey().contains("NI")) {
         //   vm.getFgrnItems(fgrnItem.fgrnId)

//            fgrnItem.rminList?.forEach {
//                batchNumber = it.batchNumber.toString()
//                lotMaterialCode= it.materialCode?.substring(6).toString()
//            }
//            vm.getPreSamplingQualitydata(batchNumber.trim(), lotMaterialCode)
      //  }
       // vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.configItems.observe(viewLifecycleOwner, Observer {
            updateConfigItems(it)
        })


    }
    private fun updateFeatureUI(it: List<VegaFeatureMaster>?) {
        it?.let {
            it.forEach {
                when (it.featureName) {
                    "Direct" ->isDirect= it.mandatory == true
                    "Indirect" ->isInDirect= it.mandatory == true
                }
            }
        }
        if(isDirect || isInDirect)  vm.fetchMaterials()
    }

//    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
//        response.let {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    hideLoading()
//                    when (it.data?.success) {
//                        true -> {
//                            lotQualityList =
//                                it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
//                            if(lotQualityList.isNotEmpty()) {
//                                val lot = lotQualityList.filter { it.sapQCName == "NICERTI" }
//                                if(!lot.isNullOrEmpty())
//                                ticketNumber = lot[0].satNam.toString()
//                            }
//                            vm.getFgrnItems(fgrnItem.fgrnId)
//                        }
//                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
//                    }
//                }
//                Resource.Status.LOADING -> showLoading()
//                Resource.Status.ERROR -> {
//                    hideLoading()
//                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
//                }
//            }
//        }
//    }


//    private fun getQualityList(params: List<VegaQualityParamsWithQualitative>?) {
//        params?.let {
//            when (it.isNotEmpty()) {
//                true -> {
//                    it.forEach { item ->
//                        if (item.qualityParameter.nameChar == "NICERTI") {
//                            paramsNicaraguaQticketList.add(
//                                prepareTicketQualityPostFGRN(
//                                    fgrnItem,
//                                    item.qualityParameter
//                                )
//                            )
//                        }
//                    }
//                }
//                else -> {}
//            }
//        }
//    }

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCocoaProcessingFgrnResponse>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data

                            if (getCurrentKey().split("_")[1].contains("NI")) {

                                if (plantList.contains(plantId)){ vm.deleteOfflineRminLot(fgrnItem.message.toString()) }
                                postFGRNLotAndTicketSequence()
                            }
                            updateFgrnStatus(success?.get(0)?.messages?.get(0)?.message, 4)
                            moveToSuccessPage(success)
                        }
                        else -> {
                            updateFgrnStatus(it.error.toString(), 3)
                            moveToFailurePage(it.error ?: "")
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                     updateFgrnStatus(it.error.toString(), 3)
                    moveToFailurePage(it.error ?: "")
                }

            }
        }
    }

    private fun updateFgrnStatus(message: String?, status: Int) {
        message?.let { vm.updateFgrnStatus(it, status, fgrnItem.fgrnId) }
    }

    private fun moveToSuccessPage(success: List<VegaCocoaProcessingFgrnResponse>?) {
        val data = success?.map { it.batchNumber }
        val msg = success?.get(0)?.messages?.get(0)?.message
        var message = ""
        data?.forEach { item ->
            if (msg?.contains(item.toString())!!) message = msg.replace(item.toString(), "")
        }
        val tallyKeys = success?.map { it.encodedImageContent } as ArrayList
        val isPrintNeed = tallyKeys.any { it != "" }
        intent.putExtra(AppUtils.TITLE, requireContext().resources.getString(R.string.fgrn_success))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            message.plus(" ").plus(data.toString().replace("[", "").replace("]", ""))
        )
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (batch_no.isNotEmpty()) {
               val gradeName= fgrnReq.qualityDetails?.filter { it.nameCharValue=="NIPOSITI" }?.get(0)?.qualityParameterValue.toString()
                if(qualityGradeWithDescList.isNotEmpty()){
                    qualityGradeWithDescList= qualityGradeWithDescList.filter { it.paramName==gradeName } as ArrayList<QualitativeParams>
                  if(qualityGradeWithDescList.isNotEmpty())
                fgrnReq.qualityParamDesc= qualityGradeWithDescList[0].paramDesc.toString()
                }
                intent.putExtra(UIUtils.PRINT_FGRN_TALLYSHEET, true)
                intent.putExtra(UIUtils.DISPATCH_BATCH, batch_no)
                intent.putExtra(UIUtils.PRINT_FGRN_REQ, fgrnReq)
            }
            intent.putExtra(AppUtils.PRINT_ENABLE, false)
        } else {
            intent.putExtra(AppUtils.PRINT_ENABLE, isPrintNeed)
            intent.putStringArrayListExtra(AppUtils.TALLY_SHEETS, tallyKeys)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun moveToFailurePage(msg: String) {

        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.trans_failed))
        intent.putExtra(AppUtils.SUB_TITLE, msg)
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
//        startActivity(intent)
       startActivity(intent)
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.fgrn_post_confirm)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if(isDryingPalnt) {
                            val postReq =
                                prepareRminFgrnPostRequest(
                                    fgrnItem,
                                    fgrnItemWithGrades.gradeItems,
                                    isRoundOff,
                                    isIndexweighmenttype,
                                    rspos,
                                    rsnum,
                                    phase,
                                    xchpf,
                                    sapMaterialList
                                )
                            vm.postFgrn(postReq)
                            fgrnReq = postReq
                        }else{
                            val postReq =
                                prepareFgrnPostRequest(
                                    fgrnItem,
                                    fgrnItemWithGrades.gradeItems,
                                    isRoundOff,
                                    isIndexweighmenttype,
                                )

                            postReq.qualityDetails = paramsNicaraguaList
                            vm.postFgrn(postReq)
                            fgrnReq = postReq

                        }



                    } else {
                        val postReq =
                            prepareFgrnPostRequest(
                                fgrnItem,
                                fgrnItemWithGrades.gradeItems,
                                isRoundOff,
                                isIndexweighmenttype
                            )
                        vm.postFgrn(postReq)
                    }
                },
                { dismiss() })
        }
    }

    private fun UpdateBatch(response: Resource<GenericReqAndResp<NicaraguaUpdateFgrnSequencePost>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()

                savePostedSequence()

                startActivity(intent)
                requireActivity().finish()
            }
            Resource.Status.ERROR -> {
                savePostedSequence()

                startActivity(intent)
                requireActivity().finish()
            }
            else -> {}
        }
    }

    private fun savePostedSequence(){
        val batchSequence = PreferenceHelper.get(Constants.FGRN_BATCH_SEQUENCE, "")
        saveFgrnBatchSequence(batchSequence)

        if(isMillingPlant) {
            val fgrnTicketSequence = PreferenceHelper.get(Constants.FGRN_TALLY_SEQUENCE, "")
            saveFGRNTallySequence(fgrnTicketSequence)
        }

    }

    private fun updateItems(data: VegaCocoaFgrnItemWithGrades) {
        fgrnItemWithGrades = data
        if (getCurrentKey().split("_")[1].contains("NI")) {
            fgrnItemWithGrades.gradeItems?.forEach {
                var materialCode = it.fgrnGrades.materialCode
                vm.getQualityParams(materialCode, false, "")
            }
        }
        data.gradeItems?.let {
            updateAdapter(it)
        }
        val bagConsumpList = arrayListOf<VegaCocoaFgrnGradesMatrialWeights>()
        data.gradeItems?.forEach { item ->
            val data = item.bagItems?.filter { it.batchNumber.isNotEmpty() }
            if (data != null) {
                bagConsumpList.addAll(data)
            }
        }
        val conData = arrayListOf<VegaCocoaFgrnBagCosumption>()
        var bagType = listOf<String>()
        bagConsumpList.forEach {
            if (bagType.contains(it.bagType)) {
                conData.forEach { item ->
                    if (item.bagType.equals(it.bagType))
                        item.bagCount = (item.bagCount.toInt().plus(it.bagCount.toInt())).toString()
                }
            } else {
                val dat = VegaCocoaFgrnBagCosumption()
                dat.bagCount = it.bagCount
                dat.bagType = it.bagType
                dat.batchNumber = it.batchNumber
                conData.add(dat)
                bagType = conData.map { it.bagType }
            }
        }
        updateBagConsumptionAdapter(conData)
    }

    private fun updateAdapter(gradeItems: List<VegaCocoaFgrnGradesWithBagItems>) {

        binding.rvFgrnSummary.setUpAdapter(
            gradeItems as MutableList,
            R.layout.item_vega_india_coffee_fgrn_summary,
            ItemVegaIndiaCoffeeFgrnSummaryBinding::inflate,
            { it, pos, bindItem ->
                isIndexweighmenttype = it.fgrnGrades.isIndexweighmenttype!!
                isRoundOff = it.fgrnGrades.isRoundOff!!
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
                        item.tareWeight?.toDouble()?.times(item.bagCount.toDouble())!!
                    )
                        .plus(palletAvg!!)
                    if (item.isRoundOff!!) {
                        isRoundOff = item.isRoundOff!!
                    }

                }
                netWeight = grossWeight.minus(tareWeight)
                if (it.fgrnGrades.isRoundOff!!) {
                    bindItem.tvWeightData.text = netWeight.roundToInt().toString().plus(" KG")
                    //  fgrnItem.netWeight = netWeight.roundToInt().toString()

                } else {
                    bindItem.tvWeightData.text = netWeight.formatThreeDigits().plus(" KG")
                }

                bindItem.tvLotId.text = it.fgrnGrades.batchNumber

                bindItem.tvStLocation.text = it.fgrnGrades.lotStorageLocationCode
                bindItem.tvNoOfBag.text =
                    it.bagItems?.sumOf { it1 -> it1.bagCount.toInt() }.toString()
                bindItem.ivEdit.setOnClickListener { view ->
                    if (isIndexweighmenttype && !fgrnItem.gradeList.isNullOrEmpty()) {
                        it.fgrnGrades.grossWeight = fgrnItem.gradeList!!.get(0).grossWeight
                        it.fgrnGrades.tareWeight = fgrnItem.gradeList!!.get(0).tareWeight
                        it.fgrnGrades.netWeight = fgrnItem.gradeList!!.get(0).netWeight
                    }
                    val gson = GsonUtils()
                    val poGrade = gson.toJson(listOf(it.fgrnGrades))
                    activity?.onBackPressed()
                    callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT_EDIT, fgrnItem, poGrade)
                }
                if (it.fgrnGrades.isIndexweighmenttype!!)
                    bindItem.tvWeightData.text = it.fgrnGrades.netWeight.plus(" KG")
            })

    }

    fun factor(n: Number): Boolean {
        return n.toDouble() % 100.0 == 0.0
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        plantList.clear()

        var millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }

        if (!millingPlants.isNullOrEmpty()) {
            isMillingPlant = true
        }


        var dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        if(!dryingPlants.isNullOrEmpty()){
            isDryingPalnt = true
        }

        var plantLists = ""
        dryingPlants.forEach {
            plantLists = it.value?.trim() ?: ""
            if (plantLists.isNotEmpty()) {
                plantList = plantLists.split(",") as ArrayList<String>
            }
        }
    }

    private fun updateBagConsumptionAdapter(bagConsumpList: ArrayList<VegaCocoaFgrnBagCosumption>) {
        if (bagConsumpList.size > 0) {
            binding.rvBagConsumption.visible()
            binding.tvBagConsumption.visible()
        } else {
            binding.rvBagConsumption.gone()
            binding.tvBagConsumption.gone()
        }

        binding.rvBagConsumption.setUpAdapter(
            bagConsumpList,
            R.layout.item_vega_india_coffee_bag_consump_summary,
            ItemVegaIndiaCoffeeBagConsumpSummaryBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvBagTypeValue.text = it.bagType
                bindItem.tvBagCountValue.text = it.bagCount
                bindItem.tvBatchValue.text = it.batchNumber
            })
    }



    private fun postFGRNLotAndTicketSequence(){
        val rightNow = Calendar.getInstance()
        val currentmonth = (rightNow.get(Calendar.MONTH) + 1).toString()
        val currentyear = rightNow.get(Calendar.YEAR)

        val year = if (currentmonth.equals("10") || currentmonth.equals("11") || currentmonth.equals("12"))
            (currentyear + 1).toString() else currentyear.toString()

        val prefix3 = ""
        val prefix1 = getCurrentUserName()


        var fgrnTicketSequence=""
        var isFgrnTicketSeq="N"

        if(isMillingPlant) {
            isFgrnTicketSeq="Y"
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
        }

        var fgrnBatchSequence = PreferenceHelper.get(Constants.FGRN_BATCH_SEQUENCE, "")
        var fgrnBasequence = ""
        if (fgrnBatchSequence.isNotEmpty()) {
            val fgrnBSeq = fgrnBatchSequence.substring(fgrnBatchSequence.length - 5)
            if (fgrnBSeq.isNotEmpty()) fgrnBasequence = (fgrnBSeq.toInt()).toString()
        }
        when (fgrnBasequence.length) {
            1 -> fgrnBatchSequence = "0000".plus(fgrnBasequence)
            2 -> fgrnBatchSequence = "000".plus(fgrnBasequence)
            3 -> fgrnBatchSequence = "00".plus(fgrnBasequence)
            4 -> fgrnBatchSequence = "0".plus(fgrnBasequence)
            5 -> fgrnBatchSequence = fgrnBasequence.toString()
        }




        val postData = NicaraguaUpdateFgrnSequencePost(
            getPlantDetails(),
            prefix1,
            year,
            "",
            fgrnTicketSequence,
            fgrnBatchSequence,
            "N",
            "",
            "",
            "",
            "",
            "N",
            "N",
            "N",
            isFgrnTicketSeq,
            "Y",
            prefix3
        )

        vm.updateFgrnSequence(postData)

    }



//    private fun prepareTicketQualityPostFGRN(
//        poOrderDetails: VegaCocoaFgrnItems?,
//        qualityParameter: VegaQualityParameter
//    ): VegaNicaraguaFgrnQuality {
//        val qualityData = VegaNicaraguaFgrnQuality()
//        fgrnItemWithGrades.gradeItems?.forEach {
//            qualityData.materialCode = it.fgrnGrades.materialCode
//        }
//        qualityData.plantId = getPlantDetails().plantId
//        qualityData.storageLocationCode = storageLocation
//        qualityData.nameCharValue = qualityParameter.nameChar
//        qualityData.descrCharValue = qualityParameter.descrChar.toString()
//
//        qualityData.qualityParameterValue = ticketNumber
//            //vm.generateFGRNTicketNumber()
//        qualityData.batchNumber = batch_no
//        return qualityData
//    }

}
