package com.olam.warehouse.vegax.bcapprovecameroon.ui.quality

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaConfigDetails
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonWeighBridge
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.FROM_CAMEROON_COCOA_QA
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.bcapprovecameroon.R
import com.olam.warehouse.vegax.bcapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.bcapprovecameroon.databinding.FragmentVegaBcApproveCameroonModuleBinding
import com.olam.warehouse.vegax.bcapprovecameroon.ui.OnFragmentBcApproveCameroonInteractionListener
import com.olam.warehouse.vegax.bcapprovecameroon.ui.VegaBcApproveCameroonViewModel
import com.olam.warehouse.vegax.bcapprovecameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaBcApproveCameroonFragment : BaseFragment() {

    private val vm: VegaBcApproveCameroonViewModel by viewModel()
    private var mListener: OnFragmentBcApproveCameroonInteractionListener? = null
    private var mAlertDialog: AlertDialog? = null
    private var callBack: OnParamsListener? = null
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var isData: Boolean? = false
    private var wbDetails = VegaQualityApproveCameroonWeighBridge()
    private var vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var materialNo: String? = ""
    private var mAdapter = VegaCameroonBcApproveParamsAdapter { enableProceedBtn(it) }
    private var netWeight: String? = ""
    private var netQty: String? = ""
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
    private var bagCount: String = ""
    private var bagWeight: String = ""
    private var grossWeight: String = ""
    private var palletTare: String = ""
    private var date: String = ""
    private var receivingStorageLocationCode: String = ""
    private var defaultTransferLocation = mutableListOf<String>()

    private lateinit var binding: FragmentVegaBcApproveCameroonModuleBinding

    override val layoutResourceId = R.layout.fragment_vega_bc_approve_cameroon_module

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaBcApproveCameroonModuleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("bcapprovecameroon/ui/quality/VegaBcApproveCameroonFragment").title("Vega_Cameroon/Approve").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentBcApproveCameroonInteractionListener) {
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
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
            getActionBtnChangedView(binding.btnOkApprove, it, true)
            getActionBtnChangedView(binding.btnAccept, it, true)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
        }
        vm.getConfigItems(UserRoles.APPROVE.role)

        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridge
        materialNo =
            if (!wbDetails.materialNumber?.length?.equals(18)!!) "000000".plus(wbDetails.materialNumber) else wbDetails.materialNumber
        netWeight = wbDetails.grnQty

        binding.rvApproveQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvApproveQuality.isNestedScrollingEnabled = false
        binding.rvApproveQuality.adapter = mAdapter
        binding.tvApproveParamsWeighBID.text = wbDetails.wbid

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialNumber.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it) })

        vm.quality.observe(viewLifecycleOwner, Observer { updateQualityApproval(it) })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })

        vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateWeighmentDetails(it)  })
        wbDetails.wbid?.let { vm.getMtntWeightDetails(it) }

        vm.weightbridgeList.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
        vm.getWeightbridgeListDetails()

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })




        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }

        binding.btnReject.setOnClickListener{
            if(secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))
            else
                proceedToPost(FNREJECT,R.string.confirm_reject_message)
        }
        binding.btnAccept.setOnClickListener{
            if(secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))
            else
                proceedToPost(FNACCEPT, R.string.confirm_quality_message)
        }
        binding.tvLotIdValue.text = wbDetails.batchNumber
        binding.tvSupplierValue.text = wbDetails.supplierName
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvStorageLocationValue.text = wbDetails.plantDesc
        binding.tvGrnQtyValue.text = wbDetails.grnQty?.replace(" ","")
        binding.tvGrnNoValue.text = wbDetails.grnNumber

        populateReceivingPlantDropDown()
    }

    private fun updateWeightbridgeListUI(list: Resource<GenericReqAndResp<List<VegaCameroonWeighmentDetails>>>) {
        list.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge = it.data?.data
                    if (weighBridge?.size!! > 0) {
                        var completeWeighBridgeList = weighBridge as MutableList<VegaCameroonWeighmentDetails>
                        var filteredList = completeWeighBridgeList.filter { it.weighBridgeId == wbDetails.wbid }
                        if(filteredList.size > 0){
                            bagCount = filteredList[0].bagCount.toString()
                            bagWeight = filteredList[0].bagWeight.toString()
                            grossWeight = filteredList[0].grossWeight.toString()
                            netWeight = filteredList[0].netWeight.toString()
                            palletTare =
                                (grossWeight.toDouble() - bagWeight.toDouble() - netWeight?.toDouble()!!).toString()
                        }
                        println("")
                    }
                    else {

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

    private fun populateReceivingPlantDropDown(){
        var charValue = mutableListOf<String>()
        charValue.add(0,"Select" )
        charValue.add(1, "2731:USRM")
        charValue.add(2, "2764:SARM")

        val charValueAdapter =
            this.context?.let { ArrayAdapter(it,android.R.layout.simple_list_item_1,charValue) }
        binding.spReceivingPlant.adapter = charValueAdapter
        binding.spReceivingPlant.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                receivingPlant = charValue[pos]
                receivingStorageLocationCode = charValue[pos].split(":")[0]
            }
        }
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val defaultStorageLoc = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        var isExist = false
        val isMaterial = defaultStorageLoc?.map { it.materialCode }?.contains(wbDetails.materialNumber?.removeRange(0, 6))
        defaultStorageLoc?.forEach {
            if (wbDetails.materialNumber?.contains(it.materialCode)!! && !it.materialCode.isNullOrEmpty() && !isExist && isMaterial!!) {
                if (it.applicable?.contains("Y")!!) {
                    it.value?.split(",")?.let { it1 -> defaultTransferLocation.addAll(it1) }
                    isExist = true
                }
            } else if (it.materialCode.isNullOrEmpty() && !isExist && !isMaterial!!) {
                if (it.applicable?.contains("Y")!!) {
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
                            if (item.qualityParameter.vegaMandatory.equals("X") ||
                                item.qualityParameter.entryObligatory.equals("X") ||
                                (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {

                                if( !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                    &&  !item.qualityParameter.nameChar.equals("B_GRNQTY1") && !item.qualityParameter.nameChar.equals("LOBM_UDCODE"))
                                    value.add(item)

                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.replace("%","").trim()

                                    if( !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                        &&  !item.qualityParameter.nameChar.equals("B_GRNQTY1") && !item.qualityParameter.nameChar.equals("LOBM_UDCODE"))
                                        value.add(item)
                                }
                            }

                        }
                    } else {
                        it.forEach { item1 ->

                            if( !item1.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item1.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                &&  !item1.qualityParameter.nameChar.equals("B_GRNQTY1") && !item1.qualityParameter.nameChar.equals("LOBM_UDCODE"))
                                value.addAll(it)
                        }
                    }



                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo
                    )
                    if(preQualityList.size > 0) {

                    }
                    preQualityList.forEach{
                        when(it.sapQCName){
                            "B_GRNQTY1" -> {
                                binding.tvGrnQtyValue.text = it.satNam
                                grnQty = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
//
                            }
                            "B_PRIMARY_REFR" -> {

                                binding.tvPrimaryRefractionValue.text =
                                    it.satNam.toString().plus(" KG")
                                primaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_PAID_WT" ->{

                                binding.tvPaidWeightValue.text = it.satNam.toString().plus(" KG")
                                paidWeight = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_TOTAL_REFR" -> {

                                binding.tvTotalRefractionValue.text =
                                    it.satNam.toString().plus(" %")
                                totalRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "DISCOUNTEDWEIGHT1" -> {

                                binding.tvDiscountedWeightValue.text = it.satNam
                                discountedWeight =
                                    it.satNam?.replace(",", "")?.replace("%", "")?.toDouble() ?: 0.0
                            }
                            "B_NETWEIGHT" ->
                            {
                                netQty = it.satNam.toString()
                            }

                        }
                    }
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun updateWeighmentDetails(response: Resource<GenericReqAndResp<VegaCameroonWeighmentDetails>>?) {
        response.let {
            when (it?.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            sendingStorageLocationCode = it.data?.data?.storageLocationCode.toString()
                            uom = it.data?.data?.unitsOfMeasure.toString()
                            mtntNo = it.data?.data?.delivery.toString()
                            mtntWeight = it.data?.data?.appoximateWeight.toString()
                            inspectionLotNo = it.data?.data?.inspectionLotNum.toString()
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

    private fun updateQualityApproval(response: Resource<GenericReqAndResp<VegaCameroonQualityApprovePostResponse>>) {
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
                         wbDetails.materialNumber.toString(),
                         wbDetails.materialName.toString(),
                         "",
                         "",
                         "",
                         "",
                         "",
                         wbDetails.meins,
                         "",
                         wbDetails.grnQty


                     )
                 )
                 intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
                 intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)

                 intent.putExtra(AppUtils.PRINT_ENABLE, true)
             }
        startActivity(intent)
       activity?.finish()
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaBcApproveCameroonResponse>>?) {
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

        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "\n WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(wbDetails.batchNumber)
        )

        if(finalApprovalStatus == FNACCEPT) {


            var totalRefraction = primaryRefraction - secondaryRefraction
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    wbDetails.batchNumber.toString(),
                    wbDetails.materialNumber.toString(),
                    wbDetails.materialName.toString(),
                    "",
                    "",
                    receivingPlant, //receiving Plant
                    "",
                    sendingStorageLocationCode, //sending storage location
                    wbDetails.meins,
                    netQty, //Net Weight
                    wbDetails.grnQty, //Grn Qty
                    false,
                    false,
                    false,
                    "",
                    totalRefraction.toString(), // Total Refraction(Primary - Secondary)
                    paidWeight.toString(), // Clean Cocoa
                    "",
                    inspectionLotNo,//bwart inspection lot
                    mtntWeight,//phase mtnt weight
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
            intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
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
       qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if((itValue?.qualityParameter?.vegaValueMandatory.equals("X"))){
                    if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }
                }
                else {
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



    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {

        MaterialDialog(requireContext()).show {
            message(msg)

            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    confirmBcApproval(finalApproval)
                },
                { dismiss() })
        }

    }

    fun confirmBcApproval(finalApproval:String){
        vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
        vegaBcApprovePostData.weighBridgeId = wbDetails.wbid
        vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
        vegaBcApprovePostData.grnQty = wbDetails.grnQty?.replace(" ","")
        vegaBcApprovePostData.finalApproval = "X"
        vegaBcApprovePostData.item = wbDetails.item
        vegaBcApprovePostData.plant = wbDetails.plantId
        vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
        vegaBcApprovePostData.materialCode = wbDetails.materialNumber
        vegaBcApprovePostData.sendingStorageLoc = "BFRM"
        vegaBcApprovePostData.uom = "KG"
        vegaBcApprovePostData.receivingStorageLoc = receivingPlant

        var qualityDetails :ArrayList<QualityDetails> = ArrayList()
        qualityParameterList.forEach{
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

        var usageDecision = QualityDetails()
        if(finalApproval == FNACCEPT){
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    A"
            vegaBcApprovePostData.autoTransfer = "T"
        } else {
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    R"
            vegaBcApprovePostData.autoTransfer = "F"
        }
        qualityDetails.add(usageDecision)

        vegaBcApprovePostData.qualityDetails = qualityDetails
        vm.postApproval(
            PostApprovalData(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                approvalDetails = vegaBcApprovePostData
            )
        )
    }



    private fun updatePreQuality(data: Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>) {
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

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }

            }
        }
    }





}
