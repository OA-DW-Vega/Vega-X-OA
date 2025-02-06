package com.olam.warehouse.vegax.qualityindiacoffee.ui.params

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vega.model.VegaReceivingMtnWrapper
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.FragmentVegaIndiaCoffeeQualityParamsBinding
import com.olam.warehouse.vegax.qualityindiacoffee.ui.VegaIndiaCoffeeQualityViewModel
import com.olam.warehouse.vegax.qualityindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaIndiaCoffeeQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var batchNo1: String? = ""
    private var mergedBatchNumber: String? = ""
    private var receivedWeight: String? = ""
    private var delivery: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var supplierCode: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var trucNo: String? = ""
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private val valueBatchCreationDate = mutableListOf<VegaQualityParamsWithQualitative>()
    private var mAdapter = VegaIndiaCoffeeQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaIndiaCoffeeQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""


    interface OnParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            mergedBatchNumber: String,
            receivedWeight: String,
            finalApproval: String,
            materialCode: String,
            supplierCode: String,
            wbType: String?
        )

        fun replaceFgrnFragment(
            fragment: String,
            model: VegaQualityWBDetails,
            id: String,
            isThirdPartyMaterial: Boolean,
            currentMaterial: String
        )

    }

    companion object {
        fun newInstance() = VegaIndiaCoffeeQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaIndiaCoffeeQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_india_coffee_quality_params

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
        binding = FragmentVegaIndiaCoffeeQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaQualityParameterFragment").title("Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener {
            if((binding.tvNewLot.isVisible) && wbType.equals("STO"))
                proceeToPost(FNMTNRQUALITY)
            else if (wbType.equals("PROCURE"))
                proceeToPost(FNQUALITY)
            else
                showSnack(requireContext().resources.getString(R.string.assign_lot_to_proceed))
        }
        binding.tvFilter.setOnClickListener {
            if (!isSort) {
                mAdapter.upadteFilter(mAdapter.getItems())
                isSort = true
            }
        }

        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer { custonLocationList = it.filter { !it.storageLocationType.equals("P") }.toMutableList() })
        vm.getCustomLocations()
    }

    private fun initExtra() {
        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHBRIDGE)!!
            trucNo = weighBridgeDetails.vehicleNumber
            wbId = weighBridgeDetails.weighBridgeId
            batchNo = weighBridgeDetails.batchNumber
            batchNo1 = weighBridgeDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            materialNo = weighBridgeDetails.materialCode
            supplierCode = weighBridgeDetails.supplierCode
            netWeight = weighBridgeDetails.netWeight
            tarWeight = weighBridgeDetails.bagWeight
            challanNo = weighBridgeDetails.challan
            itemValue = weighBridgeDetails.item
            wbType = weighBridgeDetails.weighBridgeType
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }
        binding.tvParamsWeighBID.text = wbId

        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        vm.weighBridgeId.observe(viewLifecycleOwner, Observer {
            updateWeighbridgeValueUI(it)
        })
        if (isData!!) {
            materialNo?.let {
                if (!copiedWbid.equals("null") && copiedWbid.isNotEmpty())
                    vm.getQualityParams(copiedMaterial, isData, copiedWbid)
                else
                    vm.getQualityParams(materialNo!!, isData, wbId)
            }
        }else if(batchNo?.trim().isNullOrEmpty()){
            vm.getWeighBridgeIdDetail(wbId?.trim()!!, false)
        }else{
            vm.getPreSamplingQualitydata(batchNo?.trim()!!, materialNo?.trim()!!)
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if(!(batchNo?.trim().isNullOrEmpty())) {
            binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
            binding.etBatchNo.isEnabled = batchNo.isNullOrEmpty()
        }
        if (wbType.equals(PROCURE)) {
            binding.btnParamsProceed.visible()
            binding.tvType.text = SUPPLIER
        } else {
            binding.tvType.text = MTNR
            binding.btAssignLot.visibility = View.VISIBLE
        }
        enableProceedBtn(weighBridgeDetails.status)
        binding.btAssignLot.setOnClickListener { moveCreateLot() }

    }

    private fun moveCreateLot() {
        mListener.replaceFgrnFragment(
            FRAG_CREATE_LOT,
            weighBridgeDetails,
            materialNo.toString(),
            false,
            materialNo.toString()
        )
    }

    private fun enableProceedBtn(status: Int?) {

            binding.btnParamsProceed.isEnabled = true
            ViewCompat.setBackgroundTintList(
                binding.btnParamsProceed,
                context?.let {
                    ContextCompat.getColorStateList(
                        it,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                })

    }

    private fun proceeToPost(finalApproval: String) {
        var isValueNeed = true
//        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        //var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        var data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals("X")) || (!itValue?.qualityParameter?.preSampling.equals(""))) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (itValue?.qualityParameter?.formulaParam.equals("X") || itValue?.qualityParameter?.nameChar.equals(
                            "ZQM_BATCH_CREATION_DATE"
                        )
                    ) {
                        itValue?.qualityParameter?.mandatory = 0
                    } else if (itValue?.qualityParameter?.preSampling.equals("E") && (itValue?.qualityParameter?.nameChar.equals(
                            "Z_ACCEPTED_BAGS"
                        )
                                || itValue?.qualityParameter?.nameChar.equals("Z_LOT_MERGE")
                                || itValue?.qualityParameter?.nameChar.equals("Z_REJECTED_BAGS"))
                    ) {
                        itValue?.qualityParameter?.mandatory = 0
                    } else {
                        isValueNeed = false
                        missedPos.add(index)
                        itValue?.qualityParameter?.mandatory = 1
                    }

                } else {
                    itValue?.qualityParameter?.mandatory = 0
                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }

        if(valueBatchCreationDate.size>0){
            valueBatchCreationDate.get(0).qualityParameter.qualityParameterValue = DateUtils.getFormattedCurrentDate()
            qualityParameterList.add(valueBatchCreationDate.get(0).qualityParameter)
        }

        if (isValueNeed)
            showConfirmDialog(finalApproval)
        else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {

                                if ((!(item.qualityParameter.nameChar.equals("Z_ACCEPTED_BAGS")
                                            || item.qualityParameter.nameChar.equals("Z_LOT_MERGE")
                                            || item.qualityParameter.nameChar.equals("Z_REJECTED_BAGS"))) && wbType.equals(
                                        "STO"
                                    )
                                )
                                    value.add(item)
                                else if (wbType.equals("PROCURE")) {
                                    if((!(item.qualityParameter.nameChar.equals("ZQM_BATCH_CREATION_DATE")))){
                                                value.add(item)
                                    }else{
                                        valueBatchCreationDate.add(item)
                                    }
                                }

                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue =
                                        item1.satNam!!.split(" ")[0]
                                    if ((!(item.qualityParameter.nameChar.equals("Z_ACCEPTED_BAGS")
                                                || item.qualityParameter.nameChar.equals("Z_LOT_MERGE")
                                                || item.qualityParameter.nameChar.equals("Z_REJECTED_BAGS"))) && wbType.equals(
                                            "STO"
                                        )
                                    )
                                        value.add(item)
                                    else if (wbType.equals("PROCURE"))
                                        value.add(item)
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
                else -> {
                    binding.btnParamsProceed.isEnabled = false
                    binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
                    showErrorDialogWithFAQLink(requireContext(),"Quality params not available for this material")
                    //setErrorContentView("Quality params not available for this material")
                }
            }
        }
    }

    private fun updateWeighbridgeValueUI(response: Resource<GenericReqAndResp<VegaReceiving>>?) {
        when (response?.status) {
            Resource.Status.SUCCESS -> {
                binding.etBatchNo.setText(response.data?.data?.batchNumber?.trim()!!, TextView.BufferType.EDITABLE)
                delivery = response.data?.data?.delivery?.trim()!!
                if (wbType.equals("STO")){
                    fetchMtnDetails()
                }else {
                    vm.getPreSamplingQualitydata(response.data?.data?.batchNumber?.trim()!!, materialNo?.trim()!!)
                }

            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                showErrorDialogWithFAQLink(requireContext(), response.error.toString())
            }
            else -> {}
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
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
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

    private fun fetchMtnDetails() {
        vm.warehouse.observe(viewLifecycleOwner, Observer { processApiResult(it) })
        vm.fetchWarehouseWithMtns()
    }

    private fun processApiResult(data: Resource<GenericReqAndResp<VegaReceivingMtnWrapper>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> saveResult(it.data)
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> showErrorDialogWithFAQLink(requireContext(), it.error.toString())
            }
        }
    }

    private fun saveResult(data: GenericReqAndResp<VegaReceivingMtnWrapper>?) {
        data?.data?.let {
            binding.etBatchNo.isEnabled = (binding.etBatchNo.text.trim().toString()).isNullOrEmpty()
            val mtns = data.data.batchDetails.filter { it.batch == binding.etBatchNo.text.trim().toString() && it.mtnNumber == delivery}
            mtns.forEach{
                 receivedWeight = it.weight.toString()
             }
            vm.getPreSamplingQualitydata(binding.etBatchNo.text.trim().toString(), materialNo?.trim()!!)
        }
    }


    private fun moveTosummary(batchNo: String, finalApproval: String) {
        materialNo?.let {
            supplierCode?.let { it1 ->
                mListener.onParamsProceed(
                    qualityParameterList,
                    wbId,
                    batchNo,
                    mergedBatchNumber!!,
                    receivedWeight!!,
                    finalApproval,
                    it,
                    it1,
                    wbType
                )
            }
        }
    }

    private fun showConfirmDialog(finalApproval: String) {
        moveTosummary(batchNo1!!, finalApproval)
        /*MaterialDialog(requireContext()).show {
            message(msg)
            positiveButton(text = UIUtils.getSpannedText("Proceed", true)) {
                *//*qualityParameterList.clear()
                mAdapter.getItems().forEach { qualityParameterList.add(it?.qualityParameter) }*//*
                mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval)
            }
            negativeButton(text = UIUtils.getSpannedText("Cancel", false)) {
                dismiss()
            }
        }*/
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 -> if (it1.isNotEmpty()) isEnable = true }
        }

            binding.btnParamsProceed.isEnabled = true
        binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
    }

    fun updateLotDetails(bundle: Bundle) {
        val isCreateNewLot = bundle.getBoolean(CREATE_NEW_LOT)
        val newLot = bundle.getString(LOT_ID)
        val stockList = bundle.getParcelableArrayList(STOCK_LIST) ?: ArrayList<VegaCoffeeRminLots>()
        if (isCreateNewLot) {
            binding.tvNewLot.visible()
            binding.tvNewLot.text = getString(R.string.lot_id).plus(" ").plus(newLot)
            batchNo1 = newLot.toString()
            mergedBatchNumber = ""
        } else {
            binding.tvNewLot.visible()
            binding.tvNewLot.text = getString(R.string.lot_id).plus(" ").plus(stockList[0].batchNumber)
//            binding.flAssignLot.visible()
//            binding.tvNewLot.gone()
//            binding.icAssignLotCard.cbLotId.gone()
//            binding.icAssignLotCard.tvLotNoValue.text = stockList[0].batchNumber
//            binding.icAssignLotCard.tvStorageValue.text = stockList[0].storageLocationCode
//            binding.icAssignLotCard.tvElWeightValue.text = stockList[0].weight.plus("KG")
//            binding.icAssignLotCard.tvWeightValue.text = stockList[0].editedWeight.plus(" ").plus("KG")
            batchNo1 = binding.etBatchNo.text.toString()
            mergedBatchNumber = stockList[0].batchNumber
        }

        /* currentGrade.isDefaultLot = false
         currentGrade.isCreateNewLot = bundle.getBoolean(CREATE_NEW_LOT)
         currentGrade.lotStorageLocationCode = bundle.getString(STORAGE_LOC) ?: ""
         val stockList = bundle.getParcelableArrayList(STOCK_LIST) ?: ArrayList<VegaCoffeeRminLots>()
         if (stockList.size > 0) {
             weighBridgeDetails.batchNumber = stockList[0].batchNumber
             currentGrade.weight = stockList[0].weight
             val lotWeight = stockList[0].weight ?: "0.0"
             currentGrade.eligibeWeight =
                 if (!eligibleWeight.equals(0.0)) eligibleWeight.minus(lotWeight.toDouble())
                     .formatThreeDigits() else "0.0"
             weighBridgeDetails.unitsOfMeasure = stockList[0].unitOfMeasure
             currentGrade.lotStorageLocationCode = stockList[0].storageLocationCode
         } else {
             currentGrade.eligibeWeight = eligibleWeight.toString()
             if (!bundle.getBoolean(CREATE_NEW_LOT)) currentGrade.batchNumber =
                 bundle.getString(LOT_ID) ?: ""
         }
         vm.saveFgrnGrade(currentGrade)*/
    }

}
