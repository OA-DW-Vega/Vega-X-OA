package com.olam.warehouse.vegax.ppqcameroon.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqcameroon.R
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotParams
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqPostResponse
import com.olam.warehouse.vegax.ppqcameroon.databinding.FragmentVegaCameroonPpqQualitySummaryBinding
import com.olam.warehouse.vegax.ppqcameroon.utils.*
import kotlinx.android.synthetic.main.item_vega_cameroon_ppq_quality_summary_params.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaCameroonPpqQualitySummaryFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_ppq_quality_summary
    private val vm: VegaCameroonPpqViewModel by viewModel()

    private lateinit var binding: FragmentVegaCameroonPpqQualitySummaryBinding
    private lateinit var mListener: OnSummaryParamsListener
    private var lotDetails = VegaCameroonPpqInspectionLotDetails()
    private var qualityParameterList = arrayListOf<VegaCameroonPpqInspectionLotParams?>()
    private var wbid: String? = ""
    private var batchNo: String? = ""
    private var finalApproval: String? = ""
    private var challanNo: String? = ""

    private var isAccept: Boolean = false

    interface OnSummaryParamsListener {
        fun onParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            challan: String
        )

        fun onMtnrParamsProceed(
            qualityParameter: ArrayList<VegaQualityParameter?>,
            wbId: String?,
            batchNo: String,
            finalApproval: String,
            lotItems: ArrayList<VegaCoffeeLot>
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCameroonPpqQualitySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        fun newInstance() = VegaCameroonPpqQualitySummaryFragment().putArgs {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqcameroon/ui/VegaCameroonPpqQualitySummaryFragment")
            .title("Vega_Cameroon/PPQ")
            .with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnSummaryParamsListener
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAccept, it, true)
        }
        wbid = arguments?.getString(INSPECTION_LOT)
        batchNo = arguments?.getString(BATCH_NO)
        finalApproval = ""
        challanNo = ""
        lotDetails =
            arguments?.getParcelable<VegaCameroonPpqInspectionLotDetails>("LOT_LIST") as VegaCameroonPpqInspectionLotDetails

        binding.tvApproveParamsWeighBID.text = wbid
        binding.tvBatchNo.text = batchNo
        binding.btnAccept.setOnClickListener {
            val params = lotDetails.qualityParameters.filter { it.formula.isEmpty() }
            val isParamValue = params.any { it.MeanValue.isNullOrEmpty() }
            when (!isParamValue) {
                true -> showConfirmDialog(
                    batchNo.toString(),
                    getString(R.string.accept_quality),
                    ACCEPT
                )
                else -> {
                }
            }
        }
        binding.btnReject.setOnClickListener {
            val params = lotDetails.qualityParameters.filter { it.formula.isEmpty() }
            val isParamValue = params.any { it.MeanValue.isNullOrEmpty() }
            when (!isParamValue) {
                true -> showConfirmDialog(
                    batchNo.toString(),
                    getString(R.string.reject_quality),
                    REJECT
                )
                else -> {

                }
            }

        }
        vm.postLotDetails.observe(viewLifecycleOwner, Observer { updateUIResponse(it) })

        setUpAdapter(lotDetails.qualityParameters)
    }

