package com.olam.warehouse.vegax.ppqindiacoffee.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityPreParameter
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqindiacoffee.R
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLotParams
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqindiacoffee.data.domain.model.VegaIndiaCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqindiacoffee.databinding.FragmentVegaIndiaCoffeePpqParamsBinding
import com.olam.warehouse.vegax.ppqindiacoffee.utils.*
import kotlinx.android.synthetic.main.item_vega_india_coffee_ppq_params.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaIndiaCoffeePpqQualityParamsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_india_coffee_ppq_params
    private val vm: VegaIndiaCoffeePpqViewModel by viewModel()
    private lateinit var binding: FragmentVegaIndiaCoffeePpqParamsBinding

    private var lotDetail = VegaIndiaCoffeePpqInspectionLotDetails()
    private var inspectionLot = VegaIndiaCoffeePpqInspectionLots()
    private var paramList = mutableListOf<VegaIndiaCoffeePpqInspectionLotParams>()
    private var isAccept: Boolean = false
    private var mAdapter = VegaIndiaCoffeePpqQualityParamsAdapter { enableProceedBtn(it) }
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var materialNo: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var plantId: String? = ""
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var isData: Boolean? = false
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    val ppqLotList = arrayListOf<VegaIndiaCoffeePpqInspectionLotParams>()

    companion object {
        fun newInstance(inspectionLot: VegaIndiaCoffeePpqInspectionLots) =
            VegaIndiaCoffeePpqQualityParamsFragment().putArgs {
                putParcelable(INSPECTION_LOT, inspectionLot)
            }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaIndiaCoffeePpqParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqindiacoffee/ui/ppq/VegaIndiaCoffeePpqQualityParamsFragment")
            .title("PPQ").with(tracker)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initExtra() {
        inspectionLot = arguments?.getParcelable(INSPECTION_LOT)!!
    }

    private fun initUI() {
        binding.tvLotNo.text = inspectionLot.inspectionLotNum
        binding.tvMaterial.text = inspectionLot.materialName
        binding.etBatchNo.setText(inspectionLot.chargeNum)
        plantId = inspectionLot.plantId

        binding.rvPpqParams.layoutManager = LinearLayoutManager(this.context)
        binding.rvPpqParams.adapter = mAdapter
        vm.postLotDetails.observe(viewLifecycleOwner, Observer {
            updateUIResponse(it)
        })
        materialNo =
            if (!inspectionLot.materialCode.length.equals(18)) "000000".plus(inspectionLot.materialCode) else inspectionLot.materialCode

        vm.custonLocation.observe(
            this,
            Observer { custonLocationList = it.filter { !it.storageLocationType.equals("P") }.toMutableList() })
        vm.getCustomLocations()

        lotbatchNo = inspectionLot.chargeNum
        lotmaterialNo = inspectionLot.materialCode

        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })
        vm.getPreSamplingQualitydata(lotbatchNo?.trim()!!, lotmaterialNo?.trim()!!)
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.btnParamsProceed.setOnClickListener {
            proceedToPost()
        }
        binding.btnReject.setOnClickListener {
            val params = paramList.filter { it.formula.isEmpty() }
            val isParamValue = params.any { it.MeanValue.isNullOrEmpty() }
            when (!isParamValue) {
                true -> showConfirmDialog(
                    inspectionLot.chargeNum,
                    getString(R.string.reject_quality),
                    REJECT
                )
                else -> {
                    params.forEach { item1 ->
                        paramList.forEach { item2 ->
                            if (item1.charDesc.equals(item2.charDesc)) {
                                if (item2.MeanValue.isNullOrEmpty()) item2.mandatory = 1 else item2.mandatory = 0
                            }
                        }

                    }
                    activity?.toast(getString(R.string.atleast_one_value))
                    binding.rvPpqParams.adapter?.notifyDataSetChanged()
                }
            }

        }
    }

    private fun updateUIResponse(response: Resource<GenericReqAndResp<VegaIndiaCoffeePpqPostResponse>>) {
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

                        }
                    } else {
                        value.addAll(it)
                    }

                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        "",
                        "",
                        ""
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
                            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }
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

    private fun moveToSuccess(data: VegaIndiaCoffeePpqPostResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isAccept) intent.putExtra(AppUtils.TITLE, getString(R.string.success_accept))
        else intent.putExtra(AppUtils.TITLE, getString(R.string.success_reject))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(": ").plus(inspectionLot.chargeNum))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaIndiaCoffeePpqInspectionLotDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            paramList.clear()
                            lotDetail = it.data!!.data
                            paramList.addAll(it.data!!.data.qualityParameters)
                            setUpAdapter(paramList)
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

    private fun setUpAdapter(items: MutableList<VegaIndiaCoffeePpqInspectionLotParams>) {
        binding.rvPpqParams.setUp(
            paramList,
            R.layout.item_vega_india_coffee_ppq_params,
            { it, pos ->
                val spannable: SpannableStringBuilder?
                spannable = SpannableStringBuilder(it.charDesc)
                spannable.insert(spannable.length, "")
                edQualityValue.setText(it.MeanValue)
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
                    charValue.addAll(it.qualitative.map { data -> data.codeTxt })
                    val charValueAdapter =
                        ArrayAdapter(spItem.context, android.R.layout.simple_list_item_1, charValue)
                    spItem.adapter = charValueAdapter
                    if (!it.MeanValue.isNullOrEmpty())
                        spItem.setSelection(charValue.indexOf(it.MeanValue))
                    spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            pos1: Int,
                            p3: Long
                        ) {
                            if (pos1 > 0) {
                                paramList[pos].MeanValue = charValue[pos1]
                                paramList[pos].code =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                                paramList[pos].Code1 =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                                paramList[pos].codeGroup =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].codeGroup
                                paramList[pos].CodeGrp1 =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].codeGroup
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
                        if (!it.formula.isNullOrEmpty())
                            llEdit.let {
                                ViewCompat.setBackground(
                                    it,
                                    ContextCompat.getDrawable(
                                        context,
                                        com.olam.warehouse.presentation.R.drawable.custom_edit_text
                                    )
                                )
                            }
                        else
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

                    if (!it.formula.isNullOrEmpty()) {
                        edQualityValue.isEnabled = false
                        tvQualityName.isEnabled = false
                        tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.grey_border))
                    } else {
                        edQualityValue.isEnabled = true
                        tvQualityName.isEnabled = true
                        tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.black))
                    }
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
                        if (!it.quantitative.lowerLimit.isNullOrEmpty()) it.quantitative.lowerLimit.trim()
                            .replace(
                                ",",
                                "."
                            ).toDouble() else 0.00
                    val upperLimit =
                        if (!it.quantitative.upperLimit.isNullOrEmpty()) it.quantitative.upperLimit.trim()
                            .replace(
                                ",",
                                "."
                            ).toDouble() else 0.00
                    edQualityValue.onChange { text ->
                        try {
                            if (text.isEmpty()) {
                                paramList[pos].MeanValue = ""
                                return@onChange
                            }
                            val editVal: String?
                            val dot = text[0].toString()
                            editVal = if (dot == ".") {
                                if (text.length == 1) "0.00" else "0".plus(text)
                            } else text

                            if (upperLimit > 0) {
                                if (validateRangeParamValue(editVal, lowerLimit, upperLimit)) {
                                    paramList[pos].MeanValue = editVal
                                } else {
                                    edQualityValue.setError("value out of range!", null)
                                }
                            } else {
                                paramList[pos].MeanValue = editVal
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }
                }
                tvQualityName.text = spannable

            },
            {

            })
    }

    private fun validateRangeParamValue(id: String?, noFrom: Double?, noTo: Double?): Boolean {
        return when {
            id?.isNotEmpty()!! -> {
                return when {
                    id.toDouble() < noFrom!! || id.toDouble() > noTo!! -> false
                    else -> true
                }
            }
            else -> true
        }
    }

    @SuppressLint("WrongConstant")
    private fun handleScroll(editText: EditText) {
        editText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val view = textView.focusSearch(View.FOCUS_FORWARD)
                if (view != null) {
                    if (!view.requestFocus(View.FOCUS_FORWARD)) {
                        return@OnEditorActionListener true
                    }
                }
                return@OnEditorActionListener false
            }
            false
        })
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
            ACCEPT -> lotDetail.usageDecision = true
            REJECT -> lotDetail.usageDecision = false
        }
        lotDetail.inspectionLotNum = inspectionLot.chargeNum
        lotDetail.materialCode = materialNo!!
        lotDetail.plantId = plantId!!
        qualityParameterList.forEach { item ->
            val dat = VegaIndiaCoffeePpqInspectionLotParams()
            dat.MeanValue = item?.qualityParameterValue!!
            dat.charDesc = item.descrChar!!
            dat.codeValue = item.nameChar
            ppqLotList.add(dat)
        }
        lotDetail.qualityParameters = ppqLotList

        vm.saveInspectionLotDetails(lotDetail)
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

    private fun proceedToPost() {
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
                if (getCurrentKey().split("_")[2].contains("CASH")) {
                    if (!itValue?.qualityParameter?.formulaParam.equals("X"))
                        qualityParameterList.add(itValue?.qualityParameter)
                } else {
                    qualityParameterList.add(itValue?.qualityParameter)
                }

            }
        }
        if (isValueNeed)
            showConfirmDialog(
                inspectionLot.chargeNum,
                getString(R.string.accept_quality),
                ACCEPT
            )
        else {
            showSnack(requireContext().resources.getString(R.string.atleast_one_value))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }
}
