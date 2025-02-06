package com.olam.warehouse.vegax.qualityapprovenigeria.ui.quality

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.utils.prepareVegaQualityList
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.master.work.convertMtToKg
import com.olam.warehouse.presentation.BuildConfig
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.UIUtils.FROM_CAMEROON_COCOA_QA
import com.olam.warehouse.presentation.utils.UIUtils.FROM_NIGERIA_COCOA_QA
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovenigeria.R
import com.olam.warehouse.vegax.qualityapprovenigeria.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovenigeria.databinding.BottomSheetQaBinding
import com.olam.warehouse.vegax.qualityapprovenigeria.databinding.CutsomDialogQualityApproveNigeriaCocoaLayoutBinding
import com.olam.warehouse.vegax.qualityapprovenigeria.databinding.FragmentVegaQualtyApproveNigeriaBinding
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.OnFragmentQualityApproveNigeriaInteractionListener
import com.olam.warehouse.vegax.qualityapprovenigeria.ui.VegaQualityApproveNigeriaViewModel
import com.olam.warehouse.vegax.qualityapprovenigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaQualityApproveNigeriaFragment : BaseFragment() {

    private val vm: VegaQualityApproveNigeriaViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaQualityApproveNigeria>()
    private var mListener: OnFragmentQualityApproveNigeriaInteractionListener? = null
    private var completeWeighBridgeList = mutableListOf<VegaQualityApproveCameroonWeighBridge>()
    private var mAlertDialog: AlertDialog? = null
    private var callBack: OnParamsListener? = null
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var isData: Boolean? = false
    private var wbDetails = VegaQualityApproveCameroonWeighBridge()
    private var wbPostDetails = VegaQualityWBDetails()
    private var vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
    private var qualityPostList = arrayListOf<VegaQualityApproveCameroonWeighBridge>()
    private var qualityQcPostList = arrayListOf<VegaQualityWBDetails>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var materialNo: String? = ""
    private var mAdapter = VegaNigeriaQualityApproveParamsAdapter { enableProceedBtn(it) }
    private var netWeight: String? = ""
    private var netwgt: String = ""
    private var netQty: String = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var finalApprovalStatus: String? = ""
    private var primaryRefraction: Double = 0.0
    private var secondaryRefraction: Double = 0.0
    private var totalRefraction: Double = 0.0
    private var grnQty: Double = 0.0
    private var paidWeight: Double = 0.0
    private var discountedWeight: Double = 0.0
    private var receivingPlant: String = ""
    private var sendingStorageLocationCode: String = ""
    private var uom: String = ""
    private var mtntWeight: String = ""
    private var mtntNo: String = ""
    private var inspectionLotNo: String = ""
    private var vehicleNumber: String = ""
    private var bagCount: String = ""
    private var bagWeight: String = ""
    private var grossWeight: String = ""
    private var palletTare: String = ""
    private var date: String = ""
    private var otLotNum: String = ""
    private var juteBagCount: String = ""
    private var nylonBagCount: String = ""
    private var palletCount: String = ""
    private var receivingStorageLocationCode: String = ""
    private var defaultTransferLocation = mutableListOf<String>()
    private var plantDetails = Plant()
    private var selectedPlantId = ""
    private var usageDecisionQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var secondaryRefractionQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var primaryRefractionQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var totalRefractionQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var receivingPlantQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var otLotQualityDetails: VegaQualityParameter = VegaQualityParameter()
    private var bagTypeList = mutableListOf<VegaPackageMaterial>()
    private var materialCode: String = ""
    private var receivingPlantName: String = ""
    private var admixtureValue: String = ""
    private var grntPrice: String = ""
    private var openGrntTypeList = ArrayList<String>()
    private var gateEntryOrderList = mutableListOf<VegaNigeriaGateEntryPostData>()
    var vegaMaterialList = mutableListOf<VegaMaterial>()
    private var materialObj = VegaMaterial()

    private lateinit var binding: FragmentVegaQualtyApproveNigeriaBinding

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_nigeria
    private var workFlowData: WorkflowFields? = null


    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaQualtyApproveNigeriaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityapproveNigeria/ui/quality/VegaQualityApproveNigeriaFragment").title("Vega_Nigeria/Approve")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentQualityApproveNigeriaInteractionListener) {
            mListener = context
            callBack = context as OnParamsListener

        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "10")
        /* val wbid = arguments?.get(APPROVE_DATA) as VegaQualityApproveNigeriaWeighBridgeId
         approveQuality =
             arguments?.getParcelableArrayList<VegaQualityApproveNigeria>(APPROVE_QUALITY_DATA) as MutableList<VegaQualityApproveNigeria>
         binding.tvApproveParamsWeighBID.text = wbid.weighBridgeId*/
        vm.getConfigItems(UserRoles.APPROVE.role)

        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridge
        wbPostDetails = arguments?.get(APPROVE_WB_DATA) as VegaQualityWBDetails
        plantDetails = arguments?.get(APPROVE_PLANT_DATA) as Plant
        selectedPlantId = plantDetails.plantId
        materialNo =
            if (!wbDetails.materialNumber?.length?.equals(18)!!) "000000".plus(wbDetails.materialNumber) else wbDetails.materialNumber
        netWeight = wbDetails.grnQty
//        grnQty = wbDetails.grnQty?.toDouble() ?: 0.0
//        tarWeight = wbDetails.bagWeight
        binding.rvApproveQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvApproveQuality.isNestedScrollingEnabled = false
        binding.rvApproveQuality.adapter = mAdapter
        binding.tvApproveParamsWeighBID.text = wbDetails.wbid
        if(wbDetails.qchar == "D")
            binding.tvNegotiateFlag.visibility = View.VISIBLE
        else
            binding.tvNegotiateFlag.visibility = View.GONE

