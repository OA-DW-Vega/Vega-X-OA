package com.olam.warehouse.vegax.ppqsesame.ui

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
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.veganicaragua.entity.QualitativeParams
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.*
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ppqsesame.R
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLotParams
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqInspectionLots
import com.olam.warehouse.vegax.ppqsesame.data.domain.model.VegaSesamePpqPostResponse
import com.olam.warehouse.vegax.ppqsesame.databinding.FragmentVegaSesamePpqParamsBinding
import com.olam.warehouse.vegax.ppqsesame.databinding.ItemVegaSesamePpqParamsBinding
import com.olam.warehouse.vegax.ppqsesame.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 8/7/2020.
 */
class VegaSesamePpqQualityParamsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_ppq_params
    private val vm: VegaSesamePpqViewModel by viewModel()
    private lateinit var binding: FragmentVegaSesamePpqParamsBinding

    private var lotDetail = VegaSesamePpqInspectionLotDetails()
    private var inspectionLot = VegaSesamePpqInspectionLots()
    private var paramList = mutableListOf<VegaSesamePpqInspectionLotParams>()
    private var isAccept: Boolean = false
    private var mAdapter = VegaNigeriaSesamePpqQualityParamsAdapter { enableProceedBtn(it) }
    private var custonLocationList = mutableListOf<VegaCustomStLocation>()
    private var storageLocationList = mutableListOf<VegaStorageLocation>()
    private var materialNo: String? = ""
    private var lotbatchNo: String? = ""
    private var lotmaterialNo: String? = ""
    private var plantId: String? = ""
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private var isData: Boolean? = false
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    val ppqLotList = arrayListOf<VegaSesamePpqInspectionLotParams>()
    var isMillingPlant= false


    companion object {
        fun newInstance(inspectionLot: VegaSesamePpqInspectionLots) = VegaSesamePpqQualityParamsFragment().putArgs {
            putParcelable(INSPECTION_LOT, inspectionLot)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaSesamePpqParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ppqsesame/ui/ppq/VegaSesamePpqQualityParamsFragment")
            .title("PPQ").with(tracker)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    private fun initExtra() {
        inspectionLot = arguments?.getParcelable(INSPECTION_LOT)!!
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnParamsProceed, it, true)
        }
        binding.tvLotNo.text = inspectionLot.inspectionLotNum
        binding.tvMaterial.text = inspectionLot.materialName
        binding.etBatchNo.setText(inspectionLot.chargeNum)
        plantId = inspectionLot.plantId

        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })


        binding.rvPpqParams.layoutManager = LinearLayoutManager(this.context)
        binding.rvPpqParams.adapter = mAdapter
        vm.postLotDetails.observe(viewLifecycleOwner, Observer {
            updateUIResponse(it)
        })
        materialNo =
            if (!inspectionLot.materialCode.length.equals(18))
                "000000".plus(inspectionLot.materialCode)
            else inspectionLot.materialCode

        vm.custonLocation.observe(
            viewLifecycleOwner,
            Observer {
                custonLocationList =
                    it.filter { !it.storageLocationType.equals("P") }.toMutableList()
            })
        vm.getCustomLocations()

        if (getCurrentKey().split("_")[1].contains("NI")) {
            vm.storageLocation.observe(
                viewLifecycleOwner,
                Observer { storageLocationList = it.toMutableList() })
            vm.getStorageLocations()
            vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
                materialQualityGradeList = it.toMutableList()
            })
            vm.getMaterialQualityGrades(materialNo.toString())
        }

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

    private fun updateUIResponse(response: Resource<GenericReqAndResp<VegaSesamePpqPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            it.data?.data?.let { it1 -> moveToSuccess(it1) }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
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

                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                item.qualitative =
                                    prepareQualitativeNic(
                                        storageLocationList,
                                        item.qualityParameter.materialCode
                                    )
                            } else {
                                item.qualitative =
                                    prepareQualitative(
                                        custonLocationList,
                                        item.qualityParameter.materialCode
                                    )
                            }
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
                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                        item.qualityParameter.qualityParameterValue =
                                            item1.satNam!!
                                    } else
                                        item.qualityParameter.qualityParameterValue =
                                            item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }

                        }
                    } else {
                        value.addAll(it)
                    }

                    if (getCurrentKey().split("_")[1].contains("NI"))

                        mAdapter.addItems(
                            sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                            "",
                            "",
                            inspectionLot.materialName
                        )
                    else
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

    private fun moveToSuccess(data: VegaSesamePpqPostResponse) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isAccept) intent.putExtra(AppUtils.TITLE, getString(R.string.success_accept))
        else intent.putExtra(AppUtils.TITLE, getString(R.string.success_reject))
        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString(R.string.batch_number).plus(": ").plus(inspectionLot.chargeNum)
        )
        startActivity(intent)
        requireActivity().finish()
    }

    private fun updateUI(response: Resource<GenericReqAndResp<VegaSesamePpqInspectionLotDetails>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            paramList.clear()
                            lotDetail = it.data!!.data
                            paramList.addAll(it.data!!.data.qualityParameters)
                            setUpAdapter()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun setUpAdapter() {
        binding.rvPpqParams.setUpAdapter(
            paramList, R.layout.item_vega_sesame_ppq_params,
            ItemVegaSesamePpqParamsBinding::inflate,
            { it, pos, bindItem ->
                val spannable: SpannableStringBuilder?
                spannable = SpannableStringBuilder(it.charDesc)
                spannable.insert(spannable.length, "")
                bindItem.edQualityValue.setText(it.MeanValue)
                bindItem.tvUnit.text = it.meansUnit
                if (it.qualitative.isNotEmpty()) {
                    bindItem.llDropDown.visible()
                    bindItem.llEdit.gone()
                    if (it.mandatory?.equals(1)!!) bindItem.llDropDown.let {
                        ViewCompat.setBackground(
                            it,
                            ContextCompat.getDrawable(
                                context,
                                com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                            )
                        )
                    }
                    else bindItem.llDropDown.let {
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
                        ArrayAdapter(
                            bindItem.spItem.context,
                            android.R.layout.simple_list_item_1,
                            charValue
                        )
                    bindItem.spItem.adapter = charValueAdapter
                    if (!it.MeanValue.isNullOrEmpty())
                        bindItem.spItem.setSelection(charValue.indexOf(it.MeanValue))
                    bindItem.spItem.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {/* Nothing to Implement*/

                            }

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
                    handleScroll(bindItem.edQualityValue)
                    bindItem.llDropDown.gone()
                    bindItem.llEdit.visible()

                    if (it.mandatory?.equals(1)!!) bindItem.llEdit.let {
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
                            bindItem.llEdit.let {
                                ViewCompat.setBackground(
                                    it,
                                    ContextCompat.getDrawable(
                                        context,
                                        com.olam.warehouse.presentation.R.drawable.custom_edit_text
                                    )
                                )
                            }
                        else
                            bindItem.llEdit.let {
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
                        bindItem.edQualityValue.isEnabled = true
                        bindItem.tvQualityName.isEnabled = true
                        bindItem.tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.grey_border))
                    } else {
                        bindItem.edQualityValue.isEnabled = true
                        bindItem.tvQualityName.isEnabled = true
                        bindItem.tvQualityName.setTextColor(getColor(com.olam.warehouse.presentation.R.color.black))
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
                    bindItem.edQualityValue.setText(it.MeanValue, TextView.BufferType.EDITABLE)
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
                    bindItem.edQualityValue.onChange { text ->
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
                                    bindItem.edQualityValue.setError("value out of range!", null)
                                }
                            } else {
                                paramList[pos].MeanValue = editVal
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }
                }
                bindItem.tvQualityName.text = spannable

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
                if (view != null && !view.requestFocus(View.FOCUS_FORWARD)) {
                    return@OnEditorActionListener true
                }
                return@OnEditorActionListener false
            }
            false
        })
    }

    private fun showConfirmDialog(msg: String, postType: String) {
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
            val dat = VegaSesamePpqInspectionLotParams()
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
            binding.btnParamsProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }


    private fun proceedToPost() {
        var isValueNeed = true
        var total = 0.0
        val invalidDataPos = mutableListOf<Int>()
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
                    if (getCurrentKey().split("_")[1].contains("NG")) {
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
                        if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                                "X"
                            )) || (!itValue?.qualityParameter?.preSampling.equals(""))
                        ) {
                            if ((itValue?.qualityParameter?.vegaValueMandatory.equals("X"))) {
                                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                                    if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                                        isValueNeed = false
                                        missedPos.add(index)
                                        itValue?.qualityParameter?.mandatory = 0

                                    } else {
                                        isValueNeed = true
                                        missedPos.add(index)
                                        itValue?.qualityParameter?.mandatory = 1
                                    }
                                }
                            } else {
                                itValue?.qualityParameter?.mandatory = 0
                            }
                            if (getCurrentKey().split("_")[2].contains("CASH")
//                                || getCurrentKey().split("_")[1].contains("NI")
                            ) {
                                qualityParameterList.add(itValue?.qualityParameter)
                            }
                            //qualityParameterList.add(grnQtyQualityParam)
                        }
                    }

                } else {
                    itValue?.qualityParameter?.mandatory = 0
                    if (getCurrentKey().split("_")[1].contains("NI")) {
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
                            total += itValue?.qualityParameter?.qualityParameterValue?.trim()?.toDouble()!!
                        }
                    }

                }
                qualityParameterList.add(itValue?.qualityParameter)
            }
        }
        if (isValueNeed)
            if (getCurrentKey().split("_")[1].contains("NI")) {
                var sum = total.toInt()
                if (sum != 100) {
                    mAdapter.getItems().forEachIndexed { index, itValue ->
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
                    showSnack(requireContext().resources.getString(R.string.ente_fields_equal))
                    mAdapter.updateMissedPos(invalidDataPos, data)
                }

                mAdapter.getItems().forEachIndexed { index, itValue ->
                    if (itValue?.qualityParameter?.nameChar.equals("NIFG0014")
                        && inspectionLot.materialName.contains("Certificado")
                    ) {
                        if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                            itValue?.qualityParameter?.mandatory = 1
                            invalidDataPos.add(index)
                            showSnack(requireContext().resources.getString(R.string.ente_fields_ROB))
                            mAdapter.updateMissedPos(invalidDataPos, data)

                        } else
                            if (itValue?.qualityParameter?.qualityParameterValue!!.isNotEmpty()) {
                                if (!validateParamValue
                                        (
                                        itValue.qualityParameter?.qualityParameterValue?.replace("%", "")?.trim()
                                            .toString(),
                                        itValue.qualityParameter?.nameChar.toString(),
                                        itValue.qualityParameter?.numValFm,
                                        itValue.qualityParameter?.numValTo
                                    )
                                ) {
                                    itValue.qualityParameter?.mandatory = 1
                                    invalidDataPos.add(index)
                                    showSnack("Value out of range !!")
                                    mAdapter.updateMissedPos(invalidDataPos, data)

                                }


                            }

                    } else if (!itValue?.qualityParameter?.nameChar.equals("NIFG0014")) {
                        if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                            itValue?.qualityParameter?.mandatory = 1
                            invalidDataPos.add(index)
                            showSnack(requireContext().resources.getString(R.string.ente_fields_ROB))
                            mAdapter.updateMissedPos(invalidDataPos, data)

                        } else
                            if (itValue?.qualityParameter?.qualityParameterValue!!.isNotEmpty()) {
                                if (!validateParamValue
                                        (
                                        itValue.qualityParameter?.qualityParameterValue?.replace("%", "")?.trim()
                                            .toString(),
                                        itValue.qualityParameter?.nameChar.toString(),
                                        itValue.qualityParameter?.numValFm,
                                        itValue.qualityParameter?.numValTo

                                    )
                                ) {
                                    itValue.qualityParameter?.mandatory = 1
                                    invalidDataPos.add(index)
                                    showSnack("Value out of range !!")
                                    mAdapter.updateMissedPos(invalidDataPos, data)

                                }


                            }

                    }
                }

                if (invalidDataPos.size == 0)
                    showConfirmDialog(
                        getString(R.string.accept_quality),
                        ACCEPT
                    )

            } else
                showConfirmDialog(
                    getString(R.string.accept_quality),
                    ACCEPT
                )
        else {
            showSnack(requireContext().resources.getString(R.string.atleast_one_value))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun validateParamValue(id: String?, namechar: String, numValFm: String?, numValTo: String?): Boolean {
      println("id***:"+id+":" +namechar+":"+numValFm+":"+numValTo )
        if (!isdouble(id)) {
            return !id.equals("Select")

        } else if (namechar == "NIFGICO")
            return true
        else return !(id?.toDouble()!! < numValFm!!.toDouble() || id.toDouble()!! > numValTo!!.toDouble())
    }

    fun isdouble(s: String?): Boolean {
        try {
            s?.toDouble()
        } catch (e: java.lang.NumberFormatException) {
            return false
        } catch (e: NullPointerException) {
            return false
        }
        // only got here if we didn't return false
        return true
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {

        var millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }

        if (!millingPlants.isNullOrEmpty()) {
            isMillingPlant = true
        }

    }


}




