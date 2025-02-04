package com.olam.warehouse.vegax.qualityindo.ui.params

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualityParamPost
import com.olam.warehouse.vegax.qualityindo.data.domain.model.VegaIndoCoffeeQualitySupplierParamPost
import com.olam.warehouse.vegax.qualityindo.databinding.FragmentIndoCoffeeQualityParamsBinding
import com.olam.warehouse.vegax.qualityindo.ui.VegaIndoCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualityindo.utils.*
import kotlinx.android.synthetic.main.fragment_indo_coffee_quality_params.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeQualityParameterFragment : BaseFragment() {

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
    private var mAdapter = VegaIndoCoffeeQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaIndoCoffeeQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
//    private var qualitySupplierPostList = arrayListOf<VegaQualityWBDetails>()
    private var weightmentType: String? = ""
    private var isView: Boolean? = false
    private var grnNumber: String? = ""
    private var isAccept: Boolean? = true

    interface OnParamsListener
    companion object {
        fun newInstance() = VegaIndoCoffeeQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentIndoCoffeeQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_indo_coffee_quality_params

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIndoCoffeeQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaCoffeeQualityParameterFragment")
            .title("Quality")
            .with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener { proceeToPost(FNQUALITY, R.string.confirm_Quty_message) }
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
            this,
            Observer { custonLocationList = it.filter { !it.storageLocationType.equals("P") }.toMutableList() })
        vm.getCustomLocations()
        vm.quality.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.qualitySupplier.observe(viewLifecycleOwner, Observer { updateSupplierQualityPostSuccess(it) })
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })
    }

    private fun initExtra() {

        arguments.let {
            weighBridgeDetails = it?.getParcelable(WEIGHBRIDGE)!!
            weightmentType = weighBridgeDetails.weighBridgeType
            isView = weighBridgeDetails.isCopy
            if (isView == true) binding.llItemBottomView.gone() else binding.llItemBottomView.visible()
            when (weightmentType) {
                PROCURE -> {
                    /* if (weighBridgeDetails.weighMethod.equals("WS"))
                             vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, true)
                         else
                             vm.getWeighBridgeIdDetail(weighBridgeDetails.weighBridgeId, false)
                         vm.weighBridgeId.observe(viewLifecycleOwner, Observer { updateWeighbridgeValueUI(it) })*/
                    batchNo = createBatchNo()
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
                    binding.tvBatchnotitle.visibility = View.VISIBLE
                    binding.etBatchNo.visibility = View.VISIBLE
                    binding.tvParamsWeighBID.text = trucNo
                }
                STO -> {
                    lotDetails = it.getParcelable(LOT) ?: VegaCoffeeLot()
                    lotItems = it.getParcelableArrayList(LOT_LIST) ?: ArrayList()
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
                    trucNo = lotDetails.vehicleNumber
                    wbId = lotDetails.weighBridgeId
                    batchNo = lotDetails.batchNumber
                    isData = it.getBoolean(IS_PARAMS_VALUE, false)
                    materialNo = weighBridgeDetails.materialCode
                    netWeight = lotDetails.netWeight
                    tarWeight = lotDetails.bagWeight
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
                //binding.tvBatchnotitle.text = resources.getString(R.string.batch_no) + " : " + batchNo
                binding.etBatchNo.setText(weighBridgeDetails.challan, TextView.BufferType.EDITABLE)
                binding.etBatchNo.isEnabled = true
                binding.etBatchNo.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(2))
                vm.getQualityParams(materialNo!!, isData, wbId)
                //vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

                binding.btnReject.visible()
                binding.btnAccept.visible()
                binding.btnParamsProceed.gone()
                binding.tvType.text = SUPPLIER
                enableProceedBtn(weighBridgeDetails.status)
            }
            STO -> {
                binding.tvParamsWeighBID.text = trucNo
                vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
                when {
                    isData!! -> materialNo?.let {
                        when {
                            copiedWbid != "null" && copiedWbid.isNotEmpty() -> vm.getQualityParams(
                                copiedMaterial,
                                isData,
                                copiedWbid
                            )
                            else -> vm.getQualityParams(materialNo!!, isData, wbId)
                        }
                    }
                    else -> vm.getPreSamplingQualitydata(
                        lotbatchNo?.trim()!!,
                        lotmaterialNo?.trim()!!
                    )
                }
                //vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

                //if (weightmentType.equals(STO)) {
                binding.etBatchNo.filters = arrayOf<InputFilter?>(InputFilter.LengthFilter(11))
                binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)

                // }
                binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
                when {
                    wbType.equals(PROCURE) -> {
                        binding.btnReject.visible()
                        binding.btnAccept.visible()
                        binding.btnParamsProceed.gone()
                        binding.tvType.text = SUPPLIER
                    }
                    else -> {
                        binding.btnReject.gone()
                        binding.btnAccept.gone()
                        binding.btnParamsProceed.visible()
                        binding.tvType.text = MTNR
                    }
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
        val data: ArrayList<VegaQualityParamsWithQualitative?> = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                when {
                    itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty() -> {
                        when {
                            itValue?.qualityParameter?.formulaParam.equals("X") -> {
                                itValue?.qualityParameter?.mandatory = 0
                            }
                            else -> {
                                isValueNeed = false
                                missedPos.add(index)
                                itValue?.qualityParameter?.mandatory = 1
                            }
                        }

                    }
                    else -> {
                        itValue?.qualityParameter?.mandatory = 0
                    }
                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        when {
            isValueNeed -> {
                when (weightmentType) {
                    PROCURE -> {
                        when {
                            binding.etBatchNo.text.toString().isNotEmpty() -> {
                                showConfirmDialog(batchNo1!!, msg, finalApproval)
                            }
                            else -> showSnack(requireContext().resources.getString(R.string.batch_error))
                        }
                    }
                    else -> {
                        showConfirmDialog(lotDetails.batchNumber, msg, finalApproval)
                    }
                }
            }
            else -> {
                showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
                mAdapter.updateMissedPos(missedPos, data)
            }
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    it.forEach { item ->
                        if (item.qualityParameter.nameChar == "CI_SLOC_KOR") {
                            item.qualitative =
                                prepareQualitative(custonLocationList, item.qualityParameter.materialCode)
                        }

                    }
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
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
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }
                            if (item.qualityParameter.nameChar == "CI_COFFEE_TRANS_ORIGIN") {
                                item.qualitative?.forEach { item1 ->
                                    if (item1.charValue == origin) {
                                        item.qualityParameter.qualityParameterValue = origin
                                    }
                                }
                            }
                            if (item.qualityParameter.nameChar == "CI_COFFEE_TRANS_DEPT") {
                                item.qualitative?.forEach { item1 ->
                                    if (item1.charValue == department) {
                                        item.qualityParameter.qualityParameterValue = department
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
                getString(R.string.proceed),
                getString(R.string.cancel),
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

    private fun postQuality(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?,
        batchNo: String,
        finalApproval: String
    ) {

        when (weightmentType) {
            PROCURE -> {
                val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
                weighBridgeDetails.qualityDetails = qtyParams as List<VegaQuality>
                weighBridgeDetails.batchNumber = batchNo1
                weighBridgeDetails.finalApproval = finalApproval
                qualityPostList.clear()
                weighBridgeDetails.let {
                    qualityPostList.add(
                        VegaCoffeeLot(
                            item = weighBridgeDetails.item,
                            delivery = weighBridgeDetails.delivery.toString(),
                            customerNum = weighBridgeDetails.customerNum.toString(),
                            purchaseDocNum = weighBridgeDetails.purchaseDocNum.toString(),
                            purchaseDocDesc = weighBridgeDetails.purchaseDocDesc.toString(),
                            batchNumber = batchNo1 ?: "",
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
                            weighBridgeId = weighBridgeDetails.weighBridgeId,
                            challan = weighBridgeDetails.challan,
                            plant = weighBridgeDetails.plant,
                            direction = weighBridgeDetails.direction,
                            weighBridgeType = weighBridgeDetails.weighBridgeType,
                            erdat = weighBridgeDetails.erdat,
                            ertim = weighBridgeDetails.ertim,
                            qcStatus = weighBridgeDetails.qcStatus,
                            vehicleNumber = weighBridgeDetails.vehicleNumber,
                            storageLocationCode = weighBridgeDetails.storageLocationCode,
                            transportVendorCode = weighBridgeDetails.transportVendorCode,
                            contactNumber = weighBridgeDetails.contactNumber,
                            driverName = weighBridgeDetails.driverName,
                            grnNumber = weighBridgeDetails.grnNumber,
                            finalApproval = finalApproval,
                            qualityDetails = qtyParams
                        )
                    )
                }
                val editPlantDetails = getPlantDetails()
                if (etBatchNo.text.toString().isNotEmpty()) editPlantDetails.plantCode =
                    etBatchNo.text.toString()
                if (isOnline()) {
                    vm.postQualitySupplierParams(
                        VegaIndoCoffeeQualitySupplierParamPost(
                            grnApplicable = true,
                            grnFlag = false,
                            key = getCurrentKey(),
                            plant = editPlantDetails,
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
                            vehicleNumber = weighBridgeDetails.vehicleNumber.toString()
                        )
                    )
                } else {
                    weighBridgeDetails.challan = etBatchNo.text.toString()
                    saveWB(weighBridgeDetails.weighBridgeId, batchNo, "", 1)
                    saveData(qualityParameter, wbId)
                    moveToSuccessPage(this.wbId, "", "")
                }
            }
            STO -> {
                val qtyParams = qualityParameter.filter { it?.qualityParameterValue!!.isNotEmpty() }
                val lotCoutPost = lotItems.filter { it.batchNumber == lotDetails.batchNumber }
                val qcDoneLot = lotItems.filter { it.qcStatus.equals("X") }
                val isApplicableGrn = lotItems.size - 1 == qcDoneLot.size
                if (isOnline()) {
                    @Suppress("UNCHECKED_CAST")
                    lotDetails.qualityDetails = qtyParams as List<VegaQuality?>
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
                    vm.postQualityParams(
                        VegaIndoCoffeeQualityParamPost(
                            grnApplicable = isApplicableGrn,
                            grnFlag = false,
                            key = getCurrentKey(),
                            plant = getPlantDetails(),
                            lotDetails = qualityPostList,
                            bcMessage = "",
                            charg = "",
                            currentWbid = "",
                            errorMessage = "",
                            grnNumber = ""
                        )
                    )
                } else {
                    saveWB(weighBridgeDetails.weighBridgeId, batchNo.toString(), "", 1)
                    saveData(qualityParameter, wbId)
                    saveLotDetails()
                    moveToSuccessPage(this.wbId, "", "")
                }

            }
        }


    }

    private fun saveData(
        qualityParameter: ArrayList<VegaQualityParameter?>,
        wbId: String?
    ) {
        qualityParameter.forEachIndexed { index, it ->
            it?.wbid = wbId.toString()
            it?.position = index
            vm.saveQualityData(prepareVegaQualityData(it!!), batchNo.toString())
        }
    }

    private fun saveLotDetails() {
        /* val lotCoutPost = lotItems.filter { it.batchNumber.equals(lotDetails.batchNumber) }
         lotCoutPost.forEach {
             it.qualityFlag = false
             it.weighBridgeId = weighBridgeDetails.weighBridgeId
             it.tempId = weighBridgeDetails.weighBridgeId
         }*/
        lotItems.forEach {
            if (it.batchNumber.equals(lotDetails.batchNumber)) it.isOffline = true
            it.qualityFlag = false
            it.weighBridgeId = weighBridgeDetails.weighBridgeId
            it.tempId = weighBridgeDetails.weighBridgeId
        }
        vm.saveQualityLot(lotItems)
    }

    private fun updateSupplierQualityPostSuccess(response: Resource<GenericReqAndResp<VegaIndoCoffeeQualitySupplierParamPost>>) {

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
                            saveWB(
                                it.data?.data?.currentWbid.toString(),
                                batch.toString(),
                                msg.toString(),
                                4
                            )
                            saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                            it.data?.data.let {
                                it?.lotDetails.let {
                                    it?.forEach {
                                        it.let { it1 ->
                                            lotItems.forEach { it2 ->
                                                if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                    it1.qualityFlag
                                            }
                                        }
                                    }
                                }
                            }
                            saveWB(
                                weighBridgeDetails.weighBridgeId,
                                batchNo.toString(),
                                it.data?.message.toString(),
                                3
                            )
                            saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                        }
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    saveWB(weighBridgeDetails.weighBridgeId.toString(), batchNo.toString(), it.error.toString(), 3)
                    saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    //toast("${it.error}")
                }
            }
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaIndoCoffeeQualityParamPost>>) {
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
                            saveWB(
                                it.data?.data?.currentWbid.toString(),
                                batch.toString(),
                                msg.toString(),
                                4
                            )
                            saveData(qualityParameterList, it.data?.data?.currentWbid)
                        }
                        else -> {
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                            it.data?.data.let {
                                it?.lotDetails.let {
                                    it?.forEach {
                                        it.let { it1 ->
                                            lotItems.forEach { it2 ->
                                                if (it.batchNumber.equals(it1.batchNumber)) it2.qualityFlag =
                                                    it1.qualityFlag
                                            }
                                        }
                                    }
                                }
                            }
                            saveWB(
                                weighBridgeDetails.weighBridgeId,
                                batchNo.toString(),
                                it.data?.message.toString(),
                                3
                            )
                            saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                            saveLotDetails()
                        }
                        //toast("${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                    saveWB(
                        weighBridgeDetails.weighBridgeId,
                        batchNo.toString(),
                        it.error.toString(),
                        3
                    )
                    saveData(qualityParameterList, weighBridgeDetails.weighBridgeId)
                    saveLotDetails()
                    //toast("${it.error}")
                }
            }
        }
    }

    fun saveWB(weighBrideId: String, batchNo: String, message: String, status: Int) {
        weighBridgeDetails.batchNumber = batchNo
        weighBridgeDetails.wbTempId = weighBrideId
        weighBridgeDetails.status = status
        weighBridgeDetails.isErrorStatus = false
        weighBridgeDetails.isSyncStatus = status == 4

        when (weightmentType) {
            PROCURE -> if (status != 4) weighBridgeDetails.isOfflineData = true
            STO -> {
                if (status != 4 && lotItems.size - 1 == lotItems.map { it.isOffline == true }.size) weighBridgeDetails.isOfflineData =
                    true
            }
        }

        weighBridgeDetails.finalApproval = if (isAccept == true) FNQUALITY else FNREJECT
        weighBridgeDetails.message = message
        weighBridgeDetails.let { vm.saveWBDB(it) }
    }

    private fun moveToSuccessPage(currentWbid: String?, charg: String?, grnNo: String?) {
        val intent = Intent(activity, SuccessActivity::class.java)
        if (isAccept!!)
            intent.putExtra(AppUtils.TITLE, getString(R.string.quality_success))
        else
            intent.putExtra(AppUtils.TITLE, getString(R.string.quality_reject_success))
        if (!grnNo.isNullOrEmpty())
            intent.putExtra(
                AppUtils.SUB_TITLE,
                getString(R.string.new_lot_id_created).plus(lotDetails.batchNumber).plus("\n GRN No : ").plus(grnNo)
            )
        else if (grnNo.isNullOrEmpty()) {
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
        startActivity(intent)
        activity?.finish()
    }

    private fun createBatchNo(): String {
        val batchno: String =
            DateUtils.getDate(Calendar.getInstance().timeInMillis, "ddMMYY").toString()
        Log.d("CreateBatchNo", batchno.toString())
        return batchno
    }

    /*private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
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
                UIUtils.showErrorDialog(requireContext(), response.error.toString())
            }
        }
    }*/

}