//        challanNo = wbDetails.challan
        vm.getVegaMaterials()
        vm.vegaMaterials.observe(
            viewLifecycleOwner,
            Observer {
                vegaMaterialList = it as MutableList<VegaMaterial>
                if(vegaMaterialList.isNotEmpty()){
                    materialObj = vegaMaterialList.singleOrNull { it.materialCode.takeLast(12).contains(materialNo?.takeLast(12).toString()) }?:VegaMaterial()
                }
            })

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialNumber.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.grnNew.observe(viewLifecycleOwner, Observer { updateGrnUI(it) })

        vm.gateEntryGrnt.observe(viewLifecycleOwner, Observer { updateGRNTType(it) })

        vm.material.observe(viewLifecycleOwner, Observer { bagTypeList = it.toMutableList() })
        vm.getMaterials()

        vm.currentBagIssue.observe(viewLifecycleOwner, Observer { updateCurrentBagsUI(it) })

        vm.quality.observe(viewLifecycleOwner, Observer {
            updateQualityApproval(it)
        })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })

        vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateWeighmentDetails(it) })
        wbDetails.wbid?.let { vm.getMtntWeightDetails(it,wbPostDetails.weighMethod.toString()) }

//        vm.weightbridgeList.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
//        vm.getWeightbridgeListDetails()

        vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
        vm.getQCWeighBridgeList(selectedPlantId)

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })


/*        if (isData!!)
            vm.getQualityParams(materialNo!!, isData, wbDetails.weighBridgeId) // DB Query
        else
            vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString()) //API call*/

