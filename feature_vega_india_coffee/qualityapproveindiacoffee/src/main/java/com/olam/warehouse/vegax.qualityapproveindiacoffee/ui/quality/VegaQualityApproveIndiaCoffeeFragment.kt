package com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.quality

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacameroon.model.QualityDetails
import com.olam.warehouse.master.vegacameroon.model.VegaQualityApproveCameroonPostData
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
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveListener
import com.olam.warehouse.vegax.qualityapproveindiacoffee.R
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.databinding.FragmentVegaQualtyApproveIndiaCoffeeBinding
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.VegaQualityApproveIndiaCoffeeViewModel
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.roundToInt

class VegaQualityApproveIndiaCoffeeFragment : BaseFragment() {

    private val vm: VegaQualityApproveIndiaCoffeeViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaQualityApproveIndiaCoffee>()
    private var mListener: OnFragmentQualityApproveListener? = null
    private var approveQualityList = ArrayList<VegaQualityApproveIndiaCoffee>()
    private var completeWeighBridgeList = mutableListOf<VegaQualityWBDetails>()
    private var mAlertDialog: AlertDialog? = null
    private var callBack: OnParamsListener? = null
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var isData: Boolean? = false
    private var wbDetails = VegaQualityWBDetails()
    private var wbPostDetails = VegaQualityWBDetails()
    private var vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
    private var qualityPostList = arrayListOf<VegaQualityWBDetails>()
    private var qualityQcPostList = arrayListOf<VegaQualityWBDetails>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var materialNo: String? = ""
    private var mAdapter = VegaIndiaCoffeeQualityApproveParamsAdapter { enableProceedBtn(it) }
    private var netWeight: String? = ""
    private var netwgt: String? = ""
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
    private var rejectedBagValue: String = ""
    private var lotToMergeValue: String = ""
    private var acceptedBagValue: String = ""
    private var receivingStorageLocationCode: String = ""
    private var defaultTransferLocation = mutableListOf<String>()
    //private var plantDetails = Plant()
    //private var selectedPlantId = ""
    private var usageDecisionQualityDetails: VegaQualityParameter = VegaQualityParameter()

    private lateinit var binding: FragmentVegaQualtyApproveIndiaCoffeeBinding

    override val layoutResourceId = R.layout.fragment_vega_qualty_approve_india_coffee

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaQualtyApproveIndiaCoffeeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("approve/ui/quality/VegaApproveQualityFragment").title("Approve").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentQualityApproveListener) {
            mListener = context
            callBack = context as OnParamsListener

        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        /* val wbid = arguments?.get(APPROVE_DATA) as VegaQualityWBDetails
         approveQuality =
             arguments?.getParcelableArrayList<VegaQualityApproveCameroon>(APPROVE_QUALITY_DATA) as MutableList<VegaQualityApproveCameroon>
         binding.tvApproveParamsWeighBID.text = wbid.weighBridgeId*/
        vm.getConfigItems(UserRoles.APPROVE.role)

        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityWBDetails
        wbPostDetails = arguments?.get(APPROVE_WB_DATA) as VegaQualityWBDetails
        //plantDetails = arguments?.get(APPROVE_PLANT_DATA) as Plant
       // selectedPlantId = plantDetails.plantId
        materialNo =
            if (!wbDetails.materialCode?.length?.equals(18)!!) "000000".plus(wbDetails.materialCode) else wbDetails.materialCode
        netWeight = wbDetails.netWeight
        grnQty = wbDetails.grossWeight?.toDouble() ?: 0.0
//        tarWeight = wbDetails.bagWeight
        binding.rvApproveQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvApproveQuality.isNestedScrollingEnabled = false
        binding.rvApproveQuality.adapter = mAdapter
        binding.tvApproveParamsWeighBID.text = wbDetails.weighBridgeId

//        challanNo = wbDetails.challan
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.quality.observe(viewLifecycleOwner, Observer {
            try {
                updateQualityApproval(it)
            } catch (e: Exception) {
                println("Roshna => exception")
                e.printStackTrace()
            }
        })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })

        /*vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateWeighmentDetails(it) })
        wbDetails.weighBridgeId?.let { vm.getMtntWeightDetails(it) }*/

        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
        })


        vm.getWeighBridgeIdDetail(wbDetails.weighBridgeId, true)

