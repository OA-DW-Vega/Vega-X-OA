package com.olam.warehouse.vegax.qualityapprovecameroon.ui.quality

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
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
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityapprovecameroon.R
import com.olam.warehouse.vegax.qualityapprovecameroon.data.domain.model.*
import com.olam.warehouse.vegax.qualityapprovecameroon.databinding.FragmentVegaBcApproveCameroonBinding
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.OnFragmentQualityApproveCameroonInteractionListener
import com.olam.warehouse.vegax.qualityapprovecameroon.ui.VegaQualityApproveCameroonViewModel
import com.olam.warehouse.vegax.qualityapprovecameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 1/24/2020.
 */

class VegaBcApproveCameroonReadFragment : BaseFragment() {

    private val vm: VegaQualityApproveCameroonViewModel by viewModel()
    private var mListener: OnFragmentQualityApproveCameroonInteractionListener? = null

    private var mAlertDialog: AlertDialog? = null
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var isData: Boolean? = false
    private var wbDetails = VegaQualityApproveCameroonWeighBridge()
    private var wbPostDetails = VegaQualityWBDetails()
    private var vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
    private var qualityPostList = arrayListOf<VegaQualityApproveCameroonWeighBridge>()
    private var qualityQcPostList = arrayListOf<VegaQualityWBDetails>()
    private var preQualityList = mutableListOf<VegaQualityParams>()

    private var dseData = VegaQualityApproveDSE()
    private var dseWbDataDetailsList = mutableListOf<VegaQualityApproveCameroonDSEData>()
    private var jsonData = mutableListOf<String>()
    private var requestIdData: String? = ""
    private var thresholdValue: String? = ""
    private var miscData = mutableListOf<VegaCocoaMiscellaneous>()
    //private var dse = VegaQualityApproveCameroonDSEData()


    private var materialNo: String? = ""
    private var mAdapter = VegaCameroonQualityApproveParamsAdapter { enableProceedBtn(it) }
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
    private var charValue = mutableListOf<String>()

    private lateinit var binding: FragmentVegaBcApproveCameroonBinding

