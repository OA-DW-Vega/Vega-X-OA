package com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.quality

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
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.*
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
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveListener
import com.olam.warehouse.vegax.qualityapproveindiacoffee.R
import com.olam.warehouse.vegax.qualityapproveindiacoffee.data.domain.model.*
import com.olam.warehouse.vegax.qualityapproveindiacoffee.databinding.FragmentVegaBcApproveIndiaCoffeeBinding
import com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.VegaQualityApproveIndiaCoffeeViewModel
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.*
import kotlinx.android.synthetic.main.fragment_vega_qualty_approve_india_coffee.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import kotlin.math.ceil
import kotlin.math.floor

class VegaBcApproveIndiaCoffeeReadFragment : BaseFragment() {

    private val vm: VegaQualityApproveIndiaCoffeeViewModel by viewModel()
    private var approveQuality = mutableListOf<VegaQualityApproveIndiaCoffee>()
    private var mListener: OnFragmentQualityApproveListener? = null
    private var approveQualityList = ArrayList<VegaQualityApproveIndiaCoffee>()
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
    private var mAdapter = VegaIndiaCoffeeQualityApproveParamsAdapter { enableProceedBtn(it) }
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
    private var otLotNum: String = ""
    private var receivingStorageLocationCode: String = ""
    private var defaultTransferLocation = mutableListOf<String>()
    private var selectedPlantId = ""
    private var plantDetails = Plant()

    private lateinit var binding: FragmentVegaBcApproveIndiaCoffeeBinding

    override val layoutResourceId = R.layout.fragment_vega_bc_approve_india_coffee

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaBcApproveIndiaCoffeeBinding.inflate(layoutInflater)
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
//            callBack = context as OnParamsListener

        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {
        /* val wbid = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridgeId
         approveQuality =
             arguments?.getParcelableArrayList<VegaQualityApproveCameroon>(APPROVE_QUALITY_DATA) as MutableList<VegaQualityApproveCameroon>
         binding.tvApproveParamsWeighBID.text = wbid.weighBridgeId*/
        vm.getConfigItems(UserRoles.APPROVE.role)

        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridge
//        wbPostDetails = arguments?.get(APPROVE_WB_DATA) as VegaQualityWBDetails
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

//        challanNo = wbDetails.challan
        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialNumber.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.quality.observe(viewLifecycleOwner, Observer { updateQualityApproval(it) })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })

        vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateWeighmentDetails(it) })
        wbDetails.wbid?.let { vm.getMtntWeightDetails(it) }

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
        binding.btnNegotiate.setOnClickListener {
            proceedToPost(FNNEGOTIATE, R.string.confirm_reject_message)
        }
        binding.btnAccept.setOnClickListener {
            proceedToPost(FNACCEPT, R.string.confirm_quality_message)
        }
//        binding.tvLotIdValue.text = wbDetails.charg
        binding.tvLotIdValue.text = wbDetails.batchNumber
        binding.tvSupplierValue.text = wbDetails.supplierCode.plus("-").plus(wbDetails.supplierName)
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvStorageLocationValue.text = wbDetails.plantDesc
        binding.tvGrnQtyValue.text = wbDetails.grnQty?.replace(" ", "")
        binding.tvGrnNoValue.text = wbDetails.grnNumber
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


        binding.etSecondaryRefractionValue.onChange {
            var secondaryRefraction = 0.0
            if (it.length == 0) {
                secondaryRefraction = 0.0
            } else
                secondaryRefraction = it.toDouble()

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
            tvPaidWeightValue.text = paidWeight.toString().plus(" KG")

            discountedWeight =
                (((grnQty - (primaryRefraction - secondaryRefraction)) / grnQty) * 100).formatThreeDigits().toDouble()
            tvDiscountedWeightValue.text = discountedWeight.toString().plus(" %")
        }

        populateReceivingPlantDropDown()
        /*setUpAdapter(approveQuality)*/
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
                        wbPostDetails = filteredList.single()
                        if (filteredList.size > 0) {
                            bagCount = filteredList[0].bagCount.toString()
                            bagWeight = filteredList[0].bagWeight.toString()
                            grossWeight = filteredList[0].grossWeight.toString()
                            netWeight = filteredList[0].netWeight.toString()
                            palletTare =
                                (grossWeight.toDouble() - bagWeight.toDouble() - netWeight?.toDouble()!!).toString()
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
                                /*if( !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                    &&  !item.qualityParameter.nameChar.equals("B_GRNQTY1") && !item.qualityParameter.nameChar.equals("LOBM_UDCODE"))
*/
                                //value.add(item)

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
/*
                                    if( !item.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                        &&  !item.qualityParameter.nameChar.equals("B_GRNQTY1") && !item.qualityParameter.nameChar.equals("LOBM_UDCODE"))
*/
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
                            /*if( !item1.qualityParameter.nameChar.equals("B_SECONDARY_REFR") && !item1.qualityParameter.nameChar.equals("B_GRNPRICE1")
                                &&  !item1.qualityParameter.nameChar.equals("B_GRNQTY1") && !item1.qualityParameter.nameChar.equals("LOBM_UDCODE"))
*/
                            //value.addAll(it)
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
                    if (preQualityList.size > 0) {
                        /*binding.tvSecondaryRefraction.text = "Secondary Refraction"
                        binding.llSecondaryRefraction.visibility = View.VISIBLE*/
                        binding.llReceivingPlant.visibility = View.VISIBLE
//                        binding.llotLot.visibility = View.VISIBLE
                        binding.llReceivingPlant.visibility = View.VISIBLE
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
                    }
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
                                binding.tvotLotValue.text = it.satNam.toString()
                                otLotNum = it.satNam.toString()
                                if (it.satNam.toString() == "")
                                    binding.llotLot.visibility = View.GONE
                                else
                                    binding.llotLot.visibility = View.VISIBLE

//                                binding.tvotLotValue.isEnabled = it.satNam.toString() == ""
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
                            date = it.data?.data?.erdat.toString()
                            println("Roshna erdat => $date")

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
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber,
                                it.data?.data?.encodedImageContent
                            )
                            val batch = it.data?.data?.charg
                            val msg = it.data?.message
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

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?, encodedImageContent: String?) {
        val intent = Intent(activity, SuccessActivity::class.java)
        if (AppUtils.isOnline()) {
            if (finalApprovalStatus == FNACCEPT)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_negotiate))
        }
        if (finalApprovalStatus == FNACCEPT) {
            intent.putExtra(
                AppUtils.SUB_TITLE,
                "\n WB ID : ".plus(currentWbid).plus("\n LOT ID : ").plus(charg)
            )
        }


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
         }*/


        if (finalApprovalStatus == FNACCEPT) {
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
            "\n WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(wbDetails.batchNumber)
        )

