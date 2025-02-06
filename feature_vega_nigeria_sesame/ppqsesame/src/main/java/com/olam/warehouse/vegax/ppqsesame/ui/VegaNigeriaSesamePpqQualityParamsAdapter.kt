package com.olam.warehouse.vegax.ppqsesame.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ppqsesame.databinding.ItemVegaSesamePpqParamsBinding
import com.olam.warehouse.vegax.ppqsesame.utils.materialQualityGradeList

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class VegaNigeriaSesamePpqQualityParamsAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaNigeriaSesamePpqQualityParamsAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var materialname: String? = ""
    var gradeListFilter = mutableListOf<VegaQualitative>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_sesame_ppq_params, parent, false)
        val v = ItemVegaSesamePpqParamsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyParamsList.size
    }

    fun updateFilter(items: List<VegaQualityParamsWithQualitative?>) {
        val data1 = items.filter { it?.qualityParameter?.entryObligatory?.isNotEmpty()!! }
        val data2 = items.filter { it?.qualityParameter?.entryObligatory?.isEmpty()!! }
        mQtyParamsList.clear()
        mQtyParamsList.addAll(data1)
        mQtyParamsList.addAll(data2)
        notifyItemRangeChanged(0, mQtyParamsList.size)

    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        try {
            holder.bindItems(mQtyParamsList[position], position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addItems(
        qualityParameter: List<VegaQualityParamsWithQualitative>,
        tarWeight: String?,
        netWeight: String?,
        challanNo: String?
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.challanNo = challanNo
        qualityParameter.let {
            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
        }
        if (getCurrentKey().split("_")[1].contains("NI"))
        materialname = challanNo
        notifyDataSetChanged()
    }

    fun removeItems() {
        mQtyParamsList.clear()
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<VegaQualityParamsWithQualitative?> {
        return mQtyParamsList
    }

    fun updateMissedPos(
        missedPos: MutableList<Int>,
        data: ArrayList<VegaQualityParamsWithQualitative?>
    ) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: ItemVegaSesamePpqParamsBinding) :
        RecyclerView.ViewHolder(itemView.root),
        VegaSingleSelectCommonListener {
        val binding = itemView
        private var customDialog1: VegaCommonSingleSelectDialogWithSearch? = null

        fun bindItems(qualityParameter: VegaQualityParamsWithQualitative?, position: Int) {
            try {
                qualityParameter?.qualityParameter?.let { it1 ->
                    var spannable: SpannableStringBuilder?
                    when {
                        it1.qualityParamLabel.isNullOrEmpty() -> {
                            spannable = SpannableStringBuilder(it1.descrChar)
                            spannable.insert(spannable.length, "")
                            if (it1.vegaValueMandatory.isNullOrEmpty()) {
                                spannable.insert(spannable.length, "")
                            } else {
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

                            if (getCurrentKey().split("_")[1].contains("NI")) {
                                if ( it1.nameChar.contains("NIFG0014") && !materialname!!.contains("Certificado",true))
                                {
                                    spannable.insert(spannable.length, "")

                                }else{
                                    spannable.insert(spannable.length, "*")
                                    spannable.setSpan(
                                        ForegroundColorSpan(Color.RED),
                                        it1.descrChar?.length!!,
                                        it1.descrChar?.length!! + 1,
                                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                    )
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
                        "NUM" -> binding.edQualityValue.inputType =
                            InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                        else -> binding.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
                    }
                    when (it1.unitsOfMeasure.isNullOrEmpty()) {
                        true -> binding.tvUnit.visibility = View.INVISIBLE
                        false -> binding.tvUnit.visibility = View.VISIBLE
                    }
                    when {
                        it1.formulaParam.equals("X") -> {
                            binding.edQualityValue.isFocusable = false
                            binding.edQualityValue.isCursorVisible = false
                            itemView.alpha = 0.5f
                            binding.edQualityValue.isEnabled = false
                            binding.edQualityValue.isClickable = false
                            binding.spItem.isEnabled = false
                            binding.spItem.isClickable = false
                        }
                    }
                    when {
                        it1.preSampling.equals("X") -> {
                            binding.edQualityValue.isFocusable = false
                            binding.edQualityValue.isCursorVisible = false
                            itemView.alpha = 0.5f
                            binding.edQualityValue.isEnabled = false
                            binding.edQualityValue.isClickable = false
                            binding.spItem.isEnabled = false
                            binding.spItem.isClickable = false
                            binding.edQualityValue.setText(it1.qualityParameterValue)
                        }
                        else -> {
                            binding.edQualityValue.setText(it1.qualityParameterValue)
                        }
                    }
                    if (qualityParameter.qualitative!!.isNotEmpty()) {

                        when {
                            it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> binding.spItem.isEnabled =
                                false
                            else -> binding.spItem.isEnabled = true
                        }

                        if (it1.mandatory?.equals(1)!!) binding.llDropDown.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                                )
                            )
                        }
                        else binding.llDropDown.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                )
                            )
                        }
                        binding.llDropDown.visible()
                        binding.llEdit.gone()
                        val charValue = mutableListOf<String>()
                        val descValue = mutableListOf<String>()
                        descValue.add(0, "Select")
                        charValue.add(0, "Select")
                        if (getCurrentKey().split("_")[1].contains("NI") && it1.nameChar.contains("NIPOSITI")) {

                            var gradeList =
                                qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                            materialQualityGradeList.forEach { qualityGrade ->
                                gradeListFilter.addAll(gradeList.filter {
                                    it.charValue.split(" ")
                                        .get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                                })
                            }
                            descValue.addAll(gradeListFilter.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue.plus(" - ").plus(data.descValue) })

                            charValue.addAll(gradeListFilter.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })

                            binding.nicSpItem.visible()
                            binding.spItem.gone()
                            binding.nicSpItem.setOnClickListener {
                                showSingleSelectDialog(
                                    "Select Item",
                                    "QUALITYGRADE",
                                    descValue
                                )
                            }

                        } else if (getCurrentKey().split("_")[1].contains("NI")) {

                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })

                            binding.nicSpItem.visible()
                            binding.spItem.gone()
                            binding.nicSpItem.setOnClickListener {
                                showSingleSelectDialog(
                                    "Select Item",
                                    "QUALITY",
                                    charValue
                                )
                            }

                        } else {
                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })
                            val charValueAdapter =
                                ArrayAdapter(
                                    binding.spItem.context,
                                    android.R.layout.simple_list_item_1,
                                    descValue
                                )
                            binding.spItem.adapter = charValueAdapter


                        }
                        if (getCurrentKey().split("_")[1].contains("NI")) {

                            /*val charValueAdapter =
                                ArrayAdapter(
                                    binding.spItem.context,
                                    android.R.layout.simple_list_item_1,
                                    descValue
                                )
                            binding.spItem.adapter = charValueAdapter
                            */

                            if (it1.qualityParameterValue?.isNotEmpty()!!) {
                                descValue.forEachIndexed { index, s ->
                                    var postgradecode = charValue.get(index).toString()
                                    if (postgradecode.replace("\\s+".toRegex(), " ").equals(
                                            it1.qualityParameterValue?.replace("\\s+".toRegex(), " ")
                                        )
                                    )
                                        binding.nicSpItem.text = s
                                    else
                                        binding.nicSpItem.text = it1.qualityParameterValue?.toString()
                                }
                            }
                        } else {
                            val charValueAdapter =
                                ArrayAdapter(
                                    binding.spItem.context,
                                    android.R.layout.simple_list_item_1,
                                    charValue
                                )
                            binding.spItem.adapter = charValueAdapter


                            if (it1.qualityParameterValue?.isNotEmpty()!!)
                                binding.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                        }

                        binding.spItem.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onNothingSelected(p0: AdapterView<*>?) {}

                                override fun onItemSelected(
                                    p0: AdapterView<*>?,
                                    p1: View?,
                                    pos: Int,
                                    p3: Long
                                ) {
                                    updateAdapter(
                                        if (pos > 0) charValue[pos] else "",
                                        adapterPosition,
                                        it1.unitsOfMeasure
                                    )
                                }
                            }
                    } else {
                        handleScroll(binding.edQualityValue)

                        when {
                            it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> binding.edQualityValue.isEnabled =
                                false
                            else -> binding.edQualityValue.isEnabled = true
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

                        if (it1.mandatory?.equals(1)!!) binding.llEdit.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                                )
                            )
                        }
                        else binding.llEdit.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                )
                            )
                        }

                        binding.llEdit.visible()
                        binding.llDropDown.gone()
                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            it1.qualityParameterValue = it1.qualityParameterValue?.replace("%", "")
                            it1.qualityParameterValue = it1.qualityParameterValue?.replace("kg", "")
                            it1.qualityParameterValue = it1.qualityParameterValue?.trim()
                        }
                        binding.edQualityValue.setText(
                            it1.qualityParameterValue,
                            TextView.BufferType.EDITABLE
                        )
                        binding.tvUnit.text = it1.unitsOfMeasure
                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            binding.edQualityValue.isEnabled = true
                        } else {
                            when (it1.nameChar.contains("PESTAR") || it1.nameChar.contains("PESNET") ||
                                    it1.nameChar.contains("PESBRT") || (it1.nameChar == "Vega_TRANSACTION_NO" && it1.qualityParameterValue?.isNotEmpty() ?: false)) {
                                true -> binding.edQualityValue.isEnabled = false
                                false -> binding.edQualityValue.isEnabled = true
                            }
                        }

                        val numFrom =
                            if (!it1.numValFm.isNullOrEmpty()) it1.numValFm!!.trim().replace(
                                ",",
                                "."
                            ).toDouble() else 0.0
                        val numTo =
                            if (!it1.numValTo.isNullOrEmpty()) it1.numValTo!!.trim().replace(
                                ",",
                                "."
                            ).toDouble() else 0.0
                        val beforeDec =
                            if (!it1.numberDigits.isNullOrEmpty()) it1.numberDigits.toString().trim()
                                .toInt() - it1.numberDecimals.toString().trim().toInt() else 0
                        val afterDec =
                            if (!it1.numberDecimals.isNullOrEmpty()) it1.numberDecimals.toString().trim().toInt() else 0

                        binding.edQualityValue.onChange {
                            try {
                                if (it.isEmpty()) {
                                    qualityParameter.qualityParameter.qualityParameterValue = ""
                                    calculateQualityandKorValue(qualityParameter)
                                    return@onChange
                                }
                                val editVal: String?
                                val dot = it[0].toString()
                                editVal = if (dot == ".") {
                                    if (it.length == 1) "0.0" else "0".plus(it)
                                } else it

                                if (numTo != 0.0) {
                                    if (validateRangeParamValue(editVal, numFrom, numTo)) {
                                        if (afterDec != 0 && beforeDec != 0) {
                                            if (validateParamValue(editVal, beforeDec, afterDec)) {
                                                updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                                calculateQualityandKorValue(qualityParameter)
                                            } else {
                                                binding.edQualityValue.setError(
                                                    "Decimal out of range!",
                                                    null
                                                )
                                            }
                                        } else {
                                            updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                            calculateQualityandKorValue(qualityParameter)
                                        }
                                    } else {
                                        binding.edQualityValue.setError("value out of range!", null)
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    }
                                } else if (afterDec != 0 && beforeDec != 0) {
                                    if (validateParamValue(editVal, beforeDec, afterDec)) {
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                        calculateQualityandKorValue(qualityParameter)
                                    } else {
                                        binding.edQualityValue.setError("value out of range!", null)
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    }
                                } else {
                                    updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    calculateQualityandKorValue(qualityParameter)
                                }
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    binding.tvQualityName.text = spannable

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        private fun calculateQualityandKorValue(qualityParameter: VegaQualityParamsWithQualitative?) {
            if (qualityParameter?.qualityParameter?.nameChar.equals("ZNG_CASHEW_GOOD_KERNEL")
                || (qualityParameter?.qualityParameter?.nameChar.equals("ZNG_CASHEW_IMMATURE_GMS"))
                || (qualityParameter?.qualityParameter?.nameChar.equals("ZNG_CASHEW_SPOTTED_GMS"))
            ) {
                calculateKorValue()
            }
        }

        private fun calculateKorValue() {
            var goodKernal = 0.00
            var spottedCashew = 0.00
            var immatureCashew = 0.00
            mQtyParamsList.forEach {
                when (it?.qualityParameter?.nameChar) {
                    "ZNG_CASHEW_GOOD_KERNEL" -> {
                        if (it.qualityParameter.qualityParameterValue == "")
                            goodKernal = 0.00
                        else
                            goodKernal = it.qualityParameter.qualityParameterValue.toString().toDouble()
                    }
                    "ZNG_CASHEW_SPOTTED_GMS" -> {
                        if (it.qualityParameter.qualityParameterValue == "")
                            spottedCashew = 0.00
                        else
                            spottedCashew = it.qualityParameter.qualityParameterValue.toString().toDouble()
                    }
                    "ZNG_CASHEW_IMMATURE_GMS" -> {
                        if (it.qualityParameter.qualityParameterValue == "")
                            immatureCashew = 0.00
                        else
                            immatureCashew = it.qualityParameter.qualityParameterValue.toString().toDouble()
                    }
                }
            }
            if (goodKernal != 0.00 && spottedCashew != 0.00 && immatureCashew != 0.00) {
                var korValue = (((immatureCashew + spottedCashew) / 2 + goodKernal) * 0.176).formatThreeDigits()
                mQtyParamsList.forEach {
                    if (it?.qualityParameter?.nameChar == "ZNG_CASHEW_KOR") {
                        it.qualityParameter.qualityParameterValue = korValue.toString()
                        notifyDataSetChanged()
                        binding.edQualityValue.post(Runnable {
                            binding.edQualityValue.requestFocus()
                            binding.edQualityValue.setSelection(binding.edQualityValue.text.toString().length)
                        })
                    }
                }
            } else {
                mQtyParamsList.forEach {
                    if (it?.qualityParameter?.nameChar == "ZNG_CASHEW_KOR") {
                        it.qualityParameter.qualityParameterValue = ""
                        notifyDataSetChanged()
                        binding.edQualityValue.post(Runnable {
                            binding.edQualityValue.requestFocus()
                            binding.edQualityValue.setSelection(binding.edQualityValue.text.toString().length)
                        })
                    }
                }
            }


        }

        fun updateAdapter(editValue: String, position: Int, unit: String?) {
            mQtyParamsList[position]?.qualityParameter?.qualityParameterValue = editValue
            onClick(mQtyParamsList)
        }

        private fun validateParamValue(id: String?, beforeDec: Int?, afterDec: Int?): Boolean {
            return when {
                id?.isNotEmpty()!! -> {
                    var values = listOf<String?>()
                    values = when (id.contains(".")) {
                        true -> id.split(".").map { it.trim() }
                        else -> values + id
                    }
                    if (values.size == 1) {
                        values = values + "0"
                    }

                    return when {
                        values[0]?.length!! > beforeDec!! || values[1]!!.length > afterDec!! -> false
                        else -> true
                    }
                }
                else -> true
            }
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


        private fun showSingleSelectDialog(
            title: String,
            currentFlag: String,
            charValue: MutableList<String>
        ) {
            val list = java.util.ArrayList<String>()
            when (currentFlag) {
                "QUALITY" -> {
                    list.clear()
                    list.addAll(charValue)
                }
                "QUALITYGRADE" -> {
                    list.clear()
                    list.addAll(charValue)

                }
            }
            customDialog1 =
                VegaCommonSingleSelectDialogWithSearch(
                    title,
                    currentFlag,
                    list,
                    itemView.context,
                    this
                )
            customDialog1?.show()
            customDialog1?.setCanceledOnTouchOutside(false)
        }

        override fun clickOnItem(data: String, currentFlag: String) {
            customDialog1?.dismiss()
            when (currentFlag) {
                "QUALITY" -> {
                    binding.nicSpItem.text = data
                    updateAdapter(
                        data,
                        adapterPosition,
                        ""
                    )
                }
                "QUALITYGRADE" -> {
                    binding.nicSpItem.text = data


                    // val codelist = gradeListFilter.filter { it.descValue == data }.map { data -> data.charValue }

                    updateAdapter(
                        data.split(" - ")[0],
                        adapterPosition,
                        ""
                    )
                }
            }
        }

    }
}