    override val layoutResourceId = R.layout.fragment_vega_bc_approve_cameroon

    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaBcApproveCameroonBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityapprovecameroon/ui/quality/VegaBcApproveCameroonReadFragment").title("Vega_Cameroon/Approve")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentQualityApproveCameroonInteractionListener) {
            mListener = context

        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initUI() {

        vm.getConfigItems(UserRoles.APPROVE.role)

        wbDetails = arguments?.get(APPROVE_DATA) as VegaQualityApproveCameroonWeighBridge
        plantDetails = arguments?.get(APPROVE_PLANT_DATA) as Plant

        selectedPlantId = plantDetails.plantId
        materialNo =
            if (!wbDetails.materialNumber?.length?.equals(18)!!) MATERIAL_CODE.plus(wbDetails.materialNumber) else wbDetails.materialNumber
        netWeight = wbDetails.grnQty

        binding.rvApproveQuality.layoutManager = LinearLayoutManager(this.context)
        binding.rvApproveQuality.isNestedScrollingEnabled = false
        binding.rvApproveQuality.adapter = mAdapter
        binding.tvApproveParamsWeighBID.text = wbDetails.wbid

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(wbDetails.batchNumber.toString(), wbDetails.materialNumber.toString())

        vm.qualitylist.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        vm.quality.observe(viewLifecycleOwner, Observer { updateQualityApproval(it) })
        vm.approval.observe(viewLifecycleOwner, Observer { updateApprovalUI(it) })

        vm.mtntWeight.observe(viewLifecycleOwner, Observer { updateWeighmentDetails(it) })
        wbDetails.wbid?.let { vm.getMtntWeightDetails(it) }



        vm.qcweighBridge.observe(viewLifecycleOwner, Observer { updateWeightbridgeListUI(it) })
        vm.getQCWeighBridgeList(selectedPlantId)

        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })

        /*vm.getThresholdValue(getCurrentKey())
        vm.miscellaneousItems.observe(viewLifecycleOwner, Observer {
            fetchThresholdValue(it)
        })*/

        binding.btnOkApprove.setOnClickListener { activity?.onBackPressed() }

        binding.tvLotIdValue.text = wbDetails.batchNumber
        binding.tvSupplierValue.text = wbDetails.supplierName
        binding.tvMaterialValue.text = wbDetails.materialName
        binding.tvStorageLocationValue.text = wbDetails.plantDesc
        binding.tvGrnQtyValue.text = wbDetails.grnQty?.replace(" ", "")
        binding.tvGrnNoValue.text = wbDetails.grnNumber

        vm.DSEApproveData.observe(viewLifecycleOwner, Observer { updateDSEDetails(it) })
        vm.getDSEDetails(status = "PENDING")

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
                            wbPostDetails = filteredList.single()
                            bagCount = filteredList[0].bagCount.toString()
                            bagWeight = filteredList[0].bagWeight.toString()
                            grossWeight = filteredList[0].grossWeight.toString()
                            netWeight = filteredList[0].netWeight.toString()
                            palletTare =
                                (grossWeight.toDouble() - bagWeight.toDouble() - netWeight?.toDouble()!!).toString()
                        }
                        println("")
                    } else {
                        //Comment for Sonar Fix
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }

            }
        }
    }


    private fun updateConfigItems(configItems: List<VegaConfigDetails>?) {
        val defaultStorageLoc = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }
        val receivingPlantList = configItems?.filter { it.process.equals(ConfigItems.DEFAULT_TRANSFER_LOC.item) }

        if (receivingPlantList != null) {
        }
        var list = receivingPlantList?.get(0)?.value
        charValue = list?.split(",")?.map { it.trim() }?.toMutableList()?: mutableListOf<String>()

        var isExist = false
        val isMaterial =
            defaultStorageLoc?.map { it.materialCode }?.contains(wbDetails.materialNumber?.removeRange(0, 6))
        defaultStorageLoc?.forEach {
            if (wbDetails.materialNumber?.contains(it.materialCode)!! && !it.materialCode.isNullOrEmpty() && !isExist && isMaterial!!) {
                if (it.applicable?.contains(YES)!!) {
                    it.value?.split(",")?.let { it1 -> defaultTransferLocation.addAll(it1) }
                    isExist = true
                }
            } else if (it.materialCode.isNullOrEmpty() && !isExist && !isMaterial!!) {
                if (it.applicable?.contains(YES)!!) {
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
                            if (item.qualityParameter.vegaMandatory.equals(FNACCEPT) ||
                                item.qualityParameter.entryObligatory.equals(FNACCEPT) ||
                                (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                if (!item.qualityParameter.nameChar.equals(B_PRIMARY_REFR) && !item.qualityParameter.nameChar.equals(
                                        B_PAID_WT
                                    )
                                    && !item.qualityParameter.nameChar.equals(B_SECONDARY_REFR) && !item.qualityParameter.nameChar.equals(
                                        B_GRNQTY1
                                    )
                                    && !item.qualityParameter.nameChar.equals(B_TOTAL_REFR) && !item.qualityParameter.nameChar.equals(
                                        B_GRNPRICE1
                                    )
                                    && !item.qualityParameter.nameChar.equals(LOBM_UDCODE)
                                )
                                    value.add(item)


                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.replace("%", "").trim()
                                    if (!item.qualityParameter.nameChar.equals(B_PRIMARY_REFR) && !item.qualityParameter.nameChar.equals(
                                            B_PAID_WT
                                        )
                                        && !item.qualityParameter.nameChar.equals(B_SECONDARY_REFR) && !item.qualityParameter.nameChar.equals(
                                            B_GRNQTY1
                                        )
                                        && !item.qualityParameter.nameChar.equals(B_TOTAL_REFR) && !item.qualityParameter.nameChar.equals(
                                            B_GRNPRICE1
                                        )
                                        && !item.qualityParameter.nameChar.equals(LOBM_UDCODE)
                                    )
                                        value.add(item)

                                }
                            }

                        }
                    } else {
                        it.forEach { item1 ->
                            if (!item1.qualityParameter.nameChar.equals(B_PRIMARY_REFR) && !item1.qualityParameter.nameChar.equals(
                                    B_PAID_WT
                                )
                                && !item1.qualityParameter.nameChar.equals(B_SECONDARY_REFR) && !item1.qualityParameter.nameChar.equals(
                                    B_GRNQTY1
                                )
                                && !item1.qualityParameter.nameChar.equals(B_TOTAL_REFR) && !item1.qualityParameter.nameChar.equals(
                                    B_GRNPRICE1
                                )
                                && !item1.qualityParameter.nameChar.equals(LOBM_UDCODE)
                            )
                                value.addAll(it)

                        }
                    }



                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo
                    )
                    if (preQualityList.size > 0) {
                        binding.tvSecondaryRefraction.text = getString(R.string.secondary_refraction)
                        binding.llSecondaryRefraction.visibility = View.VISIBLE
                        binding.etSecondaryRefractionValue.isEnabled = false
                        binding.llReceivingPlant.visibility = View.VISIBLE
                        binding.llReceivingPlant.visibility = View.VISIBLE
                        binding.viRefractionDivision.visibility = View.VISIBLE
                        binding.viCalculationDivision.visibility = View.VISIBLE
                        binding.tvPaidWeight.text = "Paid Weight"
                        binding.llpaidWeight.visibility = View.VISIBLE
                        binding.llPrimaryRefraction.visibility = View.VISIBLE
                        binding.tvPrimaryRefraction.text = getString(R.string.primary_refraction)
                        binding.tvotLotLabel.text = getString(R.string.ot_lot)
                        binding.tvTotalRefraction.text = getString(R.string.total_refraction)
                        binding.llTotalRefraction.visibility = View.VISIBLE
                        binding.tvDiscountedWeight.text = getString(R.string.disc_weight)
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
                            B_GRNQTY1 -> {
                                binding.tvGrnQtyValue.text = it.satNam
                                grnQty = it.satNam?.replace(",", "")?.toDouble() ?: 0.0

                            }
                            B_PRIMARY_REFR -> {

                                binding.tvPrimaryRefractionValue.text = it.satNam?.replace(",","").toString().plus(" KG")
                                primaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            B_SECONDARY_REFR -> {
                                binding.etSecondaryRefractionValue.setText(it.satNam?.replace(",","").toString())
                                secondaryRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            B_PAID_WT -> {
                                paidWeight = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                                binding.tvPaidWeightValue.text = it.satNam?.replace(",", "")?.toDouble()?.formatThreeDigits().plus(" KG")
                            }
                            B_TOTAL_REFR -> {

                                binding.tvTotalRefractionValue.text = it.satNam?.replace(",", "")?.toDouble()?.formatThreeDigits().plus(" %")
                                totalRefraction = it.satNam?.replace(",", "")?.toDouble() ?: 0.0
                            }
                            DISCOUNTEDWEIGHT1 -> {

                                binding.tvDiscountedWeightValue.text = it.satNam?.replace(",", "")?.replace("%", "")?.toDouble()?.formatThreeDigits().plus(" %")
                                discountedWeight = it.satNam?.replace(",", "")?.replace("%", "")?.toDouble() ?: 0.0
                            }
                            B_NETWEIGHT -> {
                                netQty = it.satNam.toString()
                            }
                            SOURCE_LOT -> {
                                binding.tvotLotValue.text = it.satNam.toString()
                                otLotNum = it.satNam.toString()
                                if (it.satNam.toString() == "")
                                    binding.llotLot.visibility = View.GONE
                                else
                                    binding.llotLot.visibility = View.VISIBLE

                            }
                            RECEIVING_PLANT -> {
//
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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
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
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")

                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")


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
            binding.btnParamsProceed.setBackgroundColor(
                getColor(
                    if (getCurrentOriginEntity().contains(
                            "OFI"
                        )
                    ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                )
            )
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

    private fun updateApprovalUI(data: Resource<GenericReqAndResp<VegaQualityApproveCameroonResponse>>?) {
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

            }
        }
    }

    private fun moveToApprovalSuccessPage(wbId: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (finalApprovalStatus == FNACCEPT)
            intent.putExtra(AppUtils.TITLE, getString(R.string.approve_success))
        else
            intent.putExtra(AppUtils.TITLE, getString(R.string.negotiate_success))

        intent.putExtra(AppUtils.SUB_TITLE, wbId)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            "\n WB ID : ".plus(wbId).plus("\n LOT ID : ").plus(wbDetails.batchNumber)
        )

        startActivity(intent)
    }

    private fun moveToFailurePage(wbId: String, msg: String) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, "Approve Failed")
        intent.putExtra(AppUtils.SUB_TITLE, msg.plus("\n").plus(wbId))
        intent.putExtra(AppUtils.FAILURE, false)
        startActivity(intent)
    }

    private fun proceedToPost(finalApproval: String, msg: Int, requestId: String) {

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
            showConfirmDialog(wbDetails.batchNumber.toString(), msg, finalApproval, requestId)
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun showConfirmDialog(
        batchNo: String,
        msg: Int,
        finalApproval: String,
        requestId: String
    ) {

        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    if (finalApproval == FNACCEPT)
                        confirmBcApproval(finalApproval, requestId)
                    else if (finalApproval == FNNEGOTIATE)
                        postQuality(
                            qualityParameterList,
                            wbDetails.wbid,
                            batchNo,
                            finalApproval,
                            requestId
                        )
                },
                { dismiss() })

        }

    }

    fun confirmBcApproval(finalApproval: String, requestId: String) {

        vegaBcApprovePostData = VegaQualityApproveCameroonPostData()
        vegaBcApprovePostData.weighBridgeId = wbDetails.wbid
        vegaBcApprovePostData.batchNumber = wbDetails.batchNumber
        vegaBcApprovePostData.grnQty = wbDetails.grnQty?.replace(" ", "")
        vegaBcApprovePostData.finalApproval = finalApproval
        vegaBcApprovePostData.item = wbDetails.item
        vegaBcApprovePostData.plant = wbDetails.plantId
        vegaBcApprovePostData.supplierCode = wbDetails.supplierCode
        vegaBcApprovePostData.materialCode = wbDetails.materialNumber
        vegaBcApprovePostData.sendingStorageLoc = sendingStorageLocationCode
        vegaBcApprovePostData.uom = "KG"
        vegaBcApprovePostData.receivingStorageLoc = receivingPlant

        var qualityDetails: ArrayList<QualityDetails> = ArrayList()
        qualityParameterList.forEach {
            var quality = QualityDetails()
            quality.descrChar = it?.descrChar
            quality.nameChar = it?.nameChar
            quality.qualityParameterValue = it?.qualityParameterValue
            qualityDetails.add(quality)
        }
        var secondaryQuality = QualityDetails()
        secondaryQuality.descrChar = SEC_REFRACTION_POST
        secondaryQuality.nameChar = B_SECONDARY_REFR
        secondaryQuality.qualityParameterValue = secondaryRefraction.toString()
        qualityDetails.add(secondaryQuality)

        var receivingPlantCharacteristics = QualityDetails()
        receivingPlantCharacteristics.descrChar = PRIM_REFRACTION_POST
        receivingPlantCharacteristics.nameChar = B_PRIMARY_REFR
        receivingPlantCharacteristics.qualityParameterValue = primaryRefraction.toString()
        qualityDetails.add(receivingPlantCharacteristics)

        var receivingCharacteristics = QualityDetails()
        receivingCharacteristics.descrChar = RECEIVING_PLANT
        receivingCharacteristics.nameChar = RECEIVING_PLANT
        receivingCharacteristics.qualityParameterValue = receivingStorageLocationCode
        qualityDetails.add(receivingCharacteristics)

        var usageDecision = QualityDetails()
        if (finalApproval == FNACCEPT) {
            usageDecision.descrChar = USAGE_POST
            usageDecision.nameChar = LOBM_UDCODE
            usageDecision.qualityParameterValue = USAGE_DECISION_ACCEPT
            vegaBcApprovePostData.autoTransfer = AUTO_TRANSFER_T
        }

        /*if (finalApproval == FNNEGOTIATE) {
            usageDecision.descrChar = USAGE_POST
            usageDecision.nameChar = LOBM_UDCODE
            usageDecision.qualityParameterValue = USAGE_DECISION_NEGOTIATE
            vegaBcApprovePostData.autoTransfer = AUTO_TRANSFER_F
        }*/

        qualityDetails.add(usageDecision)

        vegaBcApprovePostData.qualityDetails = qualityDetails
        var list = mutableListOf<VegaQualityApproveCameroonPostData>()

        /*list.clear()
        vegaBcApprovePostData.let { list.addAll(listOf(it)) }*/

        list.add(vegaBcApprovePostData)

        vm.postApproval(
            PostApprovalData(
                key = getCurrentKey(),
                plant = plantDetails,
                requestId = requestId,
                approvalDetails = VegaQualityApproveCameroonPostData(),
                approvalDetailsList = list
            )
        )
    }

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String,
        requestId: String
    ) {
        val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }

        if (AppUtils.isOnline()) {
            @Suppress("UNCHECKED_CAST")


            wbPostDetails.qualityDetails = qtyParams as List<VegaQuality>
            wbPostDetails.batchNumber = batchNo
            wbPostDetails.finalApproval = finalApproval
            qualityPostList.clear()
            wbPostDetails.let { qualityQcPostList.addAll(listOf(it)) }


            vm.postQualityParams(
                VegaCameroonQcPost(
                    key = getCurrentKey(),
                    plant = plantDetails,
                    requestId = requestId,
                    lotDetails = qualityQcPostList
                )
            )
        } else {

        }
    }

    private fun updatePreQuality(data: Resource<GenericReqAndResp<List<VegaQualityApproveCameroon>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList =
                                it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let {
                                vm.getQualityParams(
                                    materialNo!!,
                                    isData,
                                    wbDetails.wbid
                                )
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }

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

    // DSE Details Updation
    private fun updateDSEDetails(data: Resource<GenericReqAndResp<VegaQualityApproveDSE>>?) {

        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {

                            val status =
                                it.data?.data?.wbDetails?.filter { it.dwStatus == "PENDING" }
                            dseWbDataDetailsList =
                                status as MutableList<VegaQualityApproveCameroonDSEData>


                            for (item in dseWbDataDetailsList) {
                                if (item.wbId == wbDetails.wbid) {

                                    requestIdData = item.requestId

                                    updateButton(
                                        item.dwRequestTime.toString(),
                                        item.dseResponseTime.toString(),
                                        item.dseStatus.toString(),
                                        item.requestId.toString()
                                    )
                                    break
                                } else {

                                    var requestId = ""

                                    binding.btnAccept.setBackgroundColor(
                                        getColor(
                                            if (getCurrentOriginEntity().contains(
                                                    "OFI"
                                                )
                                            ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                                        )
                                    )
                                    binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.red))

                                    binding.btnAccept.isEnabled = true
                                    binding.btnNegotiate.isEnabled = true

                                    binding.btnAccept.setOnClickListener {
                                        proceedToPost(
                                            FNACCEPT,
                                            R.string.confirm_quality_message,
                                            requestId
                                        )
                                    }
                                    binding.btnNegotiate.setOnClickListener {
                                        proceedToPost(
                                            FNNEGOTIATE,
                                            R.string.confirm_negotiate_message,
                                            requestId
                                        )
                                    }

                                }
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
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

// Update the button according to DSE Values

    private fun updateButton(
        request: String,
        response: String?,
        status: String?,
        requestId: String
    ) {

        val x = request.split("+")
        val startTime = x.get(0).split("T")
        val time1 = startTime.get(1).split(".")
        val temp = time1[0].split(":")

        val time1hours = TimeUnit.HOURS.toMillis(temp[0].toLong())
        val time1mins = TimeUnit.MINUTES.toMillis(temp[1].toLong())
        val time1secs = TimeUnit.SECONDS.toMillis(temp[2].toLong())
        val totalTime1 = time1hours + time1mins + time1secs

        val startDate = startTime[0].split("-")
        val date1days = TimeUnit.DAYS.toMillis(startDate[2].toLong())
        val totalDays1 = date1days

        @SuppressLint("SimpleDateFormat")
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }

        var diff: Long = 0

        if (response?.trim() == "null") {

            val rep = getCurrentDate().replace("'", "").trim()
            val endTime = rep.split("T")
            val t = endTime[1].split(".")
            val time2 = t[0].split(":")

            val time2hours = TimeUnit.HOURS.toMillis(time2[0].toLong())
            val time2mins = TimeUnit.MINUTES.toMillis(time2[1].toLong())
            val time2secs = TimeUnit.SECONDS.toMillis(time2[2].toLong())
            val totalTime2 = time2hours + time2mins + time2secs

            val endDate = endTime[0].split("-")
            val date2days = TimeUnit.DAYS.toMillis(endDate[2].toLong())
            val totalDays2 = date2days

            if (totalDays1 == totalDays2) {
                val duration: Long = totalTime2.minus(totalTime1)
                diff = TimeUnit.MILLISECONDS.toHours(duration)
            } else {
                val days: Long = totalDays2.minus(totalDays1)
                val duration: Long = totalTime2.minus(totalTime1)
                diff = TimeUnit.MILLISECONDS.toHours(days + duration)
            }

        } else {

            val y = response?.split("+")
            val endTime = y?.get(0)?.split("T")

            val time2 = endTime?.get(1)?.split(".")
            val temp2 = time2?.get(0)?.split(":")
            val time2hours = TimeUnit.HOURS.toMillis(temp2?.get(0)?.toLong() ?: 0)
            val time2mins = TimeUnit.MINUTES.toMillis(temp2?.get(1)?.toLong() ?: 0)
            val time2secs = TimeUnit.SECONDS.toMillis(temp2?.get(2)?.toLong() ?: 0)
            val totalTime2 = time2hours + time2mins + time2secs

            val endDate = endTime?.get(0)?.split("-")
            val date2days = TimeUnit.DAYS.toMillis(endDate?.get(2)?.toLong()!!)
            val totalDays2 = date2days

            if (totalDays1 == totalDays2) {
                val duration: Long = totalTime2.minus(totalTime1)
                diff = TimeUnit.MILLISECONDS.toHours(duration)
            } else {
                val days: Long = totalDays2.minus(totalDays1)
                val duration: Long = totalTime2.minus(totalTime1)
                diff = TimeUnit.MILLISECONDS.toHours(days + duration)
            }
        }

        /*var threshold : Long = 0
        Log.e("fetch",fetchThresholdValue(miscData))
        val fetch = fetchThresholdValue(miscData)
        Log.e("threshold",fetch.toLong().toString())
        threshold = fetch.toLong()*/

        // Threshold Time = 12 HRS
        val threshold = TimeUnit.MILLISECONDS.toHours(43200000L)

        if (status == "D" && response.isNotEmpty()) {

            if (threshold >= diff) {

                binding.btnAccept.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.red))
                binding.btnAccept.isEnabled = false
                binding.btnNegotiate.isEnabled = true
                binding.btnNegotiate.setOnClickListener {
                    proceedToPost(FNNEGOTIATE, R.string.confirm_negotiate_message, requestId)
                }
            } else {

                binding.btnAccept.setBackgroundColor(
                    getColor(
                        if (getCurrentOriginEntity().contains(
                                "OFI"
                            )
                        ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                    )
                )
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.red))

                binding.btnAccept.isEnabled = true
                binding.btnNegotiate.isEnabled = true

                binding.btnAccept.setOnClickListener {
                    proceedToPost(FNACCEPT, R.string.confirm_quality_message, requestId)
                }
                binding.btnNegotiate.setOnClickListener {
                    proceedToPost(FNNEGOTIATE, R.string.confirm_negotiate_message, requestId)
                }
            }

        } else if (status == "A" && response.isNotEmpty()) {

            if (threshold >= diff) {


                binding.btnAccept.isEnabled = true
                binding.btnNegotiate.isEnabled = false
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                binding.btnAccept.setBackgroundColor(
                    getColor(
                        if (getCurrentOriginEntity().contains(
                                "OFI"
                            )
                        ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                    )
                )
                binding.btnAccept.setOnClickListener {
                    proceedToPost(FNACCEPT, R.string.confirm_quality_message, requestId)
                }
            } else {

                binding.btnAccept.setBackgroundColor(
                    getColor(
                        if (getCurrentOriginEntity().contains(
                                "OFI"
                            )
                        ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                    )
                )
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.red))

                binding.btnAccept.isEnabled = true
                binding.btnNegotiate.isEnabled = true

                binding.btnAccept.setOnClickListener {
                    proceedToPost(FNACCEPT, R.string.confirm_quality_message, requestId)
                }
                binding.btnNegotiate.setOnClickListener {
                    proceedToPost(FNNEGOTIATE, R.string.confirm_negotiate_message, requestId)
                }
            }

        } else {

            if (threshold >= diff) {
                binding.btnAccept.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                binding.btnAccept.isEnabled = false
                binding.btnNegotiate.isEnabled = false
            } else {

                binding.btnAccept.setBackgroundColor(
                    getColor(
                        if (getCurrentOriginEntity().contains(
                                "OFI"
                            )
                        ) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green
                    )
                )
                binding.btnNegotiate.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.red))

                binding.btnAccept.isEnabled = true
                binding.btnNegotiate.isEnabled = true

                binding.btnAccept.setOnClickListener {
                    proceedToPost(FNACCEPT, R.string.confirm_quality_message, requestId)
                }
                binding.btnNegotiate.setOnClickListener {
                    proceedToPost(FNNEGOTIATE, R.string.confirm_negotiate_message, requestId)
                }
            }
        }
    }

    //json: "{\"DSE_Threshold\":[\"12 HRS\"]}" ---> Type of Data fetched from Misc Table

    // Fetch Data from Misc Table
    private fun fetchThresholdValue(miscellaneous: List<VegaCocoaMiscellaneous>): String {

        var thresholdValue = ""

        miscellaneous.forEach { item -> jsonData.add(item.json ?: "") }
        val gson = Gson()
        return jsonData.forEach {
            if (it.contains(JSON_DSE_THRESHOLD)) {
                val thresholdList = gson.fromJson(it, VegaQualityDseThreshold::class.java)
                thresholdValue = thresholdList.DSE_THRESHOLD[0]
                thresholdValue = thresholdValue.replace("HRS", "").trim()
                //Log.e("threshold",thresholdValue)
                return thresholdValue
            }
        }.toString()
    }
}



