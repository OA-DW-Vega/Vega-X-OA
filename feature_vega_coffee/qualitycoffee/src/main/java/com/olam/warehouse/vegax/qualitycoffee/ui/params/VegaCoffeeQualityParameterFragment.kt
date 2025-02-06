package com.olam.warehouse.vegax.qualitycoffee.ui.params

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.ui.printformats.NicaraguaMTNRPrintReceipt
import com.olam.warehouse.login.ui.printformats.NicaraguaMTNRPrintTicket
import com.olam.warehouse.login.ui.printformats.generateFgrnTallySheetBitMap
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaMaterialQualitGrades
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.Constants.MTNT_BATCH_NUMBER
import com.olam.warehouse.presentation.utils.UIUtils.GRN_DATA
import com.olam.warehouse.presentation.utils.UIUtils.LOTS
import com.olam.warehouse.presentation.utils.UIUtils.MATERIAL
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycoffee.R
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.NicaraguaUpdateTallySequencePost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualitycoffee.data.domain.model.VegaCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualitycoffee.databinding.FragmentVegaCoffeeQualityParamsBinding
import com.olam.warehouse.vegax.qualitycoffee.ui.VegaCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualitycoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.math.roundToInt


class VegaCoffeeQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var batchNo1: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var materialName: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var trucNo: String? = ""
    private var origin: String? = ""
    private var department: String? = ""
    private var lotDetails = VegaCoffeeLot()
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaCoffeeQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaCoffeeQualityViewModel by viewModel()
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private var qualitySupplierPostList = arrayListOf<VegaQualityWBDetails>()
    private var weightmentType: String? = ""
    private var grnNumber: String? = ""
    private var isAccept: Boolean? = false
    val value = mutableListOf<VegaQualityParamsWithQualitative>()
    private var remarks: String? = ""
    var tallySheet: String? = ""
    var tallySequence: String? = ""
    var mtnrSeq: String = ""
    private var printReceiptKeys = ArrayList<String>()
    private var printTicketKeys = ArrayList<String>()
    private var printSampleKeys = ArrayList<String>()
    private var isFinalCall = true
    private var isSampleTCal = false
    private var callBack: Callback? = null
    private var gradeList = mutableListOf<VegaQualitative>()
    private var materialQualityGradeDesc = mutableListOf<VegaNicaraguaMaterialQualitGrades>()
    private var intent: Intent? = null
    private var MERGED = false
    private var MILLING_PLANT=false
    private var THIRD_PARTY_PLANT=false
    private var existingTallySheet: String? = ""
    private var materialDetails = VegaMaterial()


    interface Callback {
        fun popAllFragmentsFromBackStack()
    }

    companion object {
        fun newInstance() = VegaCoffeeQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaCoffeeQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = Intent(activity, SuccessActivity::class.java)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaCoffeeQualityParameterFragment")
            .title("IVC/Coffee/Quality/Parameter")
            .with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        //vm.printDetails.observe(viewLifecycleOwner, { println("==========reprint cal=================> ${it.status}") })

        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener {
            proceeToPost(FNQUALITY, R.string.confirm_Quty_message)
        }
        binding.btnAccept.setOnClickListener {
            isAccept = true
            proceeToPost(FNQUALITY, R.string.confirm_Quty_message)
        }
        binding.btnReject.setOnClickListener {
            isAccept = false
            proceeToPost(FNREJECT, R.string.confirm_reject_message)
        }
        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.upadteFilter(mAdapter.getItems())
                isSort = true
            }
        }
        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()
        if (getCurrentKey().split("_")[1].contains("NI")) {
            tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
            mtnrSeq = PreferenceHelper.get(Constants.MTNR_SEQUENCE, "")
           /* if(tallySequence?.isNotEmpty() == true && MERGED){
                tallysheet = vm.generateTallySequnceNumber()
            }else{
                tallysheet = existingTallySheet.toString()
            }
            tallySheet = tallysheet
            receivingData.palletType = tallysheet
*/
            vm.configItems.observe(viewLifecycleOwner, Observer { configItems ->
                val dryingPlant = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
                val millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }
                val thirdPartyPalnts = configItems.filter { it.process.equals(ConfigItems.THIRD_PARTY_PLANT.item) }
                MERGED = !dryingPlant.isNullOrEmpty()
                MILLING_PLANT = millingPlants.isNotEmpty()
                THIRD_PARTY_PLANT = thirdPartyPalnts.isNotEmpty()

                if (tallySequence?.isNotEmpty() == true && MERGED) {
                        tallysheet = vm.generateTallySequnceNumber()
                    }
                    receivingData.palletType = tallysheet

            })
            vm.getConfigItems(UserRoles.PROCESSING.role)
        }
        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.qualitySupplier.observe(
            viewLifecycleOwner,
            Observer { updateSupplierQualityPostSuccess(it) })

    }

    private fun postPrintReceipt(
        grnNo: String, batchNo: String,
        isReceipt: String, palletNo: String, materialCode: String
    ) {
        var isTicketType = "receipt"
        if (printReceiptKeys.size > 0 && isReceipt.equals("receipt")) {
            PreferenceHelper.save(MTNR_PRINT_KEY, printReceiptKeys.get(0))
        } else if (printTicketKeys.size > 0 && isReceipt.equals("ticket")) {
            //isFinalCall = false
            isTicketType = "ticket"
            PreferenceHelper.save(MTNR_PRINT_KEY, printTicketKeys.get(0))
        } else if (printSampleKeys.size > 0 && isReceipt.equals("sample_ticket")) {
            isFinalCall = false
            isTicketType = "sample_ticket"
            PreferenceHelper.save(MTNR_PRINT_SAMPLE_KEY, printSampleKeys.get(0))
        }else{
            isFinalCall = false
        }
        val input = workDataOf(
            GRN_DATA to grnNo, BATCH_NO to batchNo,
            PRINT_TYPE to isTicketType, LOTS to palletNo, MATERIAL to materialCode
        )
        val worker = getPrintTicketOneTimeRequestWorker(input)
        enQueueWorker(worker, App.getAppContext())
        WorkManager.getInstance(App.getAppContext()).getWorkInfoByIdLiveData(worker.id)
            .observe(requireActivity(), Observer { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                           // hideCustomLoading()
                            if (isFinalCall) {
                                if (!isSampleTCal) {
                                    postPrintReceipt(grnNo, batchNo, "ticket", palletNo, materialCode)
                                    isSampleTCal = true
                                } else
                                    postPrintReceipt(grnNo, batchNo, "sample_ticket", palletNo, materialCode)
                            } else {
                                countOfLot -= 1
                                if (countOfLot == 0) {
                                    saveMtnrSequence()
                                    intent?.let { startActivity(it) }
                                    activity?.finish()
                                } else {
                                    flag = true
                                    intent?.putExtra("nicaragua_quality", true)
                                    intent?.let { startActivity(it) }
                                    hideCustomLoading()
                                    callBack?.popAllFragmentsFromBackStack()
                                }

                            }
                        }
                        WorkInfo.State.FAILED -> {
                            hideCustomLoading()
                            if (isFinalCall) {
                                if (!isSampleTCal) {
                                    postPrintReceipt(grnNo, batchNo, "ticket", palletNo, materialCode)
                                    isSampleTCal = true
                                } else
                                    postPrintReceipt(grnNo, batchNo, "sample_ticket", palletNo, materialCode)
                            } else {
                                countOfLot -= 1
                                if (countOfLot == 0) {
                                    saveMtnrSequence()
                                    intent?.let { startActivity(it) }
                                    activity?.finish()
                                } else {
                                    flag = true
                                    intent?.putExtra("nicaragua_quality", true)
                                    intent?.let { startActivity(it) }
                                    callBack?.popAllFragmentsFromBackStack()
                                }

                            }
                        }
                        WorkInfo.State.RUNNING -> {
                            //hideLoading()
                            showCustomLoading()
                        }
                        else -> {}
                    }
                }

            })
    }

    private fun initExtra() {
        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHBRIDGE)!!
            weightmentType = weighBridgeDetails.weighBridgeType
            when (weightmentType) {
                PROCURE -> {
                    if (weighBridgeDetails.weighMethod.equals("WS"))
                        vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, true)
                    else
                        vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, false)
                    vm.weighBridgeId.observe(
                        viewLifecycleOwner,
                        Observer { updateWeighbridgeValueUI(it) })
                    batchNo = CreateBatchNo()
                    wbId = weighBridgeDetails.weighBridgeId
                    // batchNo = weighBridgeDetails.batchNumber
                    isData = it.getBoolean(IS_PARAMS_VALUE, false)
                    materialNo = weighBridgeDetails.materialCode
                    materialName = weighBridgeDetails.materialName
                    netWeight = weighBridgeDetails.netWeight
                    tarWeight = weighBridgeDetails.bagWeight
                    challanNo = weighBridgeDetails.challan
                    itemValue = weighBridgeDetails.item
                    wbType = weighBridgeDetails.weighBridgeType
                    bagCount = weighBridgeDetails.bagCount
                    plant = weighBridgeDetails.plant
                    grnNumber = weighBridgeDetails.grnNumber
                    trucNo = weighBridgeDetails.vehicleNumber
                    binding.tvBatchnotitle.visibility = View.GONE
                    binding.etBatchNo.visibility = View.GONE
                    binding.tvParamsWeighBID.text = trucNo
                }
                STO -> {
                    lotDetails = it.getParcelable(LOT)!!
                    lotItems = it.getParcelableArrayList<VegaCoffeeLot>(LOT_LIST) ?: ArrayList()
                    lotItems.forEach {
                        it.bagCount = it.bagCount.toString().trim()
                        it.bagType = it.bagType.toString().trim()
                        it.bagWeight = it.bagWeight.toString().trim()
                        it.pmat2Count = it.pmat2Count.toString().trim()
                        it.pmat2Type = it.pmat2Type.toString().trim()
                        it.pmat2Weight = it.pmat2Weight.toString().trim()
                        it.pmat3Count = it.pmat3Count.toString().trim()
                        it.pmat3Type = it.pmat3Type.toString().trim()
                        it.pmat3Weight = it.pmat3Weight.toString().trim()
                        it.netWeight = it.netWeight.toString().trim()
                        it.grossWeight = it.grossWeight.toString().trim()
                        it.bagTareWeight = it.bagTareWeight.toString().trim()
                        it.storageLocation = it.storageLocationCode.toString().trim()

                        it.weighBridgeType =
                            if (it.weighBridgeType.isNullOrEmpty()) weighBridgeDetails.weighBridgeType else it.weighBridgeType
                    }
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        try {
                            val index = lotItems.size - countOfLot
                            //if (index >= 0 && index < lotItems.size) {
                                BAG_COUNT = lotDetails.bagCount.toString().trim()
                                GROSS_WEIGHT = lotDetails.grossWeight.toString().trim()
                                receivingData.unitsOfMeasure = lotDetails.unitsOfMeasure.toString().trim()
                                receivingData.bagCount = lotDetails.bagCount.toString().trim()
                                receivingData.grossWeight = lotDetails.grossWeight.toString().trim()
                                receivingData.netWeight = lotDetails.netWeight.toString().trim()
                                receivingData.tareWeight =
                                    (lotDetails.grossWeight.toString().trim().toDouble()
                                        .minus(
                                            lotDetails.netWeight.toString().trim().toDouble()
                                        )).formatTwoDigits()
                                        .toString()
                           // }
                        } catch (exception: ArrayIndexOutOfBoundsException) {
                            println("invalid index")
                        }
                    }
                    trucNo = lotDetails.vehicleNumber
                    wbId = lotDetails.weighBridgeId
                    batchNo = lotDetails.batchNumber
                    isData = it.getBoolean(IS_PARAMS_VALUE, false)
                    materialName = lotDetails.materialName
                    materialNo =
                        if (!lotDetails.materialCode?.length?.equals(18)!!) "000000".plus(lotDetails.materialCode) else lotDetails.materialCode
                    netWeight = lotDetails.netWeight
                    tarWeight = lotDetails.bagWeight
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        tarWeight = receivingData.tareWeight
                        /*receivingData.tareWeight = String.format(
                            Locale.ENGLISH,
                            "%.2f",
                            if (tarWeight?.isNotEmpty()!!) {
                                tarWeight?.toDouble()
                            } else 0.0
                        )*/
                    }
                    challanNo = lotDetails.challan
                    itemValue = lotDetails.item
                    wbType = lotDetails.weighBridgeType
                    //copiedWbid = it.getString(COPIED_WBID).toString()
                    //copiedMaterial = it.getString(COPIED_MATERIAL).toString()
                    lotbatchNo = lotDetails.batchNumber
                    lotmaterialNo = lotDetails.materialCode
                }

            }
        }
        when (weightmentType) {
            PROCURE -> {
                binding.tvBatchnotitle.text =
                    resources.getString(R.string.batch_no) + " : " + batchNo
                // binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
                binding.etBatchNo.isEnabled = true
                vm.getQualityParams(materialNo!!, isData, wbId)
                binding.btnReject.visible()
                binding.btnAccept.visible()
                binding.btnParamsProceed.gone()
                binding.tvType.text = SUPPLIER
                enableProceedBtn(weighBridgeDetails.status)
            }
            STO -> {
                binding.tvParamsWeighBID.text = trucNo

                vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
                if (isData!!)
                    materialNo?.let {
                        if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                            vm.getQualityParams(copiedMaterial, isData, copiedWbid)
                        else
                            vm.getQualityParams(materialNo!!, isData, wbId)
                    }
                else {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        var mtnBatch = lotDetails.delivery.plus(lotDetails.deliveryItem).plus(MTNT_BATCH_NUMBER)
                        var batchNo = PreferenceHelper.get(mtnBatch, "")
                        vm.getPreSamplingQualitydata(batchNo.trim(), lotmaterialNo?.trim()!!)

                    } else {
                        vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
                    }

                }

                if (weightmentType.equals(STO)) {
                    binding.etBatchNo.filters = arrayOf<InputFilter?>(LengthFilter(11))
                    binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
                }
                binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
                if (wbType.equals(PROCURE)) {
                    binding.btnReject.visible()
                    binding.btnAccept.visible()
                    binding.btnParamsProceed.gone()
                    binding.tvType.text = SUPPLIER
                } else {
                    binding.btnReject.gone()
                    binding.btnAccept.gone()
                    binding.btnParamsProceed.visible()
                    binding.tvType.text = MTNR
                }
                enableProceedBtn(weighBridgeDetails.status)
            }
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
        getMaterialDetails()
    }

    private fun enableProceedBtn(status: Int?) {
        when (status) {
            4 -> {
                binding.btnParamsProceed.isEnabled = false
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.grey) }
                )
            }
            else -> {
                binding.btnParamsProceed.isEnabled = true
                binding.btnAccept.isEnabled = true
                binding.btnReject.isEnabled = true
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let { ContextCompat.getColorStateList(it, com.olam.warehouse.presentation.R.color.red) }
                )
            }
        }
    }

    private fun getMaterialDetails(){
        vm.getMaterialDetails(if(weighBridgeDetails.materialCode?.length?.equals(18) == true) weighBridgeDetails.materialCode.toString().takeLast(12) else weighBridgeDetails.materialCode.toString())
        vm.product.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                materialDetails = it
            }
        })
    }

    private fun proceeToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        var total = 0.0
        val invalidDataPos = mutableListOf<Int>()
        when (weightmentType) {
            PROCURE -> {
                batchNo1 = ""
            }
            STO -> {
                batchNo1 = binding.etBatchNo.text.toString()
            }
        }

        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (getCurrentKey().split("_")[1].contains("NI")) {

                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIDANO")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFGICO")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014") && materialName?.contains(
                                "Certificado",
                                true
                            ) == false
                        ) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014") && materialName?.contains(
                                "Certificado",
                                true
                            ) == true
                        ) {
                            itValue?.qualityParameter?.mandatory = 1
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    } else {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }

                } else {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (materialDetails.productGroup.equals(Constants.ROBU)) {
                            if (itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals(
                                    "NICASCAB"
                                )
                                || itValue?.qualityParameter?.nameChar.equals("NIDESR")
                            ) {

                                itValue?.qualityParameter?.qualityParameterValue =
                                    itValue?.qualityParameter?.qualityParameterValue?.replace("%", "")
                                total += itValue?.qualityParameter?.qualityParameterValue?.trim()
                                    ?.toDouble()?.formatTwoDigits()?.toDouble() ?: 0.0
                            }
                        } else {
                            if (itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals(
                                    "NICASCAB"
                                )
                                || itValue?.qualityParameter?.nameChar.equals("NIDESA") || itValue?.qualityParameter?.nameChar.equals(
                                    "NIDESC"
                                )
                                || itValue?.qualityParameter?.nameChar.equals("NIDESD")
                            ) {

                                itValue?.qualityParameter?.qualityParameterValue =
                                    itValue?.qualityParameter?.qualityParameterValue?.replace("%", "")
                                total += itValue?.qualityParameter?.qualityParameterValue?.trim()
                                    ?.toDouble()?.formatTwoDigits()?.toDouble() ?: 0.0
                            }
                        }
                    }
                    itValue?.qualityParameter?.mandatory = 0
                }

                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        if (isValueNeed) {
            if (weightmentType == PROCURE) {
                if (finalApproval.equals(FNREJECT)) {
                    showRemarkDialog(finalApproval)
                } else {
                    //postQuality(qualityParameterList, wbId, batchNo1!!, finalApproval)
                    /* if (binding.etBatchNo.text.toString().isNotEmpty()) {*/
                    showConfirmDialog(batchNo1!!, msg, finalApproval)
                    /* } else showSnack(requireContext().resources.getString(R.string.batch_error))*/
                }

            } else {
                if (getCurrentKey().split("_")[1].contains("NI")) {
                    var sum = 0
                    try {
                        sum = total.formatTwoDigits().toInt()
                    } catch (E: NumberFormatException) {
                        sum = total.roundToInt()
                    }
                    if (sum != 100) {
                        mAdapter.getItems().forEachIndexed { index, itValue ->
                            if (materialDetails.productGroup.equals(Constants.ROBU)) {
                                if (itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals(
                                        "NICASCAB"
                                    ) || itValue?.qualityParameter?.nameChar.equals("NIDESR")
                                ) {
                                    itValue?.qualityParameter?.mandatory = 1
                                    invalidDataPos.add(index)
                                }

                            } else {
                                if (itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals(
                                        "NICASCAB"
                                    )
                                    || itValue?.qualityParameter?.nameChar.equals("NIDESA") || itValue?.qualityParameter?.nameChar.equals(
                                        "NIDESC"
                                    )
                                    || itValue?.qualityParameter?.nameChar.equals("NIDESD")
                                ) {
                                    itValue?.qualityParameter?.mandatory = 1
                                    invalidDataPos.add(index)
                                }
                            }
                        }
                        showSnack(requireContext().resources.getString(R.string.ente_fields_equal))
                        mAdapter.updateMissedPos(invalidDataPos, data)
                    } else
                        showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
                } else {
                    showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
                    if (finalApproval.equals(FNREJECT)) {
                        showRemarkDialog(finalApproval)
                    } else {
                        //postQuality(qualityParameterList, wbId, lotDetails.batchNumber ?: "", finalApproval)
                        showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
                    }

                }
            }
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {

        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    it.forEach { item ->
                        if (item.qualityParameter.nameChar == "CI_SLOC_KOR") {
                            item.qualitative =
                                prepareQualitative(
                                    custonLocationList,
                                    item.qualityParameter.materialCode
                                )
                        }


                    }
                    value.clear()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!
                                    } else
                                        item.qualityParameter.qualityParameterValue =
                                            item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                                if(item1.sapQCName.equals("NICERTI")) {
                                    existingTallySheet = item1.satNam
                                    if((MILLING_PLANT || THIRD_PARTY_PLANT) && item1.satNam?.isNotEmpty() == true){
                                        tallysheet = existingTallySheet.toString()
                                        receivingData.palletType = tallysheet
                                    }
                                }
                            }
                            if (item.qualityParameter.nameChar == "CI_COFFEE_TRANS_ORIGIN" && weightmentType == PROCURE) {
                                item.qualityParameter
                                item.qualitative?.forEach { item1 ->
                                    if (item1.charValue == origin) {
                                        item.qualityParameter.qualityParameterValue = "null"
                                    }
                                }
                            }
                            if (item.qualityParameter.nameChar == "CI_COFFEE_TRANS_DEPT" && weightmentType == PROCURE) {
                                item.qualitative?.forEach { item1 ->
                                    if (item1.charValue == department) {
                                        item.qualityParameter.qualityParameterValue = "null"
                                    }
                                }
                            }

                        }
                    } else {
                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        tarWeight,
                        netWeight,
                        challanNo,
                        materialName
                    )
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }

    }


    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList = it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, wbId) }
                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                grade = it.data?.data?.get(0)?.qualityGrade.toString()

                                vm.getGrades(materialNo?.trim()!!)
                                vm.getMaterialQualityGrades(materialNo?.trim()!!)

                                val gradeListFilter = mutableListOf<VegaQualitative>()
                                gradeList.clear()
                                materialQualityGradeList.clear()
                                vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
                                    materialQualityGradeList = it.toMutableList()
                                })
                                gradeListFilter.clear()
                                vm.grade.observe(viewLifecycleOwner, Observer {
                                    gradeList = it.toMutableList()
                                    materialQualityGradeList.forEach { qualityGrade ->
                                        gradeListFilter.addAll(gradeList.filter {
                                            it.charValue.split(" ")
                                                .get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                                        })
                                    }
                                    var list = gradeListFilter.filter { it.charValue.equals(grade) } as ArrayList
                                    if (list.size > 0)
                                        list.forEach { vegaQualitative ->
                                            if (vegaQualitative.descValue.isNotEmpty())
                                                receivingData.qualityGradeDesc =
                                                    vegaQualitative.descValue
                                        }


                                })

                            }
                        }
                        else ->
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")

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

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        MaterialDialog(requireContext()).show {
            message(msg)
            UIUtils.getMetirialCustomView(
                this,
                view.context.getString(R.string.proceed),
                view.context.getString(R.string.cancel),
                {
                    /*qualityParameterList.clear()
                  mAdapter.getItems().forEach { qualityParameterList.add(it?.qualityParameter) }*/
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (countOfLot > 1 && AppUtils.isOnline() &&  !MILLING_PLANT && !THIRD_PARTY_PLANT) {

                            // val year = tallySheet?.substring(0,4)
                            if (tallySequence?.isNotEmpty() == true) {
                                postTallyUpdateSequence()
                                vm.updatetallySequence.observe(
                                    viewLifecycleOwner,
                                    { UpdateTally(it, batchNo, finalApproval, isloadhide = true) })

                            }
                        } else
                            postQuality(qualityParameterList, wbId, batchNo, finalApproval)
                    } else
                        postQuality(qualityParameterList, wbId, batchNo, finalApproval)
                    dismiss()
                },
                { dismiss() })
        }
    }

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

    fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {
        when (weightmentType) {
            PROCURE -> {
                val qtyParams =
                    qualityParameter.filter { it?.qualityParameterValue?.isNotEmpty() == true } //as ArrayList<VegaQualityParameter?>
                /*var filterlist=     value.distinctBy { it.qualityParameter.nameChar }.filter {  it.qualityParameter.nameChar.equals("CI_COFFEE_TRANS_ORIGIN") }
                var filterlist1=     value.distinctBy { it.qualityParameter.nameChar }.filter { it.qualityParameter.nameChar.equals("CI_COFFEE_TRANS_DEPT")  }
                if(filterlist.size>0)
                {
                    filterlist.get(0).qualityParameter.preSampling
                    filterlist.get(0).qualityParameter.qualityParameterValue="null"
                    qtyParams.addAll(filterlist.map { it.qualityParameter } as ArrayList<VegaQualityParameter>)

                }
                if(filterlist1.size>0) {
                    filterlist1.get(0).qualityParameter.qualityParameterValue="null"
                    qtyParams.addAll(filterlist1.map { it.qualityParameter } as ArrayList<VegaQualityParameter>)
                }*/

                weighBridgeDetails.qualityDetails = prepareVegaQualityParams1(qtyParams.toList())
                weighBridgeDetails.batchNumber = batchNo1
                weighBridgeDetails.finalApproval = finalApproval
                qualityPostList.clear()
                weighBridgeDetails.let {
                    qualityPostList.add(
                        /*VegaCoffeeLot(
                            batchNumber = batchNo1 ?: "",
                            qualityDetails = qtyParams,
                            weighBridgeId = weighBridgeDetails.weighBridgeId,
                            weighBridgeType = weightmentType
                        )*/
                        VegaCoffeeLot(
                            weighBridgeId = weighBridgeDetails.weighBridgeId,
                            batchNumber = batchNo1 ?: "",
                            delivery = weighBridgeDetails.delivery!!,
                            item = weighBridgeDetails.item,
                            customerNum = weighBridgeDetails.customerNum.toString(),
                            purchaseDocNum = weighBridgeDetails.purchaseDocNum.toString(),
                            purchaseDocDesc = weighBridgeDetails.purchaseDocDesc.toString(),
                            materialName = weighBridgeDetails.materialName,
                            materialCode = weighBridgeDetails.materialCode,
                            supplierName = weighBridgeDetails.supplierName,
                            supplierCode = weighBridgeDetails.supplierCode,
                            deliveryItem = weighBridgeDetails.deliveryItem,
                            bagType = weighBridgeDetails.bagType,
                            bagCount = weighBridgeDetails.bagCount,
                            bagWeight = weighBridgeDetails.bagWeight,
                            unitsOfMeasure = weighBridgeDetails.unitsOfMeasure,
                            netWeight = weighBridgeDetails.netWeight,
                            grossWeight = weighBridgeDetails.grossWeight,
                            challan = weighBridgeDetails.challan,
                            plant = weighBridgeDetails.plant,
                            direction = weighBridgeDetails.direction,
                            weighBridgeType = weighBridgeDetails.weighBridgeType,
                            erdat = weighBridgeDetails.erdat,
                            ertim = weighBridgeDetails.ertim,
                            qcStatus = weighBridgeDetails.qcStatus,
                            vehicleNumber = weighBridgeDetails.vehicleNumber,
                            storageLocationCode = weighBridgeDetails.storageLocationCode,
                            storageLocation = weighBridgeDetails.storageLocation,
                            transportVendorCode = weighBridgeDetails.transportVendorCode,
                            contactNumber = weighBridgeDetails.contactNumber,
                            driverName = weighBridgeDetails.driverName,
                            grnNumber = weighBridgeDetails.grnNumber,
                            finalApproval = finalApproval,
                            qualityDetails = prepareVegaQualityParams1(qtyParams.toList())
                        )
                    )
                }
                vm.postQualitySupplierParams(
                    VegaCoffeeQualitySupplierParamPost(
                        grnApplicable = true,
                        grnFlag = false,
                        key = getCurrentKey(),
                        plant = getPlantDetails(),
                        lotDetails = qualityPostList,
                        bcMessage = "",
                        charg = batchNo1.toString(),
                        currentWbid = this.wbId.toString(),
                        errorMessage = "",
                        grnNumber = "",
                        driverName = weighBridgeDetails.driverName.toString(),
                        imageString = "",
                        imageUploadMsg = "",
                        contactNumber = weighBridgeDetails.contactNumber.toString(),
                        transportVendorCode = weighBridgeDetails.transportVendorCode.toString(),
                        vehicleNumber = weighBridgeDetails.vehicleNumber.toString(),
                        remarks = remarks.toString()

                    )
                )
            }
            STO -> {
                val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
                var filterlist =
                    qualityParameter.singleOrNull { it?.nameChar?.contains("NIPOSITI") == true }
                if (filterlist != null) {
                    receivingData.gradeDesc = filterlist.qualityParameterValue.toString()
                }
                var filterlis =
                    qualityParameter.singleOrNull { it?.nameChar?.contains("NIFG0014") == true }
                if (filterlist != null) {
                    receivingData.certificate = filterlis?.qualityParameterValue.toString()
                }
                val lotCoutPost = lotItems.filter { it.batchNumber.equals(lotDetails.batchNumber) }
                val qcDoneLot = lotItems.filter { it.qcStatus.equals("X") }
                val isApplicableGrn = lotItems.size - 1 == qcDoneLot.size
                if (AppUtils.isOnline()) {
                    // @Suppress("UNCHECKED_CAST")
                    lotDetails.qualityDetails = prepareVegaQualityListData(qtyParams)
                    lotDetails.batchNumber = batchNo
                    lotDetails.finalApproval = finalApproval
                    if (weighBridgeDetails.weighMethod == "WB") {
                        lotDetails.weighBridgeType = "weighBridge"
                    } else {
                        lotDetails.weighBridgeType = "weighscale"
                    }
                    qualityPostList.clear()
                    if (isApplicableGrn) {
                        lotItems.forEach {
                            if (it.batchNumber.equals(lotDetails.batchNumber)) it.qualityFlag =
                                false
                        }
                        lotDetails.let { qualityPostList.addAll(lotItems) }
                    } else {
                        lotCoutPost.forEach {
                            it.qualityFlag = false
                        }
                        lotDetails.let { qualityPostList.addAll(lotCoutPost) }
                    }
                } else {
                    //saveData(qualityParameter, wbId)
                    moveToSuccessPage(this.wbId, "", "")
                }
                if (getCurrentKey().split("_")[1].contains("NI"))
                    vm.postQualityParams(
                        VegaCoffeeQualityParamPost(
                            grnApplicable = isApplicableGrn,
                            grnFlag = false,
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            lotDetails = qualityPostList,
                            bcMessage = "",
                            charg = "",
                            currentWbid = "",
                            errorMessage = "",
                            grnNumber = "",
                            remarks = remarks.toString(),
                            isGain = IsGain
                        )
                    )
                else
                    vm.postQualityParams(
                        VegaCoffeeQualityParamPost(
                            grnApplicable = isApplicableGrn,
                            grnFlag = false,
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            lotDetails = qualityPostList,
                            bcMessage = "",
                            charg = "",
                            currentWbid = "",
                            errorMessage = "",
                            grnNumber = "",
                            remarks = remarks.toString()
                        )
                    )
            }
        }


    }

    fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
            vm.saveQualityData(prepareVegaQualityData(it!!), batchNo.toString())
        }
    }

    private fun updateSupplierQualityPostSuccess(response: Resource<GenericReqAndResp<VegaCoffeeQualitySupplierParamPost>>) {

        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber
                            )
                            val batch = it.data?.data?.charg
                            val msg = it.data?.message
                            //saveWB(it.data?.data?.currentWbid.toString(), batch.toString(), msg.toString(), 4)
                            //saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                            it.data?.data?.let {
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
                            }
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
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")

                    //saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    //saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoffeeQualityParamPost>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                   // hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccessPage(
                                it.data?.data?.currentWbid,
                                it.data?.data?.charg,
                                it.data?.data?.grnNumber
                            )
                            val batch = it.data?.data?.charg
                            val msg = it.data?.message
                            //saveWB(it.data?.data?.currentWbid.toString(), batch.toString(), msg.toString(), 4)
                            //saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                            it.data?.data?.let {
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
                            }
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
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")

                    //saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    //saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }

    fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        weighBridgeDetails.batchNumber = batchNo
        weighBridgeDetails.wbTempId = weighBrideId
        weighBridgeDetails.status = status
        weighBridgeDetails.finalApproval = FNQUALITY
        weighBridgeDetails.message = message
        weighBridgeDetails.let { vm.saveWBDB(it) }
    }

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?) {
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (countOfLot == 1 && AppUtils.isOnline() && !MILLING_PLANT && !THIRD_PARTY_PLANT) {
                // val year = tallySheet?.substring(0,4)
                if (tallySequence?.isNotEmpty() == true) {
                    postTallyUpdateSequence()
                    vm.updatetallySequence.observe(viewLifecycleOwner, { UpdateTally(it, "", "", isloadhide = false) })
                }
            } else if (!AppUtils.isOnline()) {
                saveTallySequence()
            }
            intent?.putExtra(AppUtils.TITLE, getString(R.string.grn_msg))
        } else {
            if (isAccept!!)
                intent?.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent?.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_success))
        }
        if (!grnNo.isNullOrEmpty()) {
            if (getCurrentKey().split("_")[1].contains("NI")) {

                receivingData.qualityGradeDesc =
                    getQualityDescriptionFromCode(receivingData.materialCode, receivingData.gradeDesc)

                receivingData.grnNumber = grnNo
                receivingData.batchNumber = lotDetails.batchNumber
                if (tallySequence?.isNotEmpty() == true || MILLING_PLANT || THIRD_PARTY_PLANT) {
                    intent?.putExtra(
                        AppUtils.SUB_TITLE,
                        getString(R.string.new_lot_id_created).plus("Batch No : ")
                            .plus(lotDetails.batchNumber)
                            .plus("\n GRN No : ").plus(grnNo)
                            .plus("\n Ticket No : ").plus(tallysheet)
                    )
                } else {
                    intent?.putExtra(
                        AppUtils.SUB_TITLE,
                        getString(R.string.new_lot_id_created).plus("Batch No : ")
                            .plus(lotDetails.batchNumber)
                            .plus("\n GRN No : ").plus(grnNo)
                    )
                }
            } else
                intent?.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus("Batch No : ")
                        .plus(lotDetails.batchNumber)
                        .plus("\n GRN No : ").plus(grnNo)
                )
        } else if (grnNo.isNullOrEmpty()) {
            if (getCurrentKey().split("_")[1].contains("NI")) {
                receivingData.batchNumber = lotDetails.batchNumber
                receivingData.qualityGradeDesc =
                    getQualityDescriptionFromCode(receivingData.materialCode, receivingData.gradeDesc)
                if (tallySequence?.isNotEmpty() == true || MILLING_PLANT || THIRD_PARTY_PLANT) {
                    intent?.putExtra(
                        AppUtils.SUB_TITLE,
                        getString(R.string.new_lot_id_created).plus("Batch No : ")
                            .plus(lotDetails.batchNumber)
                            .plus("\n Ticket No : ").plus(tallysheet)
                    )
                }
            } else {
                var batch: String = ""
                if (weightmentType == PROCURE) batch = charg ?: ""
                else lotDetails.batchNumber
                intent?.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus(batch)
                )
            }
        } else
            intent?.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weigh_bridge_id).plus(lotDetails.weighBridgeId)
            )
        if (getCurrentKey().split("_")[1].contains("NI")) {
            lotItems.forEach { it.grnNumber = receivingData.grnNumber }
            if(tallySequence.isNullOrEmpty() && MERGED){
                showErrorDialogWithFAQLink(requireContext(), getString(R.string.tally_empty_error))
            }else{
                intent?.putExtra(AppUtils.PRINT_ENABLE, true)
                intent?.putExtra(UIUtils.RECEIVING_DATA, receivingData)
                intent?.putExtra(UIUtils.PRINT_TICKET, true)
                intent?.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
                intent?.putExtra(AppUtils.TALLY_SHEETS, tallysheet)
                intent?.putExtra(UIUtils.LOT_DETAIL, lotItems)
                intent?.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_GRN_RECEIPT)
                intent?.putExtra(UIUtils.FROM_NIC_MTNR_GRN, true)
                intent?.putExtra(UIUtils.PRINT_TALLY_SHEET, true)
                intent?.putExtra(UIUtils.ISSPLIT_LOT, false)
                intent?.putExtra(UIUtils.DISPATCH_BATCH, tallysheet)
                intent?.putExtra(UIUtils.MERGED, MERGED)
                if(receivingData.palletType?.isNotEmpty() == true) {
                    printSampleKeys = generateFgrnTallySheetBitMap(receivingData.palletType ?: "", context)
                }
                printTicketKeys = NicaraguaMTNRPrintTicket(intent ?: Intent(), context)
                printReceiptKeys = NicaraguaMTNRPrintReceipt(intent ?: Intent(), context)
                DoAsync {
                    runOnUiThread {
                        postPrintReceipt(
                            receivingData.grnNumber ?: "",
                            receivingData.batchNumber ?: "",
                            "receipt",
                            receivingData.palletType ?: "",
                            receivingData.materialCode ?: ""
                        )
                    }
                }.execute()
            }

        }

    }

    private fun getQualityDescriptionFromCode(gradeCode1: String?, gradeDesc: String?): String? {
        // val gradeCode = if (gradeCode1?.length ?: 0 >= 4) gradeCode1?.takeLast(4).toString() else ""
        val desc = materialQualityGradeList.filter {
            it.materialCode.contains(gradeCode1!!) && gradeDesc!!.contains(
                it.gradeCode,
                true
            )
        }.map { it.grade }
        return if (desc.isNotEmpty()) desc[0] else gradeDesc
    }

    fun postMtnrUpdateSequence() {
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH) + 1).toString()
        var currentyear = rightNow.get(Calendar.YEAR)

        val year = if (currentmonth.equals("10") || currentmonth.equals("11")
            || currentmonth.equals("12")
        ) currentyear + 1 else currentyear

        val prefix1 = Constants.MTNR_SEQUENCE
        val prefix3 = Constants.MTNR_SEQUENCE
        var mtnrSequence = PreferenceHelper.get(Constants.MTNR_SEQUENCE, "")
        when (mtnrSequence.toString().length) {
            1 -> mtnrSequence = "0000".plus(mtnrSequence.toString())
            2 -> mtnrSequence = "000".plus(mtnrSequence.toString())
            3 -> mtnrSequence = "00".plus(mtnrSequence.toString())
            4 -> mtnrSequence = "0".plus(mtnrSequence.toString())
            5 -> mtnrSequence.toString()
        }
        mtnrSequence = mtnrSequence.substring(1)

        val postData = NicaraguaUpdateTallySequencePost(
            getPlantDetails(),
            prefix1,
            year.toString(),
            "",
            "N",
            "",
            "",
            "",
            "",
            "N",
            "N",
            "N",
            "Y",
            mtnrSequence,
            prefix3
        )
        vm.updateTallySequence(postData)
    }

    private fun postTallyUpdateSequence() {
        val rightNow = Calendar.getInstance()
        var currentmonth = (rightNow.get(Calendar.MONTH) + 1).toString()
        var currentyear = rightNow.get(Calendar.YEAR)

        val year = if (currentmonth.equals("10") || currentmonth.equals("11")
            || currentmonth.equals("12")
        ) currentyear + 1 else currentyear

        val prefix1 = Constants.TALLY_SHEET
        val prefix3 = Constants.TALLY_SHEET
        var tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        var tallysequence = ""
        if (tallySequence.isNotEmpty()) {
            val talSeq = tallySequence.substring(tallySequence.length - 5)
            if (talSeq.isNotEmpty()) tallysequence = (talSeq.toInt()).toString()
        }
        when (tallysequence.length) {
            1 -> tallySequence = "0000".plus(tallysequence)
            2 -> tallySequence = "000".plus(tallysequence)
            3 -> tallySequence = "00".plus(tallysequence)
            4 -> tallySequence = "0".plus(tallysequence)
            5 -> tallySequence = tallysequence
        }
        var ismtnrdocseq = "N"
        var mtnrSequence = PreferenceHelper.get(Constants.MTNR_SEQUENCE, "")
        if (mtnrSequence.isNotEmpty()) {
            when (mtnrSequence.toString().length) {
                1 -> mtnrSequence = "0000".plus(mtnrSequence.toString())
                2 -> mtnrSequence = "000".plus(mtnrSequence.toString())
                3 -> mtnrSequence = "00".plus(mtnrSequence.toString())
                4 -> mtnrSequence = "0".plus(mtnrSequence.toString())
                5 -> mtnrSequence.toString()
            }
            mtnrSequence = mtnrSequence.substring(1)
            ismtnrdocseq = "Y"
        }
        val postData = NicaraguaUpdateTallySequencePost(
            getPlantDetails(),
            prefix1,
            year.toString(),
            tallySequence,
            "",
            "N",
            "",
            "",
            "",
            "",
            "N",
            "N",
            "Y",
            ismtnrdocseq,
            mtnrSequence,
            prefix3
        )
        vm.updateTallySequence(postData)
    }


    private fun UpdateTally(
        response: Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>?,
        batchNo: String,
        finalApproval: String,
        isloadhide: Boolean
    ) {
        val tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                if (isloadhide)
                    hideLoading()
                if (response.data?.success == true) {
                    saveTallySequence(tallySequence)
                    saveMtnrSequence()
                    if (countOfLot > 1)
                        postQuality(qualityParameterList, wbId, batchNo, finalApproval)
                    /* else
                        postMtnrUpdateSequence()*/
                }
            }
            Resource.Status.LOADING -> showCustomLoading()
            Resource.Status.ERROR -> {
                saveTallySequence(tallySequence)
                saveMtnrSequence()
                hideCustomLoading()
                showErrorDialogWithFAQLink(requireContext(), "${response.data?.errors}")
            }
            else -> {}
        }
    }

    fun CreateBatchNo(): String {
        var batchno: String = "null"
        batchno = DateUtils.getDate(Calendar.getInstance().timeInMillis, "ddMMYY").toString()
        Log.d("CreateBatchNo", batchno.toString())
        return batchno
    }

    fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                origin = response.data?.data?.origin
                department = response.data?.data?.department
                vm.getQualityParams(materialNo!!, isData, wbId)
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
        }
    }

    fun showRemarkDialog(finalApproval: String) {

        showDialog(getString(com.olam.warehouse.login.R.string.reject_msg), object : DialogClick {
            override fun onPositive(remark: String) {
                if (remark.isEmpty()) Toast.makeText(
                    activity,
                    getString(com.olam.warehouse.presentation.R.string.enter_remark),
                    Toast.LENGTH_SHORT
                )
                    .show()
                else {
                    remarks = remark
                    if (weightmentType == PROCURE) {
                        postQuality(qualityParameterList, wbId, batchNo1!!, finalApproval)
                        /*if (binding.etBatchNo.text.toString().isNotEmpty()) {*/
                        //showConfirmDialog(batchNo1!!, msg, finalApproval)
                        /*} else showSnack(requireContext().resources.getString(R.string.batch_error))*/
                    } else {
                        postQuality(
                            qualityParameterList, wbId,
                            lotDetails.batchNumber, finalApproval
                        )
                        // showConfirmDialog(lotDetails.batchNumber ?: "", msg, finalApproval)
                    }

                }
            }


        }, true, remarks!!)
    }
}