//            vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }
        binding.btnReject.setOnClickListener {
                proceedToPost(FNREJECT, R.string.confirm_reject_message)
        }
        binding.btnAccept.setOnClickListener {
            if (secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            /*else if (otLotNum.trim() == "")
                showSnack(getString(R.string.validate_ot_lot))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))*/
            else
                proceedToPost(FNACCEPT, R.string.confirm_quality_message)
        }
//        binding.tvLotIdValue.text = wbDetails.charg
        binding.tvLotIdValue.text = wbDetails.batchNumber
        binding.tvSupplierValue.text = wbDetails.supplierName
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvStorageLocationValue.text = plantDetails.plantId.plus("-").plus(plantDetails.plantName)
        binding.tvGrnQtyValue.text = wbDetails.grnQty?.replace(" ", "")
        binding.tvEudrStatusValue.text= if(materialObj.complainceFlag.equals(Constants.DIRECT))"1" else "0"
        binding.tvSrcLotValue.text= wbDetails.sourceLotId.toString()
        //binding.tvGrnNoValue.text = wbDetails.grnNumber
//        val times = wbDetails.erdat?.split('(', ')')
//        binding.tvDateValue.text = wbDetails.year

        /*times?.get(1).let { it1 ->
        it1?.let { it2 ->
            DateUtils.getUTCDateTime(
                it2,
                App.getAppContext()
            )
        }
    }*/

        binding.tvotLotValue.onChange {
            otLotNum = it
        }

        binding.etSecondaryRefractionValue.onChange {
            println("Roshna => inside onChange")
            calculateQualityParams(it)
        }
        populateReceivingPlantDropDown()
        /*setUpAdapter(approveQuality)*/
        if(isNGCashewEnabled()){
            binding.btnParamsProceedCashew.visibility=View.VISIBLE
            binding.btnAccept.visibility=View.GONE
            binding.btnReject.visibility=View.GONE
        }else{
            binding.btnParamsProceedCashew.visibility=View.GONE
            binding.btnAccept.visibility=View.VISIBLE
            binding.btnReject.visibility=View.VISIBLE
        }
        binding.btnParamsProceedCashew.setOnClickListener { showBottomSheetDialog(requireContext()) }
    }

    private fun calculateQualityParams(secRefraction:String){
        var secondaryRefraction = 0.0
        if (secRefraction.length == 0) {
            secondaryRefraction = 0.0
        } else
            secondaryRefraction = secRefraction.replace(",","").toDouble()

        this.secondaryRefraction = secondaryRefraction


//            if(secondaryRefraction > primaryRefraction)
//                binding.etSecondaryRefractionValue.setError("",null)
        totalRefraction =
            (((primaryRefraction - secondaryRefraction) / grnQty) * 100).formatThreeDigits().toDouble()
        binding.tvTotalRefractionValue.text = totalRefraction.toString().plus(" %")

        var diff = primaryRefraction - secondaryRefraction

        var decimalPart = diff - (diff.toInt())

        if (decimalPart > 0.5) {
            paidWeight = floor(grnQty - (primaryRefraction - secondaryRefraction))
        } else if (decimalPart <= 0.5) {
            paidWeight = ceil(grnQty - (primaryRefraction - secondaryRefraction))
        }
//            paidWeight = (primaryRefraction - secondaryRefraction).formatThreeDigits().toDouble()
        binding.tvPaidWeightValue.text = paidWeight.toString().plus(" KG")

        discountedWeight =
            (((grnQty - (primaryRefraction - secondaryRefraction)) / grnQty) * 100).formatThreeDigits()
                .toDouble()
        binding.tvDiscountedWeightValue.text = discountedWeight.toString().plus(" %")

    }

    private fun updateCurrentBagsUI(data: Resource<GenericReqAndResp<List<VegaNigeriaCurrentBagsIssued>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    if (it.data?.data?.size ?: 0 > 1) {
                        var currentBalance = it.data?.data?.get(1)?.unresConStock
                        /*  bagIssueData.currentBalance = currentBalance
  //                    binding.llBagIssue.visibility = View.VISIBLE
                          clearAll()
                          binding.tvCurrentBagIssuedLabel.text =
                                  getString(R.string.current_bag_issued).plus(" ")
                                          .plus(currentBalance.toString().toDouble().toLong()).plus(" bags")*/
                        var totalWeight: Double? = (currentBalance)?.toDouble()
                        var enteredBags: Double? = (wbDetails.bagCount.toString()).toDouble()
                        if (((totalWeight!!) >= (enteredBags!!))) {
                            wbDetails.storageLocationCode = wbPostDetails.storageLocationCode
                            wbDetails.grntNumber = wbPostDetails.grntNumber
                            val wbData = preparePostGrnData(wbDetails, admixtureValue, bagTypeList)
                            if (AppUtils.isOnline()) {
                                vm.postGrn(
                                        VegaNigeriaGrnPost(
                                                key = getCurrentKey(),
                                                plant = getPlantDetails(),
                                                grnData = listOf(wbData),
                                                qualityDetails = preparePostGrnData1(preQualityList, qualityParameterList),
                                                userName = PreferenceHelper.get(Constants.USER_NAME, ""),
                                                grntNumber = wbDetails.grntNumber.toString(),
                                                autoTransfer = wbDetails.autoTransfer.toString()
                                        )
                                )
                            } else {
                                hideLoading()
                                saveData()
                            }
                        } else {
                            hideLoading()
                            showErrorDialogWithFAQLink(
                                    requireContext(),
                                    getString(R.string.dis_stock_not_available)
                            )
                        }
                    } else {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), getString(R.string.no_data_found))
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }

            }
        }
    }

    private fun updateGRNTType(data: Resource<GenericReqAndResp<List<VegaNigeriaGateEntryPostData>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    openGrntTypeList.clear()
                    //hideLoading()
                    data.data?.data?.let { it1 ->
                        gateEntryOrderList = it1 as MutableList<VegaNigeriaGateEntryPostData>
                    }
                    gateEntryOrderList.forEach { item ->
                        if(item.grnNumber.toString().equals(wbPostDetails.grntNumber)) {
                            if(item.unitsOfMeasure.toString().equals(MT)) {
                                var convertedWeight = convertMtToKg(item.netWeight.toString()).toDouble()
                                grntPrice = (((item.price.toString()).toDouble()) / (convertedWeight)).toString()
                            }else{
                                grntPrice = (((item.price.toString()).toDouble()) / ((item.netWeight.toString()).toDouble())).toString()
                            }
                        }
                    }

                    wbDetails.unitPrice = grntPrice
                    //wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    if( (!(wbDetails.bagType.isNullOrEmpty())) && wbDetails.bagType.equals(JUTE_BAG)){
                        val data =
                                bagTypeList.filter { it.bagType.equals(wbDetails.bagType.toString()) }
                        if (data.isNotEmpty()) {
                            materialCode = data[0].bagMaterialCode
                            //if (wbDetails.materialCode.toString().isNotEmpty() && !wbDetails.supplierCode.isNullOrEmpty() && !wbDetails.storageLocationCode.isNullOrEmpty())
                            vm.getCurrentBagsIssued(
                                    materialCode,
                                    wbDetails.supplierCode.toString(),
                                    wbDetails.storageLocationCode.toString()
                            )
                        }else{
                            showSnack(getString(R.string.bag_type_empty))
                        }
                    }else {
                        val wbData = preparePostGrnData(wbDetails, admixtureValue, bagTypeList)
                        if (AppUtils.isOnline()) {
                            vm.postGrn(
                                    VegaNigeriaGrnPost(
                                            key = getCurrentKey(),
                                            plant = getPlantDetails(),
                                            grnData = listOf(wbData),
                                            qualityDetails = preparePostGrnData1(preQualityList, qualityParameterList),
                                            userName = PreferenceHelper.get(Constants.USER_NAME, ""),
                                            grntNumber = wbDetails.grntNumber.toString(),
                                            autoTransfer = wbDetails.autoTransfer.toString()
                                    )
                            )
                        } else {
                            saveData()
                        }
                    }


                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    //moveToSuccessPage(it.data?.data?.grnNumber.toString())
                    vm.updateGrnNoToQuality(
                            wbDetails.weighBridgeId.toString(),
                            it.data?.data?.grnNumber.toString(),
                            it.data?.data?.batchNumber.toString()
                    )
                    vm.updateGrnSuccess(
                            wbDetails.weighBridgeId.toString(),
                            it.data?.data?.grnNumber.toString(),
                            it.data?.data?.batchNumber.toString(),
                            getString(R.string.grn_success),
                            4
                    )

                    it.data?.data?.weighBridgeId?.let { it1 ->
                        moveToApprovalSuccessPage(it.data?.data?.weighBridgeId!!)
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun saveData() {
        wbDetails.isOfflineData = true
        if (!wbDetails.wbTempId.contains("TMP")) {
            wbDetails.isNotWBID = true
            wbDetails.grnNumber = getTmpId()
        }
      // vm.updateGRNPrice(wbDetails)
        moveToSuccessPage(wbDetails.weighBridgeId.toString(),",","","")
    }

    private fun updateWeightbridgeListUI(list: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        list.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge = it.data?.data
                    if (weighBridge?.size!! > 0) {
                        var completeWeighBridgeList = weighBridge as MutableList<VegaQualityWBDetails>
                        var filteredList = completeWeighBridgeList.filter { it.weighBridgeId == wbDetails.wbid }
                        if (filteredList.size > 0) {
                            bagCount = filteredList[0].bagCount.toString()
                            bagWeight = filteredList[0].bagWeight.toString().toDouble().formatThreeDigits()
                            grossWeight = filteredList[0].grossWeight.toString().toDouble().formatThreeDigits()
                            netwgt = filteredList[0].netWeight.toString()
                            if (bagCount.isEmpty()) bagCount = "0"
                            if (bagWeight.isEmpty()) bagWeight = "0"
                            if (grossWeight.isEmpty()) grossWeight = "0"
                            if (netwgt.isEmpty()) netWeight = "0"
                            if (netQty.isEmpty()) netQty = "0"

                            var netQty1 = netQty
                            if (netQty1.isEmpty()) netQty1 = "0"
                            else netQty1 = netQty.toDouble().formatThreeDigits()

                            palletTare =
                                ((grossWeight.toDouble().formatThreeDigits()
                                    .toDouble()) - (bagWeight.toDouble()
                                    .formatThreeDigits().toDouble()) - (netQty1.toDouble()
                                    .formatThreeDigits()
                                    .toDouble())).toString()
                        }
                        println("")
                    } else {

                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun populateReceivingPlantDropDown() {
        var charValue = mutableListOf<String>()
        charValue.add(0, "Select")
        charValue.add(1, "2731:USRM")
        charValue.add(2, "2764:SARM")
//        defaultTransferLocation.forEach {
//            charValue.add(it)
//        }
//        charValue.addAll(defaultTransferLocation)
        val charValueAdapter =
            this.context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, charValue) }
        binding.spReceivingPlant.adapter = charValueAdapter
        binding.spReceivingPlant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                receivingPlant = charValue[pos]
                receivingStorageLocationCode = charValue[pos].split(":")[0]
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val receivingPlantList = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        if (receivingPlantList != null) {
        }
        val defaultStorageLoc = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        var isExist = false
        val isMaterial =
            defaultStorageLoc?.map { it.materialCode }?.contains(wbDetails.materialNumber?.removeRange(0, 6))
        defaultStorageLoc?.forEach {
            if (wbDetails.materialNumber?.contains(it.materialCode)!! && !it.materialCode.isNullOrEmpty() && !isExist && isMaterial!!) {
                if (it.applicable?.contains("Y")!!) {
//                    defaultTransferLocation = it.value.toString()
                    it.value?.split(",")?.let { it1 -> defaultTransferLocation.addAll(it1) }
                    isExist = true
                }
            } else if (it.materialCode.isNullOrEmpty() && !isExist && !isMaterial!!) {
                if (it.applicable?.contains("Y")!!) {
//                    defaultTransferLocation = it.value.toString()
                    it.value?.split(",")?.let { it1 -> defaultTransferLocation.addAll(it1) }

                    isExist = true
                }
            }
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.nameChar.equals("LOBM_UDCODE")) {
                                usageDecisionQualityDetails = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("B_PRIMARY_REFR")) {
                                primaryRefractionQualityDetails = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("B_SECONDARY_REFR")) {
                                secondaryRefractionQualityDetails = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("B_TOTAL_REFR")) {
                                totalRefractionQualityDetails = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("SOURCE_LOT")) {
                                otLotQualityDetails = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("RECEIVING_PLANT")) {
                                receivingPlantQualityDetails = item.qualityParameter
                            }
                            /* if (item.qualityParameter.vegaMandatory.equals("X") ||
                                 item.qualityParameter.entryObligatory.equals("X") ||
                                 (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                             ) {
                                 if (!item.qualityParameter.nameChar.equals("B_PRIMARY_REFR") && !item.qualityParameter.nameChar.equals(
                                         "B_PAID_WT"
                                     )
                                     && !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals(
                                         "B_GRNQTY1"
                                     )
                                     && !item.qualityParameter.nameChar.equals("B_TOTAL_REFR") && !item.qualityParameter.nameChar.equals(
                                         "B_GRNPRICE1"
                                     )
                                     && !item.qualityParameter.nameChar.equals("LOBM_UDCODE")
                                 )
                                     value.add(item)

                             }*/
                            preQualityList.forEach { item1 ->
                                if (item1.sapQCName == "ZNG_ADMIXTURE") {
                                    admixtureValue = item1.satNam!!
                                }
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue =
                                        item1.satNam!!.replace("%", "").trim()
                                    //.split(" ")[0]
                                    if (!item.qualityParameter.nameChar.equals("B_PRIMARY_REFR") && !item.qualityParameter.nameChar.equals(
                                            "B_PAID_WT"
                                        )
                                        && !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals(
                                            "B_GRNQTY1"
                                        )
                                        && !item.qualityParameter.nameChar.equals("B_TOTAL_REFR") && !item.qualityParameter.nameChar.equals(
                                            "B_GRNPRICE1"
                                        )
                                        && !item.qualityParameter.nameChar.equals("LOBM_UDCODE")
                                    )
                                        value.add(item)
                                }
                            }

                        }
                    } else {
                        it.forEach { item1 ->
                            if (!item1.qualityParameter.nameChar.equals("B_PRIMARY_REFR") && !item1.qualityParameter.nameChar.equals(
                                    "B_PAID_WT"
                                )
                                && !item1.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item1.qualityParameter.nameChar.equals(
                                    "B_GRNQTY1"
                                )
                                && !item1.qualityParameter.nameChar.equals("B_TOTAL_REFR") && !item1.qualityParameter.nameChar.equals(
                                    "B_GRNPRICE1"
                                )
                                && !item1.qualityParameter.nameChar.equals("LOBM_UDCODE")
                            )
                                value.addAll(it)
                        }
                    }


                    /* value.forEach {
                         if(it.qualityParameter.nameChar.equals("B_PRIMARY_REFR"))
                     }*/
                    // && !item1.qualityParameter.nameChar.equals("B_GRNQTY1")
                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo
                    )
                    /*if (preQualityList.size > 0) {
                        binding.tvSecondaryRefraction.text = "Secondary Refraction"
                        binding.llSecondaryRefraction.visibility = View.VISIBLE
                        binding.llReceivingPlant.visibility = View.VISIBLE
                        binding.llotLot.visibility = View.VISIBLE
                        binding.viRefractionDivision.visibility = View.VISIBLE
                        binding.viCalculationDivision.visibility = View.VISIBLE
                        binding.tvPaidWeight.text = "Paid Weight"
                        binding.llpaidWeight.visibility = View.VISIBLE
                        binding.llPrimaryRefraction.visibility = View.VISIBLE
                        binding.tvPrimaryRefraction.text = "Primary Refraction"
                        binding.tvotLotLabel.text = "OT Lot Number"
                        binding.tvTotalRefraction.text = "Total Refraction"
                        binding.llTotalRefraction.visibility = View.VISIBLE
                        binding.tvDiscountedWeight.text = "Discounted Weight"
                        binding.llDiscountedWeight.visibility = View.VISIBLE
                        binding.tvPrimaryRefractionValue.text = "0.0 KG"
                        primaryRefraction = 0.0
                        binding.tvPaidWeightValue.text = "0.0 KG"
                        paidWeight = 0.0
                        binding.tvTotalRefractionValue.text = "0.0 %"
                        totalRefraction = 0.0
                        binding.tvDiscountedWeightValue.text = "0.0 %"
                        discountedWeight = 0.0
                    }*/
                    preQualityList.forEach {
                        when (it.sapQCName) {
                            "B_GRNQTY1" -> {
                                binding.tvGrnQtyValue.text = it.satNam
                                grnQty = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                                binding.etSecondaryRefractionValue.setText("0")
//                                binding.tvSecondaryRefraction.text = "Secondary Refraction"
//                                binding.llSecondaryRefraction.visibility = View.VISIBLE
//                                binding.viRefractionDivision.visibility = View.VISIBLE
//                                binding.viCalculationDivision.visibility = View.VISIBLE
                            }
                            "B_PRIMARY_REFR" -> {

                                binding.tvPrimaryRefractionValue.text =
                                    it.satNam?.replace(",", "").toString().plus(" KG")
                                primaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_SECONDARY_REFR" -> {

                                binding.etSecondaryRefractionValue.setText(
                                    it.satNam?.replace(
                                        ",",
                                        ""
                                    ).toString()
                                )
                                secondaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_PAID_WT" -> {

                                binding.tvPaidWeightValue.text =
                                    it.satNam?.replace(",", "").toString().plus(" KG")
                                paidWeight = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_TOTAL_REFR" -> {

                                binding.tvTotalRefractionValue.text =
                                    it.satNam?.replace(",", "").toString().plus(" %")
                                totalRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "DISCOUNTEDWEIGHT1" -> {

                                binding.tvDiscountedWeightValue.text = it.satNam
                                discountedWeight =
                                    it.satNam?.replace(",", "")?.replace("%", "")?.toDouble() ?: 0.0
                            }
                            "B_NETWEIGHT" -> {
                                var mNetQty = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                                netQty = mNetQty.toString()
                            }
                            "SOURCE_LOT" -> {
                                binding.tvotLotValue.setText(it.satNam.toString())
                                otLotNum = it.satNam.toString()
                                binding.tvotLotValue.isEnabled = it.satNam.toString() == ""
                            }
                            "RECEIVING_PLANT" -> {
                                var charValue = mutableListOf<String>()
                                charValue.add("2731:USRM")
                                charValue.add("2764:SARM")

//                                receivingPlant = it.satNam.toString() // need storage location also?
                                receivingStorageLocationCode = it.satNam.toString()
                                charValue.forEach {
                                    if (it.contains(receivingStorageLocationCode))
                                        receivingPlant = it
                                }
                                binding.tvReceivingPlantValue.text = receivingPlant
                            }
                            "CI_JUTEBAG_CAM" -> {
                                juteBagCount = it.satNam.toString()
                            }
                            "CI_NYLONBAG_CAM" -> {
                                nylonBagCount = it.satNam.toString()
                            }
                            "CI_NOPALLET_CAM" -> {
                                palletCount = it.satNam.toString()
                            }
                        }
                    }
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
       calculateQualityParams(secondaryRefraction.toString())
    }

    private fun updateWeighmentDetails(response: Resource<GenericReqAndResp<VegaNigeriaWeighmentDetails>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            sendingStorageLocationCode = it.data?.data?.storageLocationCode.toString()
                            it.data?.data?.unitsOfMeasure = "MT"
                            uom = it.data?.data?.unitsOfMeasure.toString()
                            mtntNo = it.data?.data?.delivery.toString()
                            mtntWeight = it.data?.data?.appoximateWeight.toString()
                            inspectionLotNo = it.data?.data?.inspectionLotNum.toString()
                            vehicleNumber = it.data?.data?.vehicleNumber.toString()
                            date = it.data?.data?.erdat.toString()

                            binding.tvDateValue.text = DateUtils.getFormatedDate(date)
                        }
                        else -> {
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
                else -> {}
            }
        }
    }

    private fun updateQualityApproval(response: Resource<GenericReqAndResp<VegaNigeriaQualityApprovePostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber,
                                it.data?.data?.encodedImageContent
                            )
                         //   showConfirmApproveDialog(it.data?.data?.currentWbid.toString())
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")

                }
            }
        }
    }

    private fun showConfirmApproveDialog(wbId: String) {

        /*val mDialogView = LayoutInflater.from(activity?.applicationContext)
                .inflate(R.layout.cutsom_dialog_quality_approve_nigeria_cocoa_layout, null)*/
        val mDialogView =
            CutsomDialogQualityApproveNigeriaCocoaLayoutBinding.inflate(LayoutInflater.from(activity?.applicationContext))
        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView.root)
        mAlertDialog = mBuilder?.show()
        mAlertDialog?.setCancelable(false)
        mDialogView.llGrn.gone()
        mDialogView.llApproval.visible()
        mDialogView.tvGrnNo.text =
            getString(R.string.quality_completed).plus("\n").plus(getString(R.string.wb_id))
                .plus(wbId)
        mDialogView.ivClose.gone()

        mDialogView.tvConfirmApproval.setOnClickListener {
            mAlertDialog?.dismiss()
            preQualityList.forEach {
                if(it.sapQCName.equals(RECEIVING_PLANT)){
                    receivingPlantName = it.satNam.toString()
                }

            }
            wbDetails.grntNumber = (wbPostDetails.grntNumber)
            wbDetails.batchNumber = wbDetails.batchNumber

            wbDetails.bagCount = wbPostDetails.bagCount
            wbDetails.bagType = wbPostDetails.bagType
            wbDetails.bagWeight = wbPostDetails.bagWeight
            wbDetails.grossWeight = wbPostDetails.grossWeight
            wbDetails.item =  wbPostDetails.item
            wbDetails.materialCode = wbPostDetails.materialCode
            wbDetails.netWeight = wbPostDetails.netWeight.toString()
            wbDetails.plantId = wbPostDetails.plant
            wbDetails.supplierCode = wbPostDetails.supplierCode
            wbDetails.supplierName = wbPostDetails.supplierName
            wbDetails.storageLocationCode = wbPostDetails.storageLocationCode.toString()
            wbDetails.unitsOfMeasure = MT
            wbDetails.vehicleNumber = wbPostDetails.vehicleNumber
            wbDetails.weighBridgeId = wbPostDetails.weighBridgeId.toString()
            wbDetails.weighBridgeType = wbPostDetails.weighBridgeType
            wbDetails.deliveryItem = wbPostDetails.deliveryItem

            wbDetails.finalApproval = wbPostDetails.finalApproval
           // wbPostDetails.qualityFlag = true
           // wbPostDetails.appName = "BC"
            wbDetails.grnModel = wbPostDetails.grnModel
            wbDetails.procurementType = wbPostDetails.procurementType


            if(wbDetails.procurementType.equals(DD) || wbDetails.procurementType.equals(DX)) {
                wbDetails.autoTransfer = "T"
                wbDetails.recStorageLocation = (wbPostDetails.storageLocationCode)
                wbDetails.recPlant = receivingPlantName
            }else {
                if ((PreferenceHelper.get(Constants.WERKS, "")).equals(wbDetails.plantId.toString())) {
                    wbDetails.autoTransfer = ""
                } else {
                    wbDetails.autoTransfer = "T"
                    wbDetails.recStorageLocation = (wbPostDetails.storageLocationCode)
                    wbDetails.recPlant = receivingPlantName
                }
            }

            vm.openGrntDetails(
                    getCurrentKey(),
                    wbDetails.materialCode.toString(),
                    wbDetails.supplierCode.toString(),
                    selectedPlantId
            )
        }

        mAlertDialog?.show()
    }

    private fun moveToSuccessPage(wbId: String?, charg: String?, grnNo: String?, encodedImageContent: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.EUDR_STATUS, if(materialObj.complainceFlag.equals(Constants.COMPLAINT)) "1" else "0" )

        if (finalApprovalStatus == FNACCEPT) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
            intent.putExtra(AppUtils.SUB_TITLE, wbId)
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "\n WB ID : ".plus(wbId).plus("\n BATCH ID : ").plus(wbDetails.batchNumber)
            )
        } else {
            intent.putExtra(AppUtils.TITLE, getString(R.string.reject_success))
            intent.putExtra(AppUtils.SUB_TITLE, "\n WB ID : ".plus(wbId))
        }

        if (finalApprovalStatus == FNACCEPT) {
            /*
            intent.putExtra(
                            AppUtils.PRINT_ENABLE,
                            (encodedImageContent != null && encodedImageContent != "")
                        )
                        val grnReceipt = ArrayList<String>()
                        grnReceipt.add(encodedImageContent ?: "")
                        intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)
            */


            var totalRefraction = primaryRefraction - secondaryRefraction
            var cleanCocoa = netWeight.toString().toDouble() - totalRefraction
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    wbDetails.batchNumber.toString(),
                    wbDetails.materialNumber.toString(),
                    wbDetails.materialName.toString(),
                    vehicleNumber, //grade  is vehicleNumber
                    palletCount, // certificate
                    receivingPlant, //receiving Plant
                    "",
                    sendingStorageLocationCode, //sending storage location
                    uom,// unit of measurement
                    netQty, //Net Weight
                    wbDetails.grnQty?.replace(" ", ""), //Grn Qty
                    false,
                    false,
                    false,
                    juteBagCount, // processOrderNo
                    totalRefraction.formatThreeDigits(), // Total Refraction(Primary - Secondary)
                    paidWeight.roundToInt().toString(), // Clean Cocoa
                    nylonBagCount, //rsPos
                    inspectionLotNo,//bwart inspection lot
                    mtntWeight.replace(" ", "").plus(" ").plus(uom),//phase mtnt weight
                    mtntNo,//deliveryItem mtnt no
                    grossWeight,// xchpf Gross Weight
                    bagCount, // Bag Count
                    0,
                    false,
                    false,
                    palletTare, // saleOrderId Pallet weight
                    bagWeight // BagWeight

                )
            )
            wbDetails.year = DateUtils.getFormatedDate(date)
            intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
            intent.putExtra(FROM_NIGERIA_COCOA_QA, true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
            intent.putExtra(UIUtils.RECEIVING_DATA, wbDetails)
            intent.putExtra(UIUtils.QUALITY_DATA, vegaBcApprovePostData)

            intent.putExtra(AppUtils.PRINT_ENABLE, true)
        }
        startActivity(intent)
        requireActivity().finish()

    }

