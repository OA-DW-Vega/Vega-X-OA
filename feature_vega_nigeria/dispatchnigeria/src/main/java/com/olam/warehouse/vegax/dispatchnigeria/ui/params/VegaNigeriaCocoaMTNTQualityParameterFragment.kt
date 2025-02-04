package com.olam.warehouse.vegax.dispatchnigeria.ui.params

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchWB
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.dispatchnigeria.R
import com.olam.warehouse.vegax.dispatchnigeria.data.domain.model.*
import com.olam.warehouse.vegax.dispatchnigeria.databinding.FragmentVegaNigeriaCocoaMtntQualityParamsBinding
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaMtntViewModel
import com.olam.warehouse.vegax.dispatchnigeria.ui.VegaNigeriaCocoaReplaceFragmentCallback
import com.olam.warehouse.vegax.dispatchnigeria.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaCocoaMTNTQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var mergedBatchNumber: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNigeriaCocoaMTNTQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var plantId = ""
    private var weightedAvgList = arrayListOf<VegaNigeriaCocoaWeightedAverageDetails>()
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaNigeriaCocoaMtntViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var paidWeight: String = ""
    private var grnQty: Double = 0.0
    private var refraction: Double = 0.0
    private var discWeight: Double = 0.0
    private lateinit var callback: VegaNigeriaCocoaReplaceFragmentCallback
    private var grnNumber: String? = ""
    private var storageLocationCode: String = ""
    private var grnQtyQualityParam: VegaQualityParameter = VegaQualityParameter()
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()
    private var summaryObj: VegaCocoaDispatchWB? = null
    private var weightedAverageList = ArrayList<VegaNigeriaCocoaWeightedAverageDetails>()
    private var qualityDetails: List<VegaNigeriaCocoaQualityDetails> = emptyList()

    interface OnParamsListener {
        fun onParamsProceedQuality(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            storageLocationCode: String
        )

        fun replaceFragment(
            receivingType: String,
            data: Any
        )
    }

    companion object {
        fun newInstance(
            materialCode: String,
            mergedBatchNumber: String,
            model: VegaCocoaDispatchWB,
            modelNew: ArrayList<VegaNigeriaCocoaWeightedAverageDetails>
        ) =
            VegaNigeriaCocoaMTNTQualityParameterFragment().putArgs {
                putString(MATERIAL_CODE, materialCode)
                putString(MERGED_BATCHNUMBER, mergedBatchNumber)
                putParcelable(MTNTQUALITY_DATA, model)
                putParcelableArrayList(WEIGHTED_AVERAGE_LIST, modelNew)
            }
    }

    private lateinit var binding: FragmentVegaNigeriaCocoaMtntQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nigeria_cocoa_mtnt_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
        callback = context as VegaNigeriaCocoaReplaceFragmentCallback
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
    ): View? {
        binding = FragmentVegaNigeriaCocoaMtntQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualityecuador/ui/params/VegaEcuadorQualityParameterFragment")
            .title("Ecuador Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.btnParamsProceed.setOnClickListener {
            proceedToPost(
                FNQUALITY,
                R.string.confirm_quality_message
            )
        }
        binding.btnAccept.setOnClickListener {
            proceedToPost(
                FNQUALITY,
                R.string.confirm_quality_message
            )
        }
        binding.btnReject.setOnClickListener {
            proceedToPost(
                FNREJECT,
                R.string.confirm_reject_message
            )
        }

        vm.wsWeightedAvgPost.observe(viewLifecycleOwner, Observer { updateWeightedAvgUI(it) })
    }

    private fun initExtra() {
        materialNo = arguments?.getString(MATERIAL_CODE) ?: ""
        mergedBatchNumber = arguments?.getString(MERGED_BATCHNUMBER) ?: ""
        summaryObj = arguments?.getParcelable(MTNTQUALITY_DATA)
        weightedAverageList =
            arguments?.getParcelableArrayList<VegaNigeriaCocoaWeightedAverageDetails>(
                WEIGHTED_AVERAGE_LIST
            ) as ArrayList

        /*arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            wbId = weighBridgeDetails.weighBridgeId
            batchNo = weighBridgeDetails.batchNumber
            isData = it.getBoolean(IS_PARAMS_VALUE, false)
            //materialNo = weighBridgeDetails.materialCode
            netWeight = weighBridgeDetails.netWeight
            tarWeight = weighBridgeDetails.bagWeight
            challanNo = weighBridgeDetails.challan
            itemValue = weighBridgeDetails.item
            wbType = weighBridgeDetails.weighBridgeType
            bagCount = weighBridgeDetails.bagCount
            plant = weighBridgeDetails.plant
            grnNumber = weighBridgeDetails.grnNumber
            storageLocationCode = weighBridgeDetails.storageLocationCode
        }
        binding.tvParamsWeighBID.text = getString(R.string.wb_id).plus(" ").plus(wbId)
        if(!grnNumber.isNullOrEmpty() && !grnNumber!!.contains("TMP"))
            binding.tvGrn.text = getString(com.olam.warehouse.presentation.R.string.grn).plus(grnNumber)
        else binding.tvGrn.gone()*/
        //binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        // binding.etBatchNo.isEnabled = false

        /*vm.qualityDetailsNew.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        vm.getQualityDetails(mergedBatchNumber!!, materialNo!!)*/
        binding.etBatchNo.setText(mergedBatchNumber, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = false

        materialNo?.let {
            vm.getQualityParams(materialNo!!, isData, wbId)
            vm.qualitylist.observe(viewLifecycleOwner, Observer {
                qualityMasterData =
                    it.filter { it.qualityParameter.materialCode == materialNo }.toMutableList()
                updateUI(it)
            })
        }
        //  vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbType.equals(PROCURE)) {
            binding.tvType.text = (" ").plus(getString(R.string.supplier))
        } else {
            binding.tvType.text = MTNTNEW
        }
        enableProceedBtn(weighBridgeDetails.status)

    }

    private fun enableProceedBtn(status: Int?) {

        when (status) {
            4 -> {
                binding.btnParamsProceed.isEnabled = false
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
                ViewCompat.setBackgroundTintList(
                    binding.btnParamsProceed,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.grey
                        )
                    }
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
                            com.olam.warehouse.presentation.R.color.green
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnAccept,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.green
                        )
                    }
                )
                ViewCompat.setBackgroundTintList(
                    binding.btnReject,
                    context?.let {
                        ContextCompat.getColorStateList(
                            it,
                            com.olam.warehouse.presentation.R.color.red
                        )
                    }
                )
            }
        }
    }

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        mAdapter.itemCount
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.preSampling.isNullOrEmpty())
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
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        // qualityParameterList.add(grnQtyQualityParam)

        /* if(binding.etBatchNo.text.toString()!!.length < 10)
             showSnack(requireContext().resources.getString(R.string.batch_no_should_be_10_digit))*/

        if (isValueNeed) {
            qualityParameterList.forEach { quality ->
                qualityMasterData.forEach {
                    if (!it.qualitative.isNullOrEmpty()) {
                        it.qualitative?.forEach { item ->
                            if (quality?.nameChar == item.nameChar && quality.qualityParameterValue == item.descValue) {
                                quality.qualityParameterValue = item.charValue
                            }
                        }
                    }
                }
            }
            showConfirmDialog("", msg, finalApproval)
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun updatePreQuality(data: Resource<GenericReqAndResp<List<VegaQualityApproveNigeria>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            preQualityList =
                                it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, isData, "") }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun updateWeightedAvgUI(response: Resource<GenericReqAndResp<VegaNigeriaCocoaWeightedAverageResponse>>) {

        when (response.status) {
            Resource.Status.SUCCESS -> {
                hideLoading()
                response.data?.data?.let { it1 ->
                    plantId = it1.imWerks.toString()
                    // weightedAvgList = it1.weightedAvg as ArrayList<VegaNigeriaCocoaWeightedAverageDetails>

                    /* if(dispatchLotsList.size>0) {
                         callback.replaceFragment(PARAMS_LIST, dispatchLotsList.get(0).materialCode,mergedBatchNumber,vm.dispatchWh,weightedAvgList)
                     }*/
                    callback.replaceFragmentNew(WEIGHSCALE_SUMMARY, summaryObj)
                }
            }
            Resource.Status.LOADING -> showLoading()
            Resource.Status.ERROR -> {
                hideLoading()
                callback.replaceFragmentNew(WEIGHSCALE_SUMMARY, summaryObj)
                //UIUtils.showErrorDialog(requireContext(), response.error.toString())
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

                            if (item.qualityParameter.nameChar.equals("B_GRNQTY1")) {
                                item.qualityParameter.qualityParameterValue =
                                    weighBridgeDetails.netWeight
                                item.qualityParameter.qualitative = emptyList()
                                grnQtyQualityParam = item.qualityParameter
                            }

                            /*if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }*/

                            /*  if (!(item.qualityParameter.preSampling.equals("X"))
                              ) {
                                  value.add(item)
                              }*/

                            /*preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }*/

                            weightedAverageList.forEach { item1 ->
                                if ((item.qualityParameter.nameChar == item1.atnam) && (!(item.qualityParameter.preSampling.equals(
                                        "X"
                                    )))
                                ) {
                                    item.qualityParameter.qualityParameterValue =
                                        item1.atwrt.toString().trim()
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
                        challanNo,
                        bagCount,
                        plant,
                        isData!!
                    )
                }
                else -> setErrorContentView(getString(R.string.quality_params_not_available))
            }
        }
    }

    private fun showConfirmDialog(batchNo: String, msg: Int, finalApproval: String) {
        //callback.replaceFragment(WEIGHSCALE_SUMMARY, summaryObj,"")

        var list = ArrayList<VegaNigeriaCocoaWeightedAverageDeliveryDetail>()

        var qualityList = ArrayList<VegaNigeriaCocoaQualityDetails>()
        qualityParameterList.forEach {
            var item = VegaNigeriaCocoaQualityDetails()
            item.sapQCName = it?.nameChar
            item.sapQCDesc = it?.descrChar
            item.satNam = it?.qualityParameterValue
            qualityList.add(item)
        }
        var deliveryDetail = VegaNigeriaCocoaWeightedAverageDeliveryDetail()
        deliveryDetail.batchNumber = mergedBatchNumber
        deliveryDetail.materialCode = materialNo
        deliveryDetail.qualityDetails = qualityList
        list.add(deliveryDetail)

        vm.postWeightedAverage(
            (VegaNigeriaCocoaWeightedAveragePost(
                key = getCurrentKey(),
                batchUpdateFlag = true,
                weightedAvgFlag = false,
                plant = getPlantDetails(),
                deliveryDetails = list
            ))
        )


        //    mListener.onParamsProceedQuality(qualityParameterList, wbId, batchNo, finalApproval,storageLocationCode)


        /* MaterialDialog(requireContext()).show {
             message(msg)
             positiveButton(text = UIUtils.getSpannedText(getString(R.string.proceed), true)) {
                 //calculatePaidWeight()
                 mListener.onParamsProceed(qualityParameterList, wbId, batchNo, finalApproval)
             }
             negativeButton(text = UIUtils.getSpannedText(getString(R.string.cancel), false)) {
                 dismiss()
             }
         }*/
    }

    private fun calculatePaidWeight() {
        qualityParameterList.forEach { it ->
            if (it?.nameChar.equals("B_GRNQTY1")) {
                grnQty = it?.qualityParameterValue?.toDouble() ?: 0.0
            } else if (it?.nameChar.equals("B_SECONDARY_REFR")) {
                refraction = it?.qualityParameterValue?.toDouble() ?: 0.0
            }
        }
        discWeight = 100 - refraction
        paidWeight = ((discWeight * grnQty) / 100).formatThreeDigits()
    }

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 ->
                if (it1.isNotEmpty()) isEnable = true
            }
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
}
