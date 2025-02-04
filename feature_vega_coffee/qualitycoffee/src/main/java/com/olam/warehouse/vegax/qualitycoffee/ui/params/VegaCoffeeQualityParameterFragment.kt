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
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.*
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.*
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
import kotlin.collections.ArrayList


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
    private lateinit var mListener: OnParamsListener
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

    interface OnParamsListener
    companion object {
        fun newInstance() = VegaCoffeeQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaCoffeeQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_coffee_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        TrackHelper.track().screen("quality/ui/params/VegaCoffeeQualityParameterFragment").title("Quality")
            .with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener { proceeToPost(FNQUALITY, R.string.confirm_Quty_message) }
        binding.btnAccept.setOnClickListener {
            isAccept=true
            proceeToPost(FNQUALITY, R.string.confirm_Quty_message) }
        binding.btnReject.setOnClickListener {
            isAccept=false
            proceeToPost(FNREJECT, R.string.confirm_reject_message) }
        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.upadteFilter(mAdapter.getItems())
                isSort = true
            }
        }
        vm.custonLocation.observe(
            this,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()
        if (getCurrentKey().split("_")[1].contains("NI")) {
            tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
            if (tallySequence?.isNotEmpty() == true) {
                tallysheet = vm.generateTallySequnceNumber()
                tallySheet = tallysheet
                receivingData.palletType = tallysheet
            }
        }
        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })

        vm.qualitySupplier.observe(
            viewLifecycleOwner,
            Observer { updateSupplierQualityPostSuccess(it) })

    }

    private fun initExtra() {

        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHBRIDGE)!!
            weightmentType = weighBridgeDetails.weighBridgeType
            when (weightmentType) {
                PROCURE -> {
                    if(weighBridgeDetails.weighMethod.equals("WS"))
                        vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, true)
                    else
                        vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, false)
                    vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateWeighbridgeValueUI(it) })
                    batchNo = CreateBatchNo()
                    wbId = weighBridgeDetails.weighBridgeId
                    // batchNo = weighBridgeDetails.batchNumber
                    isData = it.getBoolean(IS_PARAMS_VALUE, false)
                    materialNo = weighBridgeDetails.materialCode
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
                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            BAG_COUNT = it.bagCount.toString().trim()
                            GROSS_WEIGHT = it.grossWeight.toString().trim()
                            receivingData.unitsOfMeasure = it.unitsOfMeasure.toString().trim()
                            receivingData.bagCount = it.bagCount.toString().trim()
                            receivingData.grossWeight = it.grossWeight.toString().trim()
                            receivingData.netWeight = it.netWeight.toString().trim()
                            //receivingData.tareWeight = it.bagTareWeight.toString().trim()
                        }
                        it.weighBridgeType =
                            if (it.weighBridgeType.isNullOrEmpty()) weighBridgeDetails.weighBridgeType else it.weighBridgeType
                    }
                    trucNo = lotDetails.vehicleNumber
                    wbId = lotDetails.weighBridgeId
                    batchNo = lotDetails.batchNumber
                    isData = it.getBoolean(IS_PARAMS_VALUE, false)
                    materialNo =
                        if (!lotDetails.materialCode?.length?.equals(18)!!) "000000".plus(lotDetails.materialCode) else lotDetails.materialCode
                    netWeight = lotDetails.netWeight
                    tarWeight = lotDetails.bagWeight
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        receivingData.tareWeight = String.format(
                            Locale.ENGLISH,
                            "%.2f",
                            if (tarWeight?.isNotEmpty()!!) {
                                tarWeight?.toDouble()
                            } else 0.0
                        )
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
                vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
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
                else vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
                vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

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

    private fun proceeToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
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
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014")) {
                            itValue?.qualityParameter?.mandatory = 0
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
                showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
                 if(finalApproval.equals(FNREJECT))
                {
                    showRemarkDialog(finalApproval)
                }
                else {
                     //postQuality(qualityParameterList, wbId, lotDetails.batchNumber ?: "", finalApproval)
                      showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
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
                        challanNo
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
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

                    postQuality(qualityParameterList, wbId, batchNo, finalApproval)
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
            binding.btnParamsProceed.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
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
                val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() } as  ArrayList<VegaQualityParameter?>
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

                weighBridgeDetails.qualityDetails =prepareVegaQualityParams1(qtyParams.toList())
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
                            weighBridgeId= weighBridgeDetails.weighBridgeId,
                         batchNumber =   batchNo1 ?: "",
                            delivery=  weighBridgeDetails.delivery!!,
                            item= weighBridgeDetails.item,
                            customerNum=   weighBridgeDetails.customerNum.toString(),
                            purchaseDocNum=  weighBridgeDetails.purchaseDocNum.toString(),
                            purchaseDocDesc=  weighBridgeDetails.purchaseDocDesc.toString(),
                            materialName=   weighBridgeDetails.materialName,
                            materialCode=   weighBridgeDetails.materialCode,
                            supplierName=  weighBridgeDetails.supplierName,
                            supplierCode=   weighBridgeDetails.supplierCode,
                            deliveryItem= weighBridgeDetails.deliveryItem,
                            bagType=  weighBridgeDetails.bagType,
                            bagCount= weighBridgeDetails.bagCount,
                            bagWeight=  weighBridgeDetails.bagWeight,
                            unitsOfMeasure= weighBridgeDetails.unitsOfMeasure,
                            netWeight=  weighBridgeDetails.netWeight,
                            grossWeight= weighBridgeDetails.grossWeight,
                            challan= weighBridgeDetails.challan,
                            plant= weighBridgeDetails.plant,
                            direction=  weighBridgeDetails.direction,
                            weighBridgeType=  weighBridgeDetails.weighBridgeType,
                            erdat= weighBridgeDetails.erdat,
                            ertim= weighBridgeDetails.ertim,
                            qcStatus=  weighBridgeDetails.qcStatus,
                            vehicleNumber=  weighBridgeDetails.vehicleNumber,
                            storageLocationCode= weighBridgeDetails.storageLocationCode,
                            storageLocation=   weighBridgeDetails.storageLocation,
                            transportVendorCode= weighBridgeDetails.transportVendorCode,
                            contactNumber= weighBridgeDetails.contactNumber,
                            driverName= weighBridgeDetails.driverName,
                            grnNumber=   weighBridgeDetails.grnNumber,
                            finalApproval =   finalApproval,
                            qualityDetails = prepareVegaQualityParams1(qtyParams.toList())
                        )
                    )
                }
                vm.postQualitySupplierParams(
                    VegaCoffeeQualitySupplierParamPost(
                        grnApplicable = true,
                        grnFlag = false, key = getCurrentKey(), plant = getPlantDetails(), lotDetails = qualityPostList,
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
                    qualityParameter.single { it?.nameChar?.contains("NIPOSITI") == true }
                if (filterlist != null) {
                    receivingData.gradeDesc = filterlist.qualityParameterValue.toString()
                }
                var filterlis =
                    qualityParameter.single { it?.nameChar?.contains("NIFG0014") == true }
                if (filterlist != null) {
                    receivingData.certificate = filterlis?.qualityParameterValue.toString()
                }
                val lotCoutPost = lotItems.filter { it.batchNumber.equals(lotDetails.batchNumber) }
                val qcDoneLot = lotItems.filter { it.qcStatus.equals("X") }
                val isApplicableGrn = lotItems.size - 1 == qcDoneLot.size
                if (AppUtils.isOnline()) {
                    @Suppress("UNCHECKED_CAST")
                    lotDetails.qualityDetails = qtyParams as List<VegaQuality>
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
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")

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
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")

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
        val intent = Intent(activity, SuccessActivity::class.java)
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (AppUtils.isOnline()) {
                // val year = tallySheet?.substring(0,4)
                if (tallySequence?.isNotEmpty() == true) {
                    postTallyUpdateSequence()
                    vm.updatetallySequence.observe(viewLifecycleOwner, { UpdateTally(it) })
                }
            } else saveTallySequence()
            intent.putExtra(AppUtils.TITLE, getString(R.string.grn_msg))
        } else {
            if (isAccept!!)
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
            else
                intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_success))
        }
        if (!grnNo.isNullOrEmpty()) {
            if (getCurrentKey().split("_")[1].contains("NI")) {
                receivingData.grnNumber = grnNo
                receivingData.batchNumber = lotDetails.batchNumber
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus("Batch No : ")
                        .plus(lotDetails.batchNumber)
                        .plus("\n GRN No : ").plus(grnNo)
                        .plus("\n Ticket No : ").plus(tallysheet)
                )
            } else
                intent.putExtra(
                    AppUtils.SUB_TITLE,
                    getString(R.string.new_lot_id_created).plus("Batch No : ")
                        .plus(lotDetails.batchNumber)
                        .plus("\n GRN No : ").plus(grnNo)
                )
        } else if (grnNo.isNullOrEmpty()) {
            var batch: String = ""
            if (weightmentType == PROCURE) {
                batch = charg ?: ""
            } else {
                lotDetails.batchNumber
            }
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(batch)
            )
        } else
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.weigh_bridge_id).plus(lotDetails.weighBridgeId)
            )
        if (getCurrentKey().split("_")[1].contains("NI")) {
            if (tallySequence?.isNotEmpty() == true) {
                intent.putExtra(AppUtils.PRINT_ENABLE, true)
                intent.putExtra(UIUtils.RECEIVING_DATA, receivingData)
                intent.putExtra(UIUtils.PRINT_TICKET, true)
                intent.putExtra(UIUtils.FROM_NICARAGUA_COFFEE, true)
                intent.putExtra(AppUtils.TALLY_SHEETS, tallysheet)
                intent.putExtra(UIUtils.NICARAGUA_PRINT_TYPE, UIUtils.NICARAGUA_PRINT_GRN_RECEIPT)
                intent.putExtra(UIUtils.FROM_NIC_MTNR_GRN, true)
                intent.putExtra(UIUtils.PRINT_TALLY_SHEET, true)
            }

        }
        startActivity(intent)
        activity?.finish()
    }


    fun postTallyUpdateSequence() {
        val rightNow = Calendar.getInstance()
        val year = rightNow.get(Calendar.YEAR).toString()
        val prefix1 = Constants.TALLY_SHEET
        var tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")
        when (tallySequence.toString().length) {
            1 -> tallySequence = "0000".plus(tallySequence.toString())
            2 -> tallySequence = "000".plus(tallySequence.toString())
            3 -> tallySequence = "00".plus(tallySequence.toString())
            4 -> tallySequence = "0".plus(tallySequence.toString())
            5 -> tallySequence.toString()
        }
        val postData = NicaraguaUpdateTallySequencePost(
            getPlantDetails(),
            prefix1,
            year,
            tallySequence,
            "N",
            "",
            "",
            "",
            "",
            "N",
            "N",
            "Y"
        )
        vm.updateTallySequence(postData)
    }


    fun UpdateTally(response: Resource<GenericReqAndResp<NicaraguaUpdateTallySequencePost>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                if (response.data?.success == true)
                    saveTallySequence()
            }
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
                vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
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
