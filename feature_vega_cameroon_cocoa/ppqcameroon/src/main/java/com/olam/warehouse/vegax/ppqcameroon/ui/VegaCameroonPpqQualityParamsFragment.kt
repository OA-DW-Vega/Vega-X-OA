package com.olam.warehouse.vegax.ppqcameroon.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqcameroon.BuildConfig
import com.olam.warehouse.vegax.ppqcameroon.R
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLotParams
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqInspectionLots
import com.olam.warehouse.vegax.ppqcameroon.data.domain.model.VegaCameroonPpqPostResponse
import com.olam.warehouse.vegax.ppqcameroon.databinding.FragmentVegaCameroonPpqParamsBinding
import com.olam.warehouse.vegax.ppqcameroon.utils.ACCEPT
import com.olam.warehouse.vegax.ppqcameroon.utils.INSPECTION_LOT
import com.olam.warehouse.vegax.ppqcameroon.utils.REJECT
import com.olam.warehouse.vegax.ppqcameroon.utils.getColor
import kotlinx.android.synthetic.main.item_vega_cameroon_ppq_params.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCameroonPpqQualityParamsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_ppq_params
    private val vm: VegaCameroonPpqViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonPpqParamsBinding

    private var lotDetail = VegaCameroonPpqInspectionLotDetails()
    private var inspectionLot = VegaCameroonPpqInspectionLots()
    private var paramList = mutableListOf<VegaCameroonPpqInspectionLotParams>()
    private var nformulaParamList = mutableListOf<VegaCameroonPpqInspectionLotParams>()
    private var formulaParamList = mutableListOf<VegaCameroonPpqInspectionLotParams>()
    private var isAccept: Boolean = false
    private var mapInputParameters: MutableMap<String?, String?> = mutableMapOf()

    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item1: String,
            item: VegaCameroonPpqInspectionLotDetails
        )
    }

    companion object {
        fun newInstance(inspectionLot: VegaCameroonPpqInspectionLots) = VegaCameroonPpqQualityParamsFragment().putArgs {
            putParcelable(INSPECTION_LOT, inspectionLot)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonPpqParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqcameroon/ui/VegaCameroonPpqQualityParamsFragment")
            .title("Vega_Cameroon/PPQ")
            .with(tracker)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initExtra() {
        inspectionLot = arguments?.getParcelable<VegaCameroonPpqInspectionLots>(INSPECTION_LOT)!!
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnAccept, it, true)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
        }
        binding.tvLotNo.text = inspectionLot.inspectionLotNum
        binding.tvMaterial.text = inspectionLot.materialName
        binding.etBatchNo.setText(inspectionLot.chargeNum)

        vm.postLotDetails.observe(viewLifecycleOwner, Observer { updateUIResponse(it) })
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getInspectionLotDetails(inspectionLot.inspectionLotNum)
        binding.btnParamsProceed.setOnClickListener {
            lotDetail.qualityParameters = nformulaParamList
            val params = nformulaParamList.filter { it.formula.isEmpty() }
            val isParamValue = params.any { it.MeanValue.isNullOrEmpty() }
            when (!isParamValue) {
                true ->
                    callBack?.replaceQualityFragment(inspectionLot.inspectionLotNum, inspectionLot.chargeNum, lotDetail)


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
        binding.btnAccept.setOnClickListener {
            val params = nformulaParamList.filter { it.formula.isEmpty() }
            val isParamValue = params.any { it.MeanValue.isNullOrEmpty() }
            when (!isParamValue) {
                true -> showConfirmDialog(
                    inspectionLot.chargeNum,
                    getString(R.string.accept_quality),
                    ACCEPT
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
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(": ").plus(inspectionLot.chargeNum))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCameroonPpqInspectionLotDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            paramList.clear()
                            lotDetail = it.data!!.data
                            paramList.addAll(it.data!!.data.qualityParameters)
                            paramList.forEach {
                                mapInputParameters.put("C".plus(it.Inspchar), "0")
                            }
                            setUpAdapter(paramList)
                            setUpFormulaAdapter(paramList)
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

    private fun setUpFormulaAdapter(items: MutableList<VegaCameroonPpqInspectionLotParams>) {
        paramList.forEach {
            if(!it.formula.isNullOrEmpty())
                formulaParamList.add(it)
        }

        binding.rvPpqParamsFormula.setUp(formulaParamList, R.layout.item_vega_cameroon_ppq_params, { it, pos ->

            val spannable: SpannableStringBuilder?
            spannable = SpannableStringBuilder(it.charDesc)
            spannable.insert(spannable.length, "")
            edQualityValue.setText(it.MeanValue)
            tvUnit.text = it.meansUnit
            llDropDown.gone()
            llEdit.visible()
            llEdit.let {
                ViewCompat.setBackground(
                    it,
                    ContextCompat.getDrawable(
                        context,
                        com.olam.warehouse.presentation.R.drawable.custom_edit_text
                    )
                )
            }
            edQualityValue.isEnabled = false
            tvQualityName.isEnabled = false
            var result = evaluateFormula(it.formula,mapInputParameters)
            edQualityValue.setText(result.toString())
            mapInputParameters.put("C".plus(it.Inspchar), result.toString())
            tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.grey_border))
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
            tvQualityName.text = spannable

        }, {

        })
    }

    private fun setUpAdapter(items: MutableList<VegaCameroonPpqInspectionLotParams>) {
        paramList.forEach {
            if(it.formula.isNullOrEmpty())
                nformulaParamList.add(it)
        }
        binding.rvPpqParams.setUp(
            nformulaParamList,
            R.layout.item_vega_cameroon_ppq_params,
            { it, pos ->

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


                    when (it.charDesc) {
                        "CLASSEMENT" -> {
                            charValue.addAll(it.qualitative.filter { it.codeGroup == "CLASCOCO" }
                                .map { data -> data.codeTxt })
                        }
                        "Smoky / Non Smoky" -> {
                            charValue.addAll(it.qualitative.filter { it.codeGroup == "IR-AB/PR" }
                                .map { data -> data.codeTxt })
                        }
                    }

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
                                nformulaParamList[pos].MeanValue = charValue[pos1]
                                nformulaParamList[pos].code =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                                nformulaParamList[pos].Code1 =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].code
                                nformulaParamList[pos].codeGroup =
                                    it.qualitative.filter { it.codeTxt.equals(charValue[pos1]) }[0].codeGroup
                                nformulaParamList[pos].CodeGrp1 =
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
                                    mapInputParameters.put(
                                        "C".plus(nformulaParamList[pos].Inspchar),
                                        nformulaParamList[pos].MeanValue
                                    )
                                } else {
                                    edQualityValue.setError("value out of range!", null)
                                    mapInputParameters.put(
                                        "C".plus(nformulaParamList[pos].Inspchar),
                                        "0"
                                    )
                                }
                            } else {
                                nformulaParamList[pos].MeanValue = editVal
                                mapInputParameters.put(
                                    "C".plus(nformulaParamList[pos].Inspchar),
                                    nformulaParamList[pos].MeanValue
                                )
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
        lotDetail.qualityParameters = nformulaParamList
        vm.saveInspectionLotDetails(lotDetail)
    }

    private fun evaluateFormula(strExpression: String?, mapInputParameters: Map<String?, String?>?): Double {
        var strExpression: String? = strExpression ?: return 0.0
        strExpression = strExpression?.trim { it <= ' ' }

        if ("" == strExpression) {
            return 0.0
        }

        if (mapInputParameters != null && mapInputParameters.size > 0) {
            for ((key, value) in mapInputParameters) {
                strExpression = strExpression?.replace(key!!, value!!, false)
            }
        }
        val strFinalExpression = strExpression!!.trim { it <= ' ' }
        return object : Any() {
            var expPos = -1
            var nextChar = 0

            private fun readNextChar() {
                nextChar =
                    if (++expPos < strFinalExpression.length) strFinalExpression[expPos].toInt() else -1
            }
            private fun charToCheck(charToCheck: Int): Boolean {
                while (nextChar == ' '.toInt()) {
                    readNextChar()
                }
                if (nextChar == charToCheck) {
                    readNextChar()
                    return true
                }
                return false
            }

            fun evaluate(): Double {
                readNextChar()
                val retValue = parseExpression()
                if (expPos < strFinalExpression.length) {
                    if (BuildConfig.DEBUG) {
                        RuntimeException("Unexpected: " + nextChar.toChar())
                            .printStackTrace()
                    }
                }
                return retValue
            }

            private fun parseExpression(): Double {
                var retValue: Double = parseValue()
                while (true) {
                    if (charToCheck('+'.toInt())) retValue += parseValue() // addition
                    else if (charToCheck('-'.toInt())) retValue -= parseValue() // subtraction
                    else return retValue
                }
            }

            private fun parseValue(): Double {
                var retValue: Double = getValue()
                while (true) {
                    if (charToCheck('*'.toInt())) retValue *= getValue() // multiplication
                    else if (charToCheck('/'.toInt())) retValue /= getValue() // division
                    else if (charToCheck('%'.toInt())) retValue %= getValue() // modulus
                    else return retValue
                }
            }

            private fun getValue(): Double {
                if (charToCheck('+'.toInt())) return getValue() // unary plus
                if (charToCheck('-'.toInt())) return -getValue() // unary minus
                var retValue = 0.0
                val startPos = expPos
                if (charToCheck('('.toInt())) { // parentheses
                    retValue = parseExpression()
                    charToCheck(')'.toInt())
                } else if (nextChar >= '0'.toInt() && nextChar <= '9'.toInt() || nextChar == '.'.toInt()) { // numbers
                    while (nextChar >= '0'.toInt() && nextChar <= '9'.toInt() || nextChar == '.'.toInt()) {
                        readNextChar()
                    }
                    retValue = strFinalExpression.substring(startPos, expPos).toDouble()
                } else if (nextChar >= 'a'.toInt() && nextChar <= 'z'.toInt()) { // functions
                    while (nextChar >= 'a'.toInt() && nextChar <= 'z'.toInt()) {
                        readNextChar()
                    }
                    val func = strFinalExpression.substring(startPos, expPos)
                    retValue = getValue()
                    retValue = when (func) {
                        "sqrt" -> Math.sqrt(retValue)
                        "sin" -> Math.sin(Math.toRadians(retValue))
                        "cos" -> Math.cos(Math.toRadians(retValue))
                        "tan" -> Math.tan(Math.toRadians(retValue))
                        "abs" -> Math.abs(retValue)
                        else -> getValue()
                    }
                } else if (nextChar >= 'A'.toInt() && nextChar <= 'Z'.toInt()) { // Conditions
                    while (nextChar >= 'A'.toInt() && nextChar <= 'Z'.toInt()) {
                        readNextChar()
                    }
                    val condition = strFinalExpression.substring(startPos, expPos)
                    when (condition) {
                        "IF" -> retValue = evaluateIFCondition()
                        "OR" -> {
                            val bRetValueOr: Boolean? = evaluateOrCondition()
                            retValue = if (bRetValueOr!!) {
                                -0.000000001
                            } else {
                                -0.000000002
                            }
                        }
                        "AND" -> {
                            val bRetValue: Boolean? = evaluateAndCondition()
                            retValue = if (bRetValue!!) {
                                -0.000000001
                            } else {
                                -0.000000002
                            }
                        }
                        else -> if (BuildConfig.DEBUG) {
                            java.lang.RuntimeException("Unexpected: " + nextChar.toChar()).printStackTrace()
                        }
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        java.lang.RuntimeException("Unexpected: " + nextChar.toChar()).printStackTrace()
                    }
                }
                if (charToCheck('^'.toInt())) retValue = Math.pow(retValue, getValue()) // exponentiation
                return retValue
            }

            private fun evaluateIFCondition(): Double {
                val leftValue = parseExpression()
                val operator: String? = getOperator()
                var rightValue = 0.0
                if ("" != operator) {
                    rightValue = parseExpression()
                }
                readNextChar()
                val ifValue = parseExpression()
                readNextChar()
                val elseValue = parseExpression()
                readNextChar()
                val retValue: Boolean? = evaluateCondition(operator!!, leftValue.toString(), rightValue.toString())
                return if (retValue!!) {
                    ifValue
                } else {
                    elseValue
                }
            }

            private fun evaluateOrCondition(): Boolean? {
                var retValue1: Boolean? = null
                while (nextChar != ')'.toInt()) {
                    val retValue: Boolean? = evaluateAndOrCondition()
                    retValue1 = if (retValue1 == null) {
                        retValue
                    } else {
                        retValue1 || retValue!!
                    }
                    while (nextChar == ' '.toInt()) {
                        readNextChar()
                    }
                }
                return retValue1
            }

            private fun evaluateAndCondition(): Boolean? {
                var retValue1: Boolean? = null
                if (charToCheck('('.toInt())) {
                    while (nextChar != ')'.toInt()) {
                        val retValue: Boolean? = evaluateAndOrCondition()
                        retValue1 = if (retValue1 == null) {
                            retValue
                        } else {
                            retValue1 as Boolean && retValue!!
                        }
                        while (nextChar == ' '.toInt()) {
                            readNextChar()
                        }
                    }
                    charToCheck(')'.toInt())
                }
                return retValue1
            }

            private fun evaluateAndOrCondition(): Boolean? {
                var retValue: Boolean? = null
                while (nextChar == ' '.toInt() || nextChar == ','.toInt()) {
                    readNextChar()
                }
                var strLeftValue: String? = null
                var dLeftValue: Double? = null
                var strRightValue: String? = null
                var dRightValue: Double? = null
                if (nextChar == '"'.toInt()) {
                    strLeftValue = getStringValue()
                } else {
                    dLeftValue = parseExpression()
                }
                while (nextChar == ' '.toInt()) {
                    readNextChar()
                }
                val operator: String? = getOperator()
                while (nextChar == ' '.toInt()) {
                    readNextChar()
                }
                if (nextChar == '"'.toInt()) {
                    strRightValue = getStringValue()
                } else {
                    dRightValue = parseExpression()
                }
                if (strLeftValue != null) {
                    if (strRightValue == null) {
                        strRightValue = dRightValue.toString()
                    }
                    retValue = evaluateCondition(operator!!, strLeftValue, strRightValue)
                } else if (dLeftValue != null) {
                    if (dRightValue == null) {
                        dRightValue = java.lang.Double.valueOf(strRightValue!!)
                    }
                    retValue = evaluateCondition(operator!!, dLeftValue.toString(), dRightValue.toString())
                } else {
                    if (BuildConfig.DEBUG) {
                        java.lang.RuntimeException("Unexpected: " + nextChar.toChar()).printStackTrace()
                    }
                }
                return retValue
            }

            private fun evaluateCondition(
                operator: String,
                leftValue: String,
                rightValue: String
            ): Boolean? {
                var retValue = false
                when (operator) {
                    "" -> if (leftValue == "true") {
                        retValue = true
                    }
                    "=" -> if (leftValue == rightValue) {
                        retValue = true
                    }
                    "!=" -> if (leftValue != rightValue) {
                        retValue = true
                    }
                }
                return retValue
            }

            private fun getOperator(): String? {
                var condOper = ""
                while (nextChar == ' '.toInt()) {
                    readNextChar()
                }
                val startPos = expPos
                if (nextChar == '<'.toInt() || nextChar == '>'.toInt() || nextChar == '='.toInt() || nextChar == '!'.toInt()) { // conditional operators
                    while (nextChar == '<'.toInt() || nextChar == '>'.toInt() || nextChar == '='.toInt() || nextChar == '!'.toInt()) {
                        readNextChar()
                    }
                    condOper = strFinalExpression.substring(startPos, expPos)
                }
                return condOper
            }

            private fun getStringValue(): String? {
                readNextChar()
                val retValue: String
                val startPos = expPos
                while (nextChar != '"'.toInt()) {
                    readNextChar()
                }
                retValue = strFinalExpression.substring(startPos, expPos)
                readNextChar()
                return retValue
            }


        }.evaluate()

    }
}