//        vm.weightbridgeList.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
//        vm.getWeightbridgeListDetails()

        /*vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
        vm.getQCWeighBridgeList(selectedPlantId)*/

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })


/*        if (isData!!)
            vm.getQualityParams(materialNo!!, isData, wbDetails.weighBridgeId) // DB Query
        else
            vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialCode.toString()) //API call*/

//            vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }
        binding.btnReject.setOnClickListener {
            /*if (secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            else if (otLotNum.trim() == "")
                showSnack(getString(R.string.validate_ot_lot))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))
            else*/
                proceedToPost(FNREJECT, R.string.confirm_reject_message)
        }
        binding.btnAccept.setOnClickListener {
            /*if (secondaryRefraction > primaryRefraction)
                showSnack(getString(R.string.validate_secondary_refraction))
            else if (otLotNum.trim() == "")
                showSnack(getString(R.string.validate_ot_lot))
            else if (receivingPlant.equals("") || receivingPlant.equals("Select"))
                showSnack(getString(R.string.validate_receiving_plant))
            else*/
                proceedToPost(FNACCEPT, R.string.confirm_quality_message)
        }
//        binding.tvLotIdValue.text = wbDetails.charg
        binding.tvLotIdValue.text = wbDetails.batchNumber
        binding.tvSupplierValue.text = wbDetails.supplierCode.plus("-").plus(wbDetails.supplierName)
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvPlantValue.text = wbDetails.plant
        binding.tvGrnQtyValue.text = wbDetails.netWeight?.replace(" ", "")
        binding.tvStorageLocationValue.text = wbDetails.storageLocationCode
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


        populateReceivingPlantDropDown()
        /*setUpAdapter(approveQuality)*/
    }

    /*private fun updateWeightbridgeListUI(list: Resource<GenericReqAndResp<List<VegaQualityWBDetails>>>) {
        list.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge = it.data?.data
                    if (weighBridge?.size!! > 0) {
                        var completeWeighBridgeList = weighBridge as MutableList<VegaQualityWBDetails>
                        var filteredList = completeWeighBridgeList.filter { it.weighBridgeId == wbDetails.weighBridgeId }
                        if (filteredList.size > 0) {
                            bagCount = filteredList[0].bagCount.toString()
                            bagWeight = filteredList[0].bagWeight.toString()
                            grossWeight = filteredList[0].grossWeight.toString()
                            netwgt = filteredList[0].netWeight.toString()
                            if (bagCount.isEmpty()) bagCount = "0"
                            if (bagWeight.isEmpty()) bagWeight = "0"
                            if (grossWeight.isEmpty()) grossWeight = "0"
                            if (netwgt!!.isEmpty()) grossWeight = "0"

                            palletTare =
                                (grossWeight.toDouble() - bagWeight.toDouble() - netQty?.toDouble()!!).toString()
                        }
                        println("")
                    } else {

                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }*/

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                /*binding.tvMaterialValue.text =response.data?.data?.materialName
                binding.tvSupplierValue.text =response.data?.data?.supplierName
                binding.tvPlantValue.text =response.data?.data?.plantName
                binding.tvGrnQtyValue.text =response.data?.data?.grossWeight
                binding.tvStorageLocationValue.text =response.data?.data?.storageLocationCode*/
                binding.tvDateValue.text = DateUtils.getFormatedDate(response.data?.data?.erdat.toString())
               /* val times = response.data?.data?.erdat?.split('(', ')')
                binding.tvDateValue.text = times?.get(0).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }*/
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
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
            println("Roshna => ${receivingPlantList.size}")
        }
        val defaultStorageLoc = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        var isExist = false
        val isMaterial =
            defaultStorageLoc?.map { it.materialCode }?.contains(wbDetails.materialCode?.removeRange(0, 6))
        defaultStorageLoc?.forEach {
            if (wbDetails.materialCode?.contains(it.materialCode)!! && !it.materialCode.isNullOrEmpty() && !isExist && isMaterial!!) {
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
                            if (item.qualityParameter.vegaMandatory.equals("X") ||
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

                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.replace("%", "").trim()
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
                   /* if (preQualityList.size > 0) {
                        binding.tvSecondaryRefraction.text = "Secondary Refraction"
                        binding.llSecondaryRefraction.visibility = View.VISIBLE
                        binding.llReceivingPlant.visibility = View.VISIBLE
                        binding.llDropDown.visibility = View.VISIBLE
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
//                                binding.tvSecondaryRefraction.text = "Secondary Refraction"
//                                binding.llSecondaryRefraction.visibility = View.VISIBLE
//                                binding.viRefractionDivision.visibility = View.VISIBLE
//                                binding.viCalculationDivision.visibility = View.VISIBLE
                            }
                            "B_PRIMARY_REFR" -> {

                                binding.tvPrimaryRefractionValue.text = it.satNam.toString().plus(" KG")
                                primaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_PAID_WT" -> {

                                binding.tvPaidWeightValue.text = it.satNam.toString().plus(" KG")
                                paidWeight = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "B_TOTAL_REFR" -> {

                                binding.tvTotalRefractionValue.text = it.satNam.toString().plus(" %")
                                totalRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            "DISCOUNTEDWEIGHT1" -> {

                                binding.tvDiscountedWeightValue.text = it.satNam
                                discountedWeight = it.satNam?.replace(",", "")?.replace("%", "")?.toDouble() ?: 0.0
                            }
                            "B_NETWEIGHT" -> {
                                netQty = it.satNam.toString()
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
                            "Z_REJECTED_BAGS" -> {
                                rejectedBagValue = it.satNam.toString()
                            }
                            "Z_ACCEPTED_BAGS" -> {
                               acceptedBagValue = it.satNam.toString()
                            }
                            "Z_LOT_MERGE" -> {
                                lotToMergeValue = it.satNam.toString()
                            }
                        }
                    }
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun updateWeighmentDetails(response: Resource<GenericReqAndResp<VegaIndiaCoffeeWeighmentDetails>>?) {
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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateQualityApproval(response: Resource<GenericReqAndResp<VegaIndiaCoffeeQualityApprovePostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            println("Roshna on true success")
                            moveToSuccessPage(
                                it.data?.data?.currentWbid)
//                            val batch = it.data?.data?.charg
//                            val msg = it.data?.message
                            //saveWB(it.data?.data?.currentWbid.toString(), batch.toString(), msg.toString(), 4)
                            //saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                            /*                          it.data?.data?.let {
                                                          it.lotDetails?.let {
                                                              it.forEach {
                                                                  it.let { it1 ->
                                                                      lotItems.forEach { it2 ->
                                                                          if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                                              it1.qualityFlag
                                                                      }
                                                                  }
                                                              }
                                                          }
                                                      }*/
                            /* saveWB(
                                 weighBridgeDetails.weighBridgeId.toString(),
                                 batchNo.toString(),
                                 it.data?.message.toString(),
                                 3
                             )
                             saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)*/
                        }
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")

                    //saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    //saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }

    private fun moveToSuccessPage(wbId: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (finalApprovalStatus == FNACCEPT) {
            intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
            intent.putExtra(AppUtils.SUB_TITLE, wbId)
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "\n WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(wbDetails.batchNumber)
            )
        } else
            intent.putExtra(AppUtils.TITLE, getString(R.string.reject_success))

/*
        if (finalApprovalStatus == FNACCEPT) {
            */
/*intent.putExtra(
                AppUtils.PRINT_ENABLE,
                (encodedImageContent != null && encodedImageContent != "")
            )
            val grnReceipt = ArrayList<String>()
            grnReceipt.add(encodedImageContent ?: "")
            intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)*//*


            var totalRefraction = primaryRefraction - secondaryRefraction
            var cleanCocoa = netWeight.toString().toDouble() - totalRefraction
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    wbDetails.batchNumber.toString(),
                    wbDetails?.materialNumber.toString(),
                    wbDetails?.materialName.toString(),
                    vehicleNumber, //grade  is vehicleNumber
                    palletCount, // certificate
                    receivingPlant, //receiving Plant
                    "",
                    sendingStorageLocationCode, //sending storage location
                    uom,// unit of measurement
                    netQty, //Net Weight
                    wbDetails?.grnQty?.replace(" ", ""), //Grn Qty
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
            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
            intent.putExtra(UIUtils.RECEIVING_DATA, wbDetails)
            intent.putExtra(UIUtils.QUALITY_DATA, vegaBcApprovePostData)

            intent.putExtra(AppUtils.PRINT_ENABLE, true)
        }
*/
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
            binding.btnParamsProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaQualityApproveIndiaCoffeeResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    mAlertDialog?.dismiss()
                    moveToApprovalSuccessPage(wbDetails.weighBridgeId.toString())
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    //context?.toast(it.error.toString())
                    moveToFailurePage(wbDetails.weighBridgeId.toString(), it.error.toString())
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
            "\n WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(wbDetails.batchNumber)
        )

        if (finalApprovalStatus == FNACCEPT) {
            /*intent.putExtra(
                AppUtils.PRINT_ENABLE,
                (encodedImageContent != null && encodedImageContent != "")
            )
            val grnReceipt = ArrayList<String>()
            grnReceipt.add(encodedImageContent ?: "")
            intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)*/

            val totalRefraction = primaryRefraction - secondaryRefraction
            val lotlist = ArrayList<VegaCoffeeSalesLots>()
            lotlist.add(
                VegaCoffeeSalesLots(
                    "",
                    wbDetails.batchNumber.toString(),
                    wbDetails.materialCode.toString(),
                    wbDetails.materialName.toString(),
                    vehicleNumber, //grade  is vehicleNumber
                    palletCount, // certificate
                    receivingPlant, //receiving Plant
                    "",
                    sendingStorageLocationCode, //sending storage location
                    uom,// unit of measurement
                    netQty, //Net Weight
                    wbDetails.grossWeight?.replace(" ", ""), //Grn Qty
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
            wbDetails.erdat = DateUtils.getFormatedDate(date)
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
        startActivity(intent)
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        finalApprovalStatus = finalApproval
        var isValueNeed = true
//        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = mAdapter.getItems()
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
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {

//                callBack?.onParamsProceed(qualityParameterList, wbDetails.wbid, batchNo, finalApproval)
                    postQuality(qualityParameterList, batchNo, finalApproval)

//                confirmBcApproval(finalApproval)

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
        /* val mDialogView = LayoutInflater.from(activity?.applicationContext).inflate(R.layout.cutsom_dialog_bc_approve_cameroon_layout, null)
         val mBuilder = AlertDialog.Builder(activity)
             .setView(mDialogView)
         mAlertDialog = mBuilder?.show()
         mAlertDialog?.setCancelable(false)
         mDialogView.llGrn.visible()
         mDialogView.llApproval.gone()
         mDialogView.ivClose.setOnClickListener { mAlertDialog?.dismiss() }
         mDialogView.tvConfirmGrn.setOnClickListener {
             wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
             val wbData = preparePostGrnData(wbDetails)
             vm.postGrn(VegaQualityApproveCameroonGrnPost(key = getCurrentKey(), plant = getPlantDetails(), grnData = listOf(wbData)))
         }
         mAlertDialog?.show()*/
    }

    fun confirmBcApproval(finalApproval: String) {
        vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
        vegaBcApprovePostData.weighBridgeId = wbDetails.weighBridgeId
        vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
        vegaBcApprovePostData.grnQty = wbDetails.grossWeight?.replace(" ", "")
        vegaBcApprovePostData.finalApproval = "X"
        vegaBcApprovePostData.item = wbDetails.item
        vegaBcApprovePostData.plant = wbDetails.storageLocation
        vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
        vegaBcApprovePostData.materialCode = wbDetails.materialCode
//        vegaBcApprovePostData.sendingStorageLoc = sendingStorageLocationCode
        vegaBcApprovePostData.sendingStorageLoc = "BFRM"
        vegaBcApprovePostData.uom = "KG"
        vegaBcApprovePostData.receivingStorageLoc = receivingPlant
//        vegaBcApprovePostData.receivingStorageLoc = receivingStorageLocationCode
//        vegaBcApprovePostData.paidWeight = binding.tvPaidWeight.text.toString().split(" ")[0]

//        val messageNav: List<MessageNav> = emptyList()
        val qualityDetails: ArrayList<QualityDetails> = ArrayList()
        qualityParameterList.forEach {
            var quality = QualityDetails()
            quality.descrChar = it?.descrChar
            quality.nameChar = it?.nameChar
            quality.qualityParameterValue = it?.qualityParameterValue
            qualityDetails.add(quality)
        }
        val secondaryQuality = QualityDetails()
        secondaryQuality.descrChar = "Secondary Refraction"
        secondaryQuality.nameChar = "B_SECONDARY_REFR"
        secondaryQuality.qualityParameterValue = secondaryRefraction.toString()
        qualityDetails.add(secondaryQuality)

        val receivingPlantCharacteristics = QualityDetails()
        receivingPlantCharacteristics.descrChar = "Primary Refraction"
        receivingPlantCharacteristics.nameChar = "B_PRIMARY_REFR"
        receivingPlantCharacteristics.qualityParameterValue = primaryRefraction.toString()
        qualityDetails.add(receivingPlantCharacteristics)

        val receivingCharacteristics = QualityDetails()
        receivingCharacteristics.descrChar = "RECEIVING_PLANT"
        receivingCharacteristics.nameChar = "RECEIVING_PLANT"
        receivingCharacteristics.qualityParameterValue = receivingStorageLocationCode
        qualityDetails.add(receivingCharacteristics)

        val sourceLotCharacteristics = QualityDetails()
        sourceLotCharacteristics.descrChar = "SOURCE_LOT"
        sourceLotCharacteristics.nameChar = "SOURCE_LOT"
        sourceLotCharacteristics.qualityParameterValue = otLotNum.trim()
        qualityDetails.add(sourceLotCharacteristics)

        val usageDecision = QualityDetails()
        /*if(finalApproval == FNACCEPT){
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    A"
            vegaBcApprovePostData.autoTransfer = "T"
        } */
        if (finalApproval == FNREJECT) {
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    R"
            vegaBcApprovePostData.autoTransfer = "F"
        }
        qualityDetails.add(usageDecision)
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
//        vegaBcApprovePostData.messageNav = messageNav
        vm.postApproval(
            PostApprovalData(
                key = getCurrentKey(),
                plant = getPlantDetails(),
                approvalDetails = vegaBcApprovePostData
            )
        )
    }

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        batchNo: String,
        finalApproval: String
    ) {

        if (finalApproval == FNREJECT) {
            usageDecisionQualityDetails.descrChar = "Usage Decision"
            usageDecisionQualityDetails.nameChar = "LOBM_UDCODE"
            usageDecisionQualityDetails.qualityParameterValue = "01       R"
            qualityParameter.add(usageDecisionQualityDetails)
        }
        if (finalApproval == FNACCEPT) {
            usageDecisionQualityDetails.descrChar = "Usage Decision"
            usageDecisionQualityDetails.nameChar = "LOBM_UDCODE"
            usageDecisionQualityDetails.qualityParameterValue
            usageDecisionQualityDetails.qualityParameterValue = "01       A"
            qualityParameter.add(usageDecisionQualityDetails)
        }
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            /*wbDetails.qualityDetails = qtyParams as List<VegaQuality>
            wbDetails.batchNumber = batchNo
            wbDetails.finalApproval = "X"
            qualityPostList.clear()
            wbDetails.let { qualityPostList.addAll(listOf(it)) }*/

            wbPostDetails.qualityDetails = qtyParams as List<VegaQuality>
            wbPostDetails.batchNumber = batchNo
            wbPostDetails.finalApproval = "X"
            wbPostDetails.qualityFlag=true
            wbPostDetails.appName = "QC"
            wbPostDetails.LOBM_UDCODE = "01       A"
            qualityPostList.clear()
            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }

            /*vm.postQualityParams(
                VegaCameroonQualityApprovePost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityPostList
                )
            )*/
            vm.postQualityParams(
                VegaCameroonQcPost(
                    key = getCurrentKey(),
                    plant = getPlantDetails(),
                    lotDetails = qualityQcPostList
                )
            )
        } else {
            //saveData(qualityParameter, wbId)
//            moveToSuccessPage(this.wbId, "", "")
        }
    }

    private fun updatePreQuality(data: Resource<GenericReqAndResp<List<VegaQualityApproveIndiaCoffee>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbDetails.weighBridgeId) }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                    /*it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList = it1 as ArrayList<VegaQualityApproveCameroon>
                        setUpAdapter(approveQualityList)
                    }*/
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
            }
        }
    }

/*
    private fun setUpAdapter(data: List<VegaQualityApproveCameroon>) {
        data.let {
            approveQuality = it as MutableList<VegaQualityApproveCameroon>
            var i = 0
            binding.rvApproveQuality.setUp(approveQuality.first().qualityParameters as MutableList<VegaQualityApproveCameroonQualityParams>,
                R.layout.item_vega_bc_approve_cameroon_params,
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


}
