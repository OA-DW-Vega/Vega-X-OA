package com.olam.warehouse.vegax.dummyquality.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.utils.HandlerUtils
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.data.domain.model.QualityParameter
import com.olam.warehouse.vegax.dummyquality.data.domain.model.QualityParametersValueList
import com.olam.warehouse.vegax.dummyquality.data.domain.model.VegaCommonDummySampleModel
import com.olam.warehouse.vegax.dummyquality.data.domain.model.prepareVegaQualityParams
import com.olam.warehouse.vegax.dummyquality.databinding.FragmentDummyQualityCaptureAcceptBinding
import com.olam.warehouse.vegax.dummyquality.databinding.ItemVegaCommonDummyQualityParamBinding
import com.olam.warehouse.vegax.dummyquality.ui.printformates.generateQualitySheet
import com.olam.warehouse.vegax.dummyquality.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

class VegaCommonDummyQualityAcceptFragment : BaseFragment(), VegaSingleSelectCommonListener {

    override val layoutResourceId = R.layout.fragment_dummy_quality_capture_accept
    private lateinit var binding: FragmentDummyQualityCaptureAcceptBinding
    private lateinit var materialName: String
    private lateinit var materialCode: String
    private lateinit var supplierName: String
    private lateinit var supplierCode: String
    private var transactionNumber: String? = null
    private lateinit var id: String
    private val viewModel: VegaCommonDummyQualityViewModel by viewModel()
    private lateinit var qualityParamList: MutableList<VegaQualityParamsWithQualitative>
    private lateinit var qualityParamListValue: MutableList<VegaQualityParamsWithQualitative>
    private var customDialog1: VegaCommonSingleSelectDialogWithSearch? = null
    var model = VegaCommonDummySampleModel()
    var qualityParameterListValue = ArrayList<QualityParametersValueList>()
    private var callBack: CallBack? = null
    private var qualityList: ArrayList<QualityParameter>? = null