/*
    private fun setUpAdapter(items: MutableList<VegaCameroonPpqInspectionLotParams>) {
        paramList.forEach {
            if(it.formula.isNullOrEmpty())
                nformulaParamList.add(it)
            println("nf ==> ${nformulaParamList}")
        }
        binding.rvPpqParams.setUp(nformulaParamList, R.layout.item_vega_cameroon_ppq_params, { it, pos ->

            //tvQualityName.text = it.charDesc
            val spannable: SpannableStringBuilder?
            spannable = SpannableStringBuilder(it.charDesc)
            spannable.insert(spannable.length, "")
            tvUnit.text = it.meansUnit
            if (it.qualitative.isNotEmpty()) {
                llDropDown.visible()
                llEdit.gone()
                if (it.mandatory?.equals(1)!!) llDropDown.let {
                    ViewCompat.setBackground(
                        it,
                        ContextCompat.getDrawable(
                            context,
                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                        )
                    )
                }
                else llDropDown.let {
                    ViewCompat.setBackground(
                        it,
                        ContextCompat.getDrawable(
                            context,
                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                        )
                    )
                }
                val charValue = mutableListOf<String>()
                charValue.add(0, "Select")

*/
/*                qualityMasterData.forEach { masterQuality ->
                        if(it.charDesc.contains(masterQuality.qualityParameter.nameChar.split("_")[1],true)){
                            masterQuality.qualitative?.map { data -> data.descValue }?.let { it1 -> charValue.addAll(it1) }
                        } }*//*

                when(it.charDesc){
                    "CLASSEMENT" ->
                    {
                        charValue.addAll(it.qualitative.filter{it.codeGroup == "CLASCOCO"}.map { data -> data.codeTxt })
                    }
                    "Smoky / Non Smoky" -> {
                        charValue.addAll(it.qualitative.filter{it.codeGroup == "IR-AB/PR"}.map { data -> data.codeTxt })
                    }
                }
//                var spinnerValues = charValue.distinct()
//                charValue.addAll(it.qualitative.map { data -> data.codeTxt })
                val charValueAdapter =
                    ArrayAdapter(spItem.context, android.R.layout.simple_list_item_1, charValue)
                spItem.adapter = charValueAdapter
                if (!it.MeanValue.isNullOrEmpty())
                    spItem.setSelection(charValue.indexOf(it.MeanValue))
                spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos1: Int, p3: Long) {
                        if (pos1 > 0) {
                            nformulaParamList[pos].MeanValue = charValue[pos1]
                            nformulaParamList[pos].code = it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                            nformulaParamList[pos].Code1 = it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                            nformulaParamList[pos].codeGroup =
                                it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].codeGroup
                            nformulaParamList[pos].CodeGrp1 =
                                it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].codeGroup
//                            binding.rvPpqParams.adapter?.notifyDataSetChanged()
                        }
                    }
                }

            } else {
                handleScroll(edQualityValue)
                llDropDown.gone()
                llEdit.visible()

                if (it.mandatory?.equals(1)!!) llEdit.let {
                    ViewCompat.setBackground(
                        it,
                        ContextCompat.getDrawable(
                            context,
                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                        )
                    )
                }
                else {
//                    if (!it.formula.isNullOrEmpty())
//                        llEdit.let {
//                            ViewCompat.setBackground(
//                                it,
//                                ContextCompat.getDrawable(
//                                    context,
//                                    com.olam.warehouse.presentation.R.drawable.custom_edit_text
//                                )
//                            )
//                        }
//                    else
                    llEdit.let {
                        ViewCompat.setBackground(
                            it,
                            ContextCompat.getDrawable(
                                context,
                                com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                            )
                        )
                    }
                }

//                if (!it.formula.isNullOrEmpty()) {
//                    edQualityValue.isEnabled = false
//                    tvQualityName.isEnabled = false
//                    var result = evaluateFormula(it.formula,mapInputParameters)
//                    edQualityValue.setText(result.toString())
//                    tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.grey_border))
//                } else {
//                    edQualityValue.isEnabled = true
//                    tvQualityName.isEnabled = true
//                    tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.black))
//                }
                edQualityValue.isEnabled = true
                tvQualityName.isEnabled = true
                tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.black))
                if (!it.quantitative.lowerLimit.isNullOrEmpty()) {
                    spannable.insert(
                        spannable.length,
                        "\n(".plus(it.quantitative.lowerLimit.trim()).plus(" - ")
                            .plus(it.quantitative.upperLimit.trim()).plus(
                                ")"
                            )
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.LTGRAY),
                        it.charDesc.length + 1,
                        spannable.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
                edQualityValue.setText(it.MeanValue, TextView.BufferType.EDITABLE)
                val lowerLimit =
                    if (!it.quantitative.lowerLimit.isNullOrEmpty()) it.quantitative.lowerLimit.trim().replace(
                        ",",
                        "."
                    ).toDouble() else 0.00
                val upperLimit =
                    if (!it.quantitative.upperLimit.isNullOrEmpty()) it.quantitative.upperLimit.trim().replace(
                        ",",
                        "."
                    ).toDouble() else 0.00
                edQualityValue.onChange { text ->
                    try {
                        if (text.isEmpty()) {
                            nformulaParamList[pos].MeanValue = ""
                            return@onChange
                        }
                        val editVal: String?
                        val dot = text[0].toString()
                        editVal = if (dot == ".") {
                            if (text.length == 1) "0.00" else "0".plus(text)
                        } else text

                        if (upperLimit > 0) {
                            if (validateRangeParamValue(editVal, lowerLimit, upperLimit)) {
                                nformulaParamList[pos].MeanValue = editVal
                                mapInputParameters.put("C".plus(nformulaParamList[pos].Inspchar), nformulaParamList[pos].MeanValue)
                            } else {
                                edQualityValue.setError("value out of range!", null)
                                mapInputParameters.put("C".plus(nformulaParamList[pos].Inspchar), "0")
                            }
                        } else {
                            nformulaParamList[pos].MeanValue = editVal
                            mapInputParameters.put("C".plus(nformulaParamList[pos].Inspchar), nformulaParamList[pos].MeanValue)
                        }
                        nformulaParamList.forEach {
                        }

                        binding.rvPpqParamsFormula.adapter?.notifyDataSetChanged()

                    } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
                }
            }
            tvQualityName.text = spannable

        }, {

        })
    }
*/

    private fun setUpAdapter(data: List<VegaCameroonPpqInspectionLotParams>) {
        data.let {
            qualityParameterList = it as ArrayList<VegaCameroonPpqInspectionLotParams?>
            var i = 0
            binding.rvApproveQuality.setUp(qualityParameterList,
                R.layout.item_vega_cameroon_ppq_quality_summary_params,
                { item, pos ->
                    i++
                    if (i % 2 == 0) {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    tvQualityNameApprove.text = item?.charDesc
                    if (item?.qualitative?.size!! > 0) {
                        tvUnitApprove.text = item.MeanValue
                        //item.qualitative!!.filter { it.descValue == item.qualityParameterValue }.map { it.charValue }
                    } else
                        tvUnitApprove.text = item.MeanValue

                },
                {

                })
        }
    }


    private fun showConfirmDialog(batchNo: String, msg: String, postType: String) {
        MaterialDialog(requireContext()).show {
            message(text = msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    when (postType) {
                        ACCEPT -> isAccept = true
                        REJECT -> isAccept = false
                    }
                    postQuality(postType)
                },
                { dismiss() })
        }
    }

    private fun postQuality(postType: String) {
        when (postType) {
            ACCEPT -> lotDetails.usageDecision = true
            REJECT -> lotDetails.usageDecision = false
        }
        vm.saveInspectionLotDetails(lotDetails)
    }

    private fun updateUIResponse(response: Resource<GenericReqAndResp<VegaCameroonPpqPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            it.data?.data?.let { it1 -> moveToSuccess(it1) }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

    private fun moveToSuccess(data: VegaCameroonPpqPostResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isAccept) intent.putExtra(AppUtils.TITLE, getString(R.string.success_accept))
        else intent.putExtra(AppUtils.TITLE, getString(R.string.success_reject))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(": ").plus(batchNo))
        startActivity(intent)
        requireActivity().finish()
    }

}


