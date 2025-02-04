package com.olam.warehouse.vegax.ppqcoffee.ui

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
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqcoffee.R
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLotDetails
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLotParams
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqInspectionLots
import com.olam.warehouse.vegax.ppqcoffee.data.domain.model.VegaCoffeePpqPostResponse
import com.olam.warehouse.vegax.ppqcoffee.databinding.FragmentVegaCoffePpqParamsBinding
import com.olam.warehouse.vegax.ppqcoffee.utils.ACCEPT
import com.olam.warehouse.vegax.ppqcoffee.utils.INSPECTION_LOT
import com.olam.warehouse.vegax.ppqcoffee.utils.REJECT
import com.olam.warehouse.vegax.ppqcoffee.utils.getColor
import kotlinx.android.synthetic.main.item_vega_coffee_ppq_params.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaCoffeePpqQualityParamsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_coffe_ppq_params
    private val vm: VegaCoffeePpqViewModel by viewModel()
    private lateinit var binding: FragmentVegaCoffePpqParamsBinding

    //    private var callBack: CallBack? = null
    private var lotDetail = VegaCoffeePpqInspectionLotDetails()
    private var inspectionLot = VegaCoffeePpqInspectionLots()
    private var paramList = mutableListOf<VegaCoffeePpqInspectionLotParams>()
    private var isAccept: Boolean = false

    /*interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaCoffeePpqInspectionLots
        )
    }*/

    companion object {
        fun newInstance(inspectionLot: VegaCoffeePpqInspectionLots) = VegaCoffeePpqQualityParamsFragment().putArgs {
            putParcelable(INSPECTION_LOT, inspectionLot)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCoffePpqParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqcoffee/ui/ppq/VegaCoffeePpqQualityParamsFragment")
            .title("PPQ").with(tracker)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initExtra() {
        inspectionLot = arguments?.getParcelable<VegaCoffeePpqInspectionLots>(INSPECTION_LOT)!!
    }

    private fun initUI() {
        binding.tvLotNo.text = inspectionLot.inspectionLotNum
        binding.tvMaterial.text = inspectionLot.materialName
        binding.etBatchNo.setText(inspectionLot.chargeNum)
        vm.postLotDetails.observe(viewLifecycleOwner, Observer { updateUIResponse(it) })
        vm.lotDetails.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getInspectionLotDetails(inspectionLot.inspectionLotNum)
        binding.btnAccept.setOnClickListener {
            val params = paramList.filter { it.formula.isEmpty() }
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

    private fun updateUIResponse(response: Resource<GenericReqAndResp<VegaCoffeePpqPostResponse>>) {
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

    private fun moveToSuccess(data: VegaCoffeePpqPostResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isAccept) intent.putExtra(AppUtils.TITLE, getString(R.string.success_accept))
        else intent.putExtra(AppUtils.TITLE, getString(R.string.success_reject))
        intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(": ").plus(inspectionLot.chargeNum))
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaCoffeePpqInspectionLotDetails>>) {
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
//                    setUpAdapter(paramList)
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun setUpAdapter(items: MutableList<VegaCoffeePpqInspectionLotParams>) {
        binding.rvPpqParams.setUp(paramList, R.layout.item_vega_coffee_ppq_params, { it, pos ->

            //tvQualityName.text = it.charDesc
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

        }, {

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
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
        lotDetail.qualityParameters = paramList
        vm.saveInspectionLotDetails(lotDetail)
    }
}