    companion object {
        fun newInstance(
            supplierCode: String,
            supplierName: String,
            materialName: String,
            materialCode: String,
            id: String
        ) =
            VegaCommonDummyQualityAcceptFragment().putArgs {
                putString(SUPPLIER_CODE, supplierCode)
                putString(SUPPLIER_NAME, supplierName)
                putString(MATERIAL_NAME, materialName)
                putString(MATERIAL_CODE, materialCode)
                putString(MATERIAL_ID, id)
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDummyQualityCaptureAcceptBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        arguments?.let {
            binding.tvSupplierName.text = "Supplier : " + requireArguments().getString(SUPPLIER_NAME)
            binding.tvMaterialName.text = "Material : " + requireArguments().getString(MATERIAL_NAME)
            transactionNumber = "TMP".plus(Random.nextLong().toString())
            supplierName = requireArguments().getString(SUPPLIER_NAME).toString()
            supplierCode = requireArguments().getString(SUPPLIER_CODE).toString()
            materialName = requireArguments().getString(MATERIAL_NAME).toString()
            materialCode = requireArguments().getString(MATERIAL_CODE).toString()
            id = requireArguments().getString(MATERIAL_ID).toString()
            if (IS_VIEW_QUALITY) {
                // here supplier code consider as id
                viewModel.getDummySampleDetails(getCurrentKey(), id)
                viewModel.dummySampleDetails.observe(viewLifecycleOwner) {
                    it.data?.let {
                        qualityList = it.data.qualityParameters as ArrayList<QualityParameter>
                        viewModel.getQualityParams("000000" + materialCode)
                    }
                }

                viewModel.qualitylist.observe(viewLifecycleOwner) {
                    qualityParamList = it.toMutableList()

                    qualityList?.forEachIndexed { index, element ->
                        var model = qualityParamList[index]
                        model.qualityParameter.qualityParameterValue = element.qualityParameterValue
                        qualityParamList.set(index, model)
                    }
                    setUpAdapter(qualityParamList)
                }
                binding.btnSave.visibility = View.GONE
                binding.btnPrint.visibility = View.VISIBLE
            } else {
                binding.btnSave.visibility = View.VISIBLE
                binding.btnPrint.visibility = View.VISIBLE
                viewModel.getQualityParams("000000" + materialCode)
                viewModel.qualitylist.observe(viewLifecycleOwner) {
                    qualityParamList = it.toMutableList()
                    if(qualityParamList.isNullOrEmpty()){
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.btnSave.isEnabled = false
                        binding.btnPrint.isEnabled = false
                        binding.btnSave.setBackgroundColor( ContextCompat.getColor(binding.btnSave.getContext(),R.color.grey_border))
                        binding.btnPrint.setBackgroundColor( ContextCompat.getColor(binding.btnPrint.getContext(),R.color.grey_border))
                    }else {
                        binding.tvNoData.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        binding.btnPrint.isEnabled = true
                        binding.btnSave.setBackgroundColor( ContextCompat.getColor(binding.btnSave.getContext(),R.color.card_pink_bg))
                        binding.btnPrint.setBackgroundColor( ContextCompat.getColor(binding.btnPrint.getContext(),R.color.card_pink_bg))
                        setUpAdapter(qualityParamList)
                    }
                }
            }
        }
        binding.btnSave.setOnClickListener {
            var isValueNeed  = true
            val missedPos = mutableListOf<Int>()
            qualityParamList.forEachIndexed { index, itValue ->

                if(itValue.qualityParameter.vegaMandatory.equals("X"))
                {
                    when {
                        itValue.qualityParameter.qualityParameterValue.isNullOrEmpty() -> {
                            when {
                                itValue.qualityParameter.formulaParam.equals("X") -> {
                                    itValue.qualityParameter.mandatory = 0
                                }
                                else -> {
                                    isValueNeed = false
                                    missedPos.add(index)
                                    itValue.qualityParameter.mandatory = 1
                                }
                            }

                        }
                        else -> {
                            itValue.qualityParameter.mandatory = 0
                        }
                    }
                }
            }
            when {
                isValueNeed -> {
                    showConfirmDialog()
                } else -> {
                showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
                binding.rvPpqParams.adapter?.notifyDataSetChanged()
            }
            }
        }
        binding.btnPrint.setOnClickListener {

            var isValueNeed  = true
            val missedPos = mutableListOf<Int>()
            qualityParamList.forEachIndexed { index, itValue ->

                if(itValue.qualityParameter.vegaMandatory.equals("X"))
                {
                    when {
                        itValue.qualityParameter.qualityParameterValue.isNullOrEmpty() -> {
                            when {
                                itValue.qualityParameter.formulaParam.equals("X") -> {
                                    itValue.qualityParameter.mandatory = 0
                                }
                                else -> {
                                    isValueNeed = false
                                    missedPos.add(index)
                                    itValue.qualityParameter.mandatory = 1
                                }
                            }

                        }
                        else -> {
                            itValue.qualityParameter.mandatory = 0
                        }
                    }
                }
            }
            when {
                isValueNeed -> {
                    var tallyPrintKeys = generateQualitySheet(
                        supplierName,
                        materialName,
                        transactionNumber?:"",
                        VegaQualityWBDetails(),
                        prepareVegaQualityParams(qualityParamList),
                        requireActivity()
                    )
                    showConfirmPrintDialog(tallyPrintKeys)
                } else -> {
                showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
                binding.rvPpqParams.adapter?.notifyDataSetChanged()
            }
            }
        }
    }
    private fun showConfirmDialog() {
        validateFields()
    }



    private fun showConfirmPrintDialog(tallyPrintKeys: ArrayList<String>) {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_print)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    //generateBitMapKey()
                    DoAsync {
                        if (tallyPrintKeys.size > 0)
                            HandlerUtils.runOnUiThread {
                                showPreviewDialog(tallyPrintKeys)
                            }
                    }.execute()
                },
                { dismiss() })
        }
    }

    private fun showPreviewDialog(tallyPrintKeys: ArrayList<String>) {
        DoAsync {
            HandlerUtils.runOnUiThread {
                hideLoading()
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(requireContext(), WifiMainActivity::class.java))
            }
        }.execute()

    }

    private fun validateFields() {

        model.key = getCurrentKey()
        model.supplierName = supplierName
        model.supplierCode = supplierCode
        model.materialName = materialName
        model.materialCode = materialCode
        model.isDummy = true
        model.isSap = false

        model.transactionNumber = transactionNumber.toString()
        model.date = DateUtils.getDate(DateUtils.getCurrentTimeInMills(), "yyyy-MM-dd")
        model.werks = getPlantDetails().plantId
        model.qualityDetails = prepareVegaQualityParams(qualityParamList)
        viewModel.postDummySample(model)
        viewModel.dummySamplePost.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if(it.data?.success == true) {
                        moveToSuccessPage(it.data?.message, model.transactionNumber)
                    }else {
                        showErrorDialogWithFAQLink(requireContext(),it.data?.message.toString())
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(),it.error.toString())
                }
                else -> {
                }
            }
        }
    }

    private fun setUpAdapter(items: MutableList<VegaQualityParamsWithQualitative>) {
        binding.rvPpqParams.setUpAdapter(items,
            R.layout.item_vega_common_dummy_quality_param,
            ItemVegaCommonDummyQualityParamBinding::inflate,
            { it, pos, bindItem ->
                val it1 = it.qualityParameter

                val spannable: SpannableStringBuilder?
                when {
                    it1.qualityParamLabel.isNullOrEmpty() -> {
                        spannable = SpannableStringBuilder(it1.descrChar)
                        spannable.insert(spannable.length, "")
                        if (it1.vegaMandatory.isNullOrEmpty()) {
                            spannable.insert(spannable.length, "")
                        } else {
                            if (it1.vegaMandatory?.equals("X")!! || it1.entryObligatory?.equals("X")!! || it1.preSampling?.equals(
                                    "X"
                                )!!
                            ) {
                                spannable.insert(spannable.length, "*")
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.RED),
                                    it1.descrChar?.length!!,
                                    it1.descrChar?.length!! + 1,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                )
                            } else {
                                spannable.insert(spannable.length, "")
                            }
                        }

                    }
                    else -> {
                        spannable = SpannableStringBuilder(it1.qualityParamLabel)
                        spannable.insert(spannable.length, "")
                        if (it1.vegaMandatory.isNullOrEmpty()) {
                            spannable.insert(spannable.length, "")
                        } else {
                            if (it1.vegaMandatory?.equals("X")!! || it1.entryObligatory?.equals("X")!! || it1.preSampling?.equals(
                                    "X"
                                )!!
                            ) {
                                spannable.insert(spannable.length, "*")
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.RED),
                                    it1.qualityParamLabel?.length!!,
                                    it1.qualityParamLabel?.length!! + 1,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                )
                            } else {
                                spannable.insert(spannable.length, "")
                            }
                        }
                    }
                }
                when (it1.dataType) {
                    //"CHAR" -> itemView.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
                    "NUM" -> bindItem.edQualityValue.inputType =
                        InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                    else -> bindItem.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
                }

                when (it1.unitsOfMeasure.isNullOrEmpty()) {
                    true -> bindItem.tvUnit.visibility = View.INVISIBLE
                    false -> bindItem.tvUnit.visibility = View.VISIBLE
                }

                when {
                    it1.formulaParam.equals("X") -> {
                        bindItem.edQualityValue.isFocusable = false
                        bindItem.edQualityValue.isCursorVisible = false
                        bindItem.root.alpha = 0.5f
                        bindItem.edQualityValue.isEnabled = false
                        bindItem.edQualityValue.isClickable = false
                        bindItem.spItem.isEnabled = false
                        bindItem.spItem.isClickable = false
                    }
                }

                when {
                    it1.preSampling.equals("X") -> {
                        bindItem.edQualityValue.isFocusable = false
                        bindItem.edQualityValue.isCursorVisible = false
                        bindItem.root.alpha = 0.5f
                        bindItem.edQualityValue.isEnabled = false
                        bindItem.edQualityValue.isClickable = false
                        bindItem.spItem.isEnabled = false
                        bindItem.spItem.isClickable = false
                        bindItem.edQualityValue.setText(it1.qualityParameterValue)
                    }
                    else -> {
                        bindItem.edQualityValue.setText(it1.qualityParameterValue)
                    }
                }
                if (it.qualitative?.isNotEmpty() == true) {
                    when {
                        it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> bindItem.spItem.isEnabled = false
                        else -> bindItem.spItem.isEnabled = true
                    }

                    if (it1.mandatory?.equals(1)!!) bindItem.llDropDown.let {
                        ViewCompat.setBackground(
                            it, ContextCompat.getDrawable(
                                bindItem.root.context,
                                com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                            )
                        )
                    }
                    else bindItem.llDropDown.let {
                        ViewCompat.setBackground(
                            it, ContextCompat.getDrawable(
                                bindItem.root.context, com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                            )
                        )
                    }
                    bindItem.llDropDown.visible()
                    bindItem.llEdit.gone()
                    val charValue = mutableListOf<String>()
                    charValue.add(0, "Select")

                    charValue.addAll(it.qualitative!!.filter { it.materialCode == it1.materialCode }
                        .map { data -> data.descValue })
                   bindItem.spItem.setText(it.qualityParameter.qualityParameterValue)
                    bindItem.spItem.setOnClickListener {
                        showSingleSelectDialog(
                            "Select Item", QUALITY, charValue, bindItem, pos
                        )
                    }

                } else {
                    handleScroll(bindItem.edQualityValue)
                    when {
                        it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> bindItem.edQualityValue.isEnabled =
                            false
                        else -> bindItem.edQualityValue.isEnabled = true
                    }
                    if (!it1.numValFm.isNullOrEmpty()) {
                        spannable.insert(
                            spannable.length,
                            "\n(".plus(it1.numValFm?.trim()).plus(" - ").plus(it1.numValTo?.trim()).plus(")")
                        )
                        when {
                            it1.qualityParamLabel.isNullOrEmpty() -> {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.LTGRAY),
                                    it1.descrChar?.length!! + 1,
                                    spannable.length,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                )
                            }
                            else -> {
                                spannable.setSpan(
                                    ForegroundColorSpan(Color.LTGRAY),
                                    it1.qualityParamLabel?.length!! + 1,
                                    spannable.length,
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                )
                            }
                        }


                    }

                    if (it1.mandatory?.equals(1)!!) bindItem.llEdit.let {
                        ViewCompat.setBackground(
                            it, ContextCompat.getDrawable(
                                bindItem.root.context,
                                com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                            )
                        )
                    }
                    else bindItem.llEdit.let {
                        ViewCompat.setBackground(
                            it, ContextCompat.getDrawable(
                                bindItem.root.context, com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                            )
                        )
                    }
                    bindItem.llDropDown.gone()
                    bindItem.llEdit.visible()
                    bindItem.edQualityValue.onChange {
                        currentPosition = pos
                        updateValues(it)
                    }
                }
                if (IS_VIEW_QUALITY) {
                    bindItem.edQualityValue.isEnabled = false
                    bindItem.spItem.isEnabled = false
                }
                bindItem.tvQualityName.text = spannable
            })
    }

    private fun updateValues(it: String) {
        qualityParamList.get(currentPosition).qualityParameter.qualityParameterValue = it
    }

    private var currentBindItem: ItemVegaCommonDummyQualityParamBinding? = null
    private var currentPosition: Int = 0
    private fun showSingleSelectDialog(
        title: String,
        currentFlag: String,
        charValue: MutableList<String>,
        bindItem: ItemVegaCommonDummyQualityParamBinding,
        pos: Int
    ) {
        currentBindItem = bindItem
        currentPosition = pos
        val list = java.util.ArrayList<String>()
        when (currentFlag) {
            QUALITY -> {
                list.clear()
                list.addAll(charValue)
            }
        }
        customDialog1 = context?.let {
            VegaCommonSingleSelectDialogWithSearch(
                title, currentFlag, list, it, this
            )
        }
        customDialog1?.show()
        customDialog1?.setCanceledOnTouchOutside(false)
    }

    override fun clickOnItem(data: String, currentFlag: String) {
        customDialog1?.dismiss()
        when (currentFlag) {
            QUALITY -> {
                currentBindItem?.spItem?.text = data
                qualityParamList.get(currentPosition).qualityParameter.qualityParameterValue = data
            }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(moveFrag: String)
    }

    private fun moveToSuccessPage(message: String?, tranId: String) {
        val intent = Intent(activity, SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        intent.putExtra(
            AppUtils.SUB_TITLE,
            tranId
        )
        startActivity(intent)
        activity?.finish()
    }
}