/*
    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?,encodedImageContent:String?) {
        val intent = Intent(activity, SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if (finalApprovalStatus == FNACCEPT)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject))
        }
        if(finalApprovalStatus == FNACCEPT){
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "\n WB ID : ".plus(currentWbid).plus("\n LOT ID : ").plus(charg)
            )
        }


*/
/*         if(finalApprovalStatus == FNACCEPT){
             if (charg?.isNotEmpty()!! && !grnNo.isNullOrEmpty())
                 intent.putExtra(
                     AppUtils.SUB_TITLE,
                     getString(R.string.new_lot_id_created).plus("\n LOT No : ").plus(charg).plus("\n GRN No : ").plus(grnNo)
                 )
             else if (charg.isNotEmpty() && grnNo.isNullOrEmpty())
                 intent.putExtra(
                     AppUtils.SUB_TITLE,
                     getString(R.string.new_lot_id_created).plus("\n LOT ID : ").plus(charg).plus("\n WB ID : ").plus(currentWbid)
                 )
             else
                 intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.weigh_bridge_id).plus(currentWbid))
         }*//*



             if(finalApprovalStatus == FNACCEPT) {
                 intent.putExtra(
                     AppUtils.PRINT_ENABLE,
                     (encodedImageContent != null && encodedImageContent != "")
                 )
                 val grnReceipt = ArrayList<String>()
                 grnReceipt.add(encodedImageContent ?: "")
                 intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)

                 val lotlist = ArrayList<VegaCoffeeSalesLots>()
                 lotlist.add(
                     VegaCoffeeSalesLots(
                         "",
                         charg.toString(),
                         wbDetails?.materialNumber.toString(),
                         wbDetails?.materialName.toString(),
                         "",
                         "",
                         "",
                         "",
                         "",
                         wbDetails?.meins,
                         "",
                         wbDetails?.grnQty


                     )
                 )
                 intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
                 intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)

                 intent.putExtra(AppUtils.PRINT_ENABLE, true)
             }
        startActivity(intent)
       activity?.finish()
    }
*/

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaQualityApproveNigeriaResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToApprovalSuccessPage(wbDetails.wbid.toString())
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    //context?.toast(it.error.toString())
                    moveToFailurePage(wbDetails.wbid.toString(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun moveToApprovalSuccessPage(wbId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (finalApprovalStatus == FNACCEPT)
            intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        else
            intent.putExtra(AppUtils.TITLE, getString(R.string.reject_success))

//        intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "\n WB ID : ".plus(wbId).plus("\n BATCH ID : ").plus(wbDetails.batchNumber)
        )

        if (finalApprovalStatus == FNACCEPT) {
            /*intent.putExtra(
                AppUtils.PRINT_ENABLE,
                (encodedImageContent != null && encodedImageContent != "")
            )
            val grnReceipt = ArrayList<String>()
            grnReceipt.add(encodedImageContent ?: "")
            intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)*/

            var totalRefraction = primaryRefraction - secondaryRefraction
            var cleanCocoa = netWeight.toString().toDouble() - totalRefraction
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    wbDetails.batchNumber.toString(),
                    wbDetails.materialNumber.toString(),
                    wbDetails.materialName.toString(),
                    vehicleNumber, //grade  is vehicleNumber
                    palletCount, // certificate
                    receivingPlant, //receiving Plant
                    "",
                    sendingStorageLocationCode, //sending storage location
                    uom,// unit of measurement
                    netQty, //Net Weight
                    wbDetails.grnQty?.replace(" ", ""), //Grn Qty
                    false,
                    false,
                    false,
                    juteBagCount, // processOrderNo
                    totalRefraction.formatThreeDigits(), // Total Refraction(Primary - Secondary)
                    paidWeight.roundToInt().toString(), // Clean Cocoa
                    nylonBagCount, //rsPos
                    inspectionLotNo,//bwart inspection lot
                    mtntWeight.replace(" ", "").plus(" ").plus(uom),//phase mtnt weight
                    mtntNo,//deliveryItem mtnt no
                    grossWeight,// xchpf Gross Weight
                    bagCount, // Bag Count
                    0,
                    false,
                    false,
                    palletTare, // saleOrderId Pallet weight
                    bagWeight // BagWeight


                )
            )
            wbDetails.year = DateUtils.getFormatedDate(date)
            intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
            intent.putExtra(FROM_NIGERIA_COCOA_QA, true)
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
            intent.putExtra(UIUtils.RECEIVING_DATA, wbDetails)
            intent.putExtra(UIUtils.QUALITY_DATA, vegaBcApprovePostData)

            intent.putExtra(AppUtils.PRINT_ENABLE, true)
        }
        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, "Approve Failed")
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        intent.putExtra(AppUtils.MSG, msg)
        startActivity(intent)
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        finalApprovalStatus = finalApproval
        var isValueNeed = true
