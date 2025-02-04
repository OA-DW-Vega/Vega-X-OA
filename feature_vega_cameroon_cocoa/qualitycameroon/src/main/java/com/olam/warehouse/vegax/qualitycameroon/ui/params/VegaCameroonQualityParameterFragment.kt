package com.olam.warehouse.vegax.qualitycameroon.ui.params

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.databinding.FragmentVegaCameroonQualityParamsBinding
import com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityViewModel
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaCameroonQualityParameterFragment : BaseFragment() {

    private var wbId: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var batchNo: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var itemValue: String? = ""
    private var wbType: String? = ""
    private var weighBridgeDetails = VegaQualityWBDetails()
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaCameroonQualityParamsAdapter { enableProceedBtn(it) }
    private var isSort: Boolean = false
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaCameroonQualityViewModel by viewModel()
    private lateinit var mListener: OnParamsListener
    private var paidWeight: String = ""
    private var grnQty: Double = 0.0
    private var refraction: Double = 0.0
    private var discWeight: Double = 0.0
    private var grnNumber: String? = ""
    private var usageDecision: VegaQualityParameter = VegaQualityParameter()
    private var grnQtyQualityParam: VegaQualityParameter = VegaQualityParameter()
    private var qualityMasterData = mutableListOf<VegaQualityParamsWithQualitative>()

    interface OnParamsListener {

fun onProceed(
    qualityParameter: ArrayList<VegaQualityParameter?>,
    wbId: String?,
    batchNo: String,
    finalApproval: String,
    challan: String
)
    }

    companion object {
        fun newInstance() = VegaCameroonQualityParameterFragment().putArgs {}
    }

    private lateinit var binding: FragmentVegaCameroonQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_quality_params

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnParamsListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitycameroon/ui/params/VegaCameroonQualityParameterFragment")
            .title("Vega_Cameroon/Quality").with(tracker)
        initUI()
        initExtra()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAccept, it, true)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
        }
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
    }

    private fun initExtra() {
        arguments?.let {
            weighBridgeDetails = it.getParcelable(WEIGHSCALE)!!
            wbId = weighBridgeDetails.weighBridgeId
            batchNo = weighBridgeDetails.batchNumber
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
        }
        binding.tvParamsWeighBID.text = getString(R.string.sample_id).plus(" ").plus(challanNo)
        if(!grnNumber.isNullOrEmpty() && !grnNumber!!.contains("TMP"))
            binding.tvGrn.text = getString(com.olam.warehouse.presentation.R.string.grn).plus(grnNumber)
        else binding.tvGrn.gone()
        binding.etBatchNo.setText(batchNo, TextView.BufferType.EDITABLE)
        binding.etBatchNo.isEnabled = false

        materialNo?.let {
            vm.getQualityParams(materialNo!!, isData, wbId)
            vm.qualitylist.observe(viewLifecycleOwner, Observer {
                qualityMasterData = it.filter { it.qualityParameter.materialCode == materialNo }.toMutableList()
                updateUI(it)
            })
        }
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if (wbType.equals(PROCURE)) {
            binding.tvType.text = (" ").plus(getString(R.string.supplier))
        } else {
            binding.tvType.text = MTNR
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

    private fun proceedToPost(finalApproval: String, msg: Int) {
        var isValueNeed = true
        qualityParameterList.clear()
        val missedPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
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
            }
            qualityParameterList.add(itValue?.qualityParameter)
        }
        qualityParameterList.add(grnQtyQualityParam)

        if (isValueNeed){
            qualityParameterList.forEach {quality ->
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
            moveToSummaryScreen(batchNo!!,  finalApproval)
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }

    private fun moveToSummaryScreen(batchNo: String,  finalApproval: String) {

        mListener.onProceed(qualityParameterList, wbId, batchNo, finalApproval, challanNo.toString())

    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->

                            if (item.qualityParameter.nameChar.equals("LOBM_UDCODE")) {
                                item.qualityParameter.qualityParameterValue = "OL-RM    A"
                                item.qualityParameter.qualitative = emptyList()
                                usageDecision = item.qualityParameter
                            }
                            if (item.qualityParameter.nameChar.equals("B_GRNQTY1")) {
                                item.qualityParameter.qualityParameterValue = weighBridgeDetails.netWeight
                                item.qualityParameter.qualitative = emptyList()
                                grnQtyQualityParam = item.qualityParameter
                            }
                            if (!(item.qualityParameter.preSampling.equals("X"))
                            ) {
                                value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
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
}