//        if (finalApprovalStatus == FNACCEPT) {
//            /*intent.putExtra(
//                AppUtils.PRINT_ENABLE,
//                (encodedImageContent != null && encodedImageContent != "")
//            )
//            val grnReceipt = ArrayList<String>()
//            grnReceipt.add(encodedImageContent ?: "")
//            intent.putStringArrayListExtra(AppUtils.GRN_DOCUMENT, grnReceipt)*/
//
//            var totalRefraction = primaryRefraction - secondaryRefraction
//            var cleanCocoa = netWeight.toString().toDouble() - totalRefraction
//            val lotlist = ArrayList<VegaCoffeeSalesLots>()
//            lotlist.add(
//                VegaCoffeeSalesLots(
//                    "",
//                    wbDetails.batchNumber.toString(),
//                    wbDetails?.materialNumber.toString(),
//                    wbDetails?.materialName.toString(),
//                    "",
//                    "",
//                    receivingPlant, //receiving Plant
//                    "",
//                    sendingStorageLocationCode, //sending storage location
//                    wbDetails?.meins,
//                    netQty, //Net Weight
//                    wbDetails?.grnQty, //Grn Qty
//                    false,
//                    false,
//                    false,
//                    "",
//                    totalRefraction.toString(), // Total Refraction(Primary - Secondary)
//                    paidWeight.toString(), // Clean Cocoa
//                    "",
//                    inspectionLotNo,//bwart inspection lot
//                    mtntWeight,//phase mtnt weight
//                    mtntNo,//deliveryItem mtnt no
//                    grossWeight,// xchpf Gross Weight
//                    bagCount, // Bag Count
//                    0,
//                    false,
//                    false,
//                    palletTare, // saleOrderId Pallet weight
//                    bagWeight // BagWeight
//
//
//                )
//            )
//            intent.putExtra(FROM_CAMEROON_COCOA_QA, true)
//            intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
//            intent.putExtra(UIUtils.RECEIVING_DATA, wbDetails)
//            intent.putExtra(UIUtils.QUALITY_DATA, vegaBcApprovePostData)
//
//            intent.putExtra(AppUtils.PRINT_ENABLE, true)
//        }
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
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    /*callBack?.onParamsProceed(qualityParameterList, wbDetails.wbid, batchNo, finalApproval)
    postQuality(qualityParameterList, wbDetails.wbid, batchNo, finalApproval)*/

                    if (finalApproval == FNACCEPT)
                        confirmBcApproval(finalApproval)
                    else if (finalApproval == FNNEGOTIATE)
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
        vegaBcApprovePostData.weighBridgeId = wbDetails.wbid
        vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
        vegaBcApprovePostData.grnQty = wbDetails.grnQty?.replace(" ", "")
        vegaBcApprovePostData.finalApproval = finalApproval
        vegaBcApprovePostData.item = wbDetails.item
        vegaBcApprovePostData.plant = wbDetails.plantId
        vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
        vegaBcApprovePostData.materialCode = wbDetails.materialNumber
//        vegaBcApprovePostData.sendingStorageLoc = sendingStorageLocationCode
        vegaBcApprovePostData.sendingStorageLoc = "BFRM"
        vegaBcApprovePostData.uom = "KG"
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

        var usageDecision = QualityDetails()
        if (finalApproval == FNACCEPT) {
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    A"
            vegaBcApprovePostData.autoTransfer = "T"
        }
        /*else {
            usageDecision.descrChar = "Usage Decision"
            usageDecision.nameChar = "LOBM_UDCODE"
            usageDecision.qualityParameterValue = "OL-RM    R"
            vegaBcApprovePostData.autoTransfer = "F"
        }*/
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
                plant = plantDetails,
                approvalDetails = vegaBcApprovePostData
            )
        )
    }

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")
            /* wbDetails.qualityDetails = qtyParams as List<VegaQuality>
             wbDetails.batchNumber = batchNo
             wbDetails.finalApproval = finalApproval
             qualityPostList.clear()
             wbDetails.let { qualityPostList.addAll(listOf(it)) }*/

            wbPostDetails.qualityDetails = qtyParams as List<VegaQuality>
            wbPostDetails.batchNumber = batchNo
            wbPostDetails.finalApproval = finalApproval
            wbPostDetails.qualityFlag=true
            qualityPostList.clear()
            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }

            /* vm.postQualityParams(
                 VegaCameroonQualityApprovePost(
                     key = getCurrentKey(),
                     plant = plantDetails,
                     lotDetails = qualityPostList
                 )
             )*/
            vm.postQualityParams(
                VegaCameroonQcPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
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
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbDetails.wbid) }
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
                else -> {
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