//        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if ((itValue?.qualityParameter?.vegaValueMandatory.equals("X"))) {
                    if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }
                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
            }
            qualityParameterList.add(itValue?.qualityParameter)
        }
        if (isValueNeed)
            showConfirmDialog(wbDetails.batchNumber.toString(), msg, finalApproval)
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }
/*
    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        */
/*qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (!itValue?.qualityParameter?.preSampling.isNullOrEmpty())
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                        itValue?.qualityParameter?.mandatory = 0
                    } else {
                        isValueNeed = false
                        missedPos.add(index)
                        itValue?.qualityParameter?.mandatory = 1
                    }

                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
//                qualityParameterList.add(itValue?.qualityParameter)
            }
            qualityParameterList.add(itValue?.qualityParameter)
        }*//*


        if (isValueNeed)
            showConfirmDialog(wbDetails.batchNumber.toString(),  msg, finalApproval)
//        showConfirmDialog(batchNo!!, msg, finalApproval)
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
//            mAdapter.updateMissedPos(missedPos, data)
        }
    }
*/


    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {

        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //                callBack?.onParamsProceed(qualityParameterList, wbDetails.wbid, batchNo, finalApproval)
                    confirmBcApproval(finalApproval)
                    postQuality(qualityParameterList, wbDetails.wbid, batchNo, finalApproval)


                    //calculatePaidWeight()
//                mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval)
//                when (finalApproval) {
//                    ACCEPT -> isAccept = true
//                    REJECT -> isAccept = false
//                }
//                postQuality(postType)
                },
                { dismiss() })
        }

    }

    fun confirmBcApproval(finalApproval: String) {
        vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
        vegaBcApprovePostData.weighBridgeId = wbDetails.wbid
        vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
        vegaBcApprovePostData.grnQty = wbDetails.grnQty?.replace(" ", "")
        vegaBcApprovePostData.finalApproval = "X"
        vegaBcApprovePostData.item = wbDetails.item
        vegaBcApprovePostData.plant = wbDetails.plantId
        vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
        vegaBcApprovePostData.materialCode = wbDetails.materialNumber
//        vegaBcApprovePostData.sendingStorageLoc = sendingStorageLocationCode
        vegaBcApprovePostData.sendingStorageLoc = "BFRM"
        vegaBcApprovePostData.uom = "MT"
        vegaBcApprovePostData.receivingStorageLoc = receivingPlant
//        vegaBcApprovePostData.receivingStorageLoc = receivingStorageLocationCode
//        vegaBcApprovePostData.paidWeight = binding.tvPaidWeight.text.toString().split(" ")[0]

//        val messageNav: List<MessageNav> = emptyList()
        var qualityDetails: ArrayList<QualityDetails> = ArrayList()
        qualityParameterList.forEach {
            var quality = QualityDetails()
            quality.descrChar = it?.descrChar
            quality.nameChar = it?.nameChar
            quality.qualityParameterValue = it?.qualityParameterValue
            qualityDetails.add(quality)
        }
        var secondaryQuality = QualityDetails()
        secondaryQuality.descrChar = "Secondary Refraction"
        secondaryQuality.nameChar = "B_SECONDARY_REFR"
        secondaryQuality.qualityParameterValue = secondaryRefraction.toString()
        qualityDetails.add(secondaryQuality)

        var receivingPlantCharacteristics = QualityDetails()
        receivingPlantCharacteristics.descrChar = "Primary Refraction"
        receivingPlantCharacteristics.nameChar = "B_PRIMARY_REFR"
        receivingPlantCharacteristics.qualityParameterValue = primaryRefraction.toString()
        qualityDetails.add(receivingPlantCharacteristics)

        var receivingCharacteristics = QualityDetails()
        receivingCharacteristics.descrChar = "RECEIVING_PLANT"
        receivingCharacteristics.nameChar = "RECEIVING_PLANT"
        receivingCharacteristics.qualityParameterValue = receivingStorageLocationCode
        qualityDetails.add(receivingCharacteristics)

        var sourceLotCharacteristics = QualityDetails()
        sourceLotCharacteristics.descrChar = "SOURCE_LOT"
        sourceLotCharacteristics.nameChar = "SOURCE_LOT"
        sourceLotCharacteristics.qualityParameterValue = otLotNum.trim()
        qualityDetails.add(sourceLotCharacteristics)

        var usageDecision = QualityDetails()
        /*if(finalApproval == FNACCEPT){
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    A"
            vegaBcApprovePostData.autoTransfer = "T"
        } */
//        if (finalApproval == FNREJECT) {
//            usageDecision.descrChar = "Usage Decision"
//            usageDecision.nameChar = "LOBM_UDCODE"
//            usageDecision.qualityParameterValue = "OL-RM    R"
//            vegaBcApprovePostData.autoTransfer = "F"
//        }
//        qualityDetails.add(usageDecision)
        /* var charsNav: ArrayList<CharsNav> = ArrayList()
         qualityParameterList.forEach {
             var char = CharsNav()
             char.Atnam = it?.nameChar
             char.CValue = it?.qualityParameterValue
             char.CDesc = ""
             char.Disp = ""
             if(it?.nameChar?.equals("LOBM_UDCODE")!!){
                 if(finalApproval == FNACCEPT){
                     char.Atnam = it?.nameChar
                     char.CValue = "OL-RM A"
                     char.CDesc = ""
                     char.Disp = ""
                 }
                 else
                 {
                     char.Atnam = it?.nameChar
                     char.CValue = "OL-RM R"
                     char.CDesc = ""
                     char.Disp = ""
                 }
             }

             charsNav.add(char)
         }*/
        vegaBcApprovePostData.qualityDetails = qualityDetails
////        vegaBcApprovePostData.messageNav = messageNav
//        vm.postApproval(
//            PostApprovalData(
//                key = getCurrentKey(),
//                plant = getPlantDetails(),
//                approvalDetails = vegaBcApprovePostData
//            )
//        )
    }

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {

        /*otLotQualityDetails.qualityParameterValue = otLotNum
        otLotQualityDetails.nameChar = "SOURCE_LOT"
        qualityParameter.add(otLotQualityDetails)
        primaryRefractionQualityDetails.qualityParameterValue = primaryRefraction.toString()
        qualityParameter.add(primaryRefractionQualityDetails)
        secondaryRefractionQualityDetails.qualityParameterValue = secondaryRefraction.toString()
        qualityParameter.add(secondaryRefractionQualityDetails)
        totalRefractionQualityDetails.qualityParameterValue = totalRefraction.toString()
//        qualityParameter.add(totalRefractionQualityDetails)
        receivingPlantQualityDetails.qualityParameterValue = receivingPlant.split(":")[0]
        receivingPlantQualityDetails.nameChar = "RECEIVING_PLANT"
        qualityParameter.add(receivingPlantQualityDetails)*/
        /* if (finalApproval == FNREJECT) {
             usageDecisionQualityDetails.qualityParameterValue = "OL-RM    R"
             qualityParameter.add(usageDecisionQualityDetails)
             wbPostDetails.qualityFlag = true
 //            wbPostDetails.qualityFlag = false
         }*/
        var qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            /*wbDetails.qualityDetails = qtyParams as List<VegaQuality>
            wbDetails.batchNumber = batchNo
            wbDetails.finalApproval = "X"
            qualityPostList.clear()
            wbDetails.let { qualityPostList.addAll(listOf(it)) }*/

            wbPostDetails.qualityDetails = prepareVegaQualityList(qtyParams, mAdapter.getItems())
            wbPostDetails.batchNumber = batchNo
            wbPostDetails.finalApproval = finalApproval
            wbPostDetails.unitsOfMeasure = MT
            wbPostDetails.appName = "QC"
            qualityPostList.clear()
            qualityQcPostList.add(wbPostDetails)
//            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }

            /*vm.postQualityParams(
                VegaCameroonQualityApprovePost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )*/
            vm.postQualityParams(
                VegaNigeriaQcPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
                    lotDetails = qualityQcPostList,
                    notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                    nextWorkFlowRole = workFlowData?.workflowRole,
                    navId = workFlowData?.workflowId,
                    currentWorkFlowRole = workFlowData?.module,
                    environment = BuildConfig.BUILD_TYPE
                )
            )
        } else {
            //saveData(qualityParameter, wbId)
//            moveToSuccessPage(this.wbId, "", "")
        }
    }

    private fun updatePreQuality(data: Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbDetails.wbid) }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                    /*it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaQualityApproveNigeria>
                        setUpAdapter(approveQualityList)
                    }*/
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

/*
    private fun setUpAdapter(data: List<VegaQualityApproveNigeria>) {
        data.let {
            approveQuality = it as MutableList<VegaQualityApproveNigeria>
            var i = 0
            binding.rvApproveQuality.setUp(approveQuality.first().qualityParameters as MutableList<VegaQualityApproveNigeriaQualityParams>,
                R.layout.item_vega_bc_approve_Nigeria_params,
                { it, pos ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    tvQualityNameApprove.text = it.sapQCName
                    tvUnitApprove.text = it.satNam
                },
                {

                })
        }
    }
*/


    fun showBottomSheetDialog(context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_qa, null)
        bottomSheetDialog.setContentView(view)

        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        val btnReject = view.findViewById<Button>(R.id.btnReject)
        val btnRecheck = view.findViewById<Button>(R.id.btnRecheck)

        btnAccept.setOnClickListener {
            // Handle Accept button click
            if (secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            /*else if (otLotNum.trim() == "")
                showSnack(getString(R.string.validate_ot_lot))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))*/
            else
                proceedToPost(FNACCEPT, R.string.confirm_quality_message)

            bottomSheetDialog.dismiss()
        }

        btnReject.setOnClickListener {
            // Handle Reject button click
            proceedToPost(FNREJECT, R.string.confirm_reject_message)
            bottomSheetDialog.dismiss()
        }

        btnRecheck.setOnClickListener {
            // Handle Recheck button click
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
