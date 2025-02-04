package com.olam.warehouse.vegax.qualityghana.ui.param

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
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityghana.R
import kotlinx.android.synthetic.main.item_vega_ghana_quality_params.view.*

/**
 * Created by Baskaran Kannan on 9/25/2020.
 */
class VegaGhanaQualityParamsAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaGhanaQualityParamsAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var bagCount: String? = ""
    private var plant: String? = ""
    private var isOfflineData: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_ghana_quality_params, parent, false)
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
        challanNo: String?,
        bagCount: String?,
        plant: String?,
        isOfflineData: Boolean
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.challanNo = challanNo
        this.bagCount = bagCount
        this.plant = plant
        this.isOfflineData = isOfflineData
        qualityParameter.let {
            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
        }
        mQtyParamsList.forEach {
            it?.qualityParameter?.nameChar?.let { it1 ->
                if (it1.contains("BAGCOUNT") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                    it.qualityParameter.qualityParameterValue = bagCount
                } else if (it1.contains("PLANT") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                    it.qualityParameter.qualityParameterValue = plant
                } else if (it1.contains("GRNQTY1") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                    it.qualityParameter.qualityParameterValue =
                        String.format("%.2f", if (netWeight?.isNotEmpty()!!) netWeight.toDouble() else 0.0)
                            .replace(",", ".")
                } else if (it1.contains("LOBM_UDCODE") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                    it.qualitative?.forEach { qualitative ->
                        if (qualitative.descValue.contains("Accept"))
                            it.qualityParameter.qualityParameterValue = qualitative.charValue
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun removeItems() {
        mQtyParamsList.clear()
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<VegaQualityParamsWithQualitative?> {
        return mQtyParamsList
    }

    fun updateMissedPos(missedPos: MutableList<Int>, data: ArrayList<VegaQualityParamsWithQualitative?>) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(qualityParameter: VegaQualityParamsWithQualitative?, position: Int) {
            try {
                qualityParameter?.qualityParameter?.let { it1 ->
                    val spannable: SpannableStringBuilder?
                    when {
                        it1.qualityParamLabel.isNullOrEmpty() -> {
                            spannable = SpannableStringBuilder(it1.descrChar)
                            spannable.insert(spannable.length, "")
                            if (it1.entryObligatory.isNullOrEmpty()) {
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
                            if (it1.entryObligatory.isNullOrEmpty()) {
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
                        "NUM" -> itemView.edQualityValue.inputType =
                            InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                        else -> itemView.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
                    }

                    when (it1.unitsOfMeasure.isNullOrEmpty()) {
                        true -> itemView.tvUnit.visibility = View.INVISIBLE
                        false -> itemView.tvUnit.visibility = View.VISIBLE
                    }

                    when {
                        it1.formulaParam.equals("X") -> {
                            itemView.edQualityValue.isFocusable = false
                            itemView.edQualityValue.isCursorVisible = false
                            itemView.alpha = 0.5f
                            itemView.edQualityValue.isEnabled = false
                            itemView.edQualityValue.isClickable = false
                            itemView.spItem.isEnabled = false
                            itemView.spItem.isClickable = false
                        }
                    }

                    when {
                        it1.preSampling.equals("X") -> {
                            itemView.edQualityValue.isFocusable = false
                            itemView.edQualityValue.isCursorVisible = false
                            itemView.alpha = 0.5f
                            itemView.edQualityValue.isEnabled = false
                            itemView.edQualityValue.isClickable = false
                            itemView.spItem.isEnabled = false
                            itemView.spItem.isClickable = false
                            itemView.edQualityValue.setText(it1.qualityParameterValue)
                        }
                        else -> {
                            itemView.edQualityValue.setText(it1.qualityParameterValue)
                            if (isOfflineData) {
                                itemView.edQualityValue.isFocusable = false
                                itemView.edQualityValue.isCursorVisible = false
                                itemView.edQualityValue.isEnabled = false
                                itemView.edQualityValue.isClickable = false
                                itemView.spItem.isEnabled = false
                                itemView.spItem.isClickable = false
                            }
                        }
                    }
                    if (qualityParameter.qualitative!!.isNotEmpty()) {

                        when {
                            it1.formulaParam.equals("X") || it1.preSampling.equals("X") || isOfflineData -> itemView.spItem.isEnabled =
                                false
                            else -> itemView.spItem.isEnabled = true
                        }

                        if (it1.mandatory?.equals(1)!!) itemView.llDropDown.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                                )
                            )
                        }
                        else itemView.llDropDown.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                )
                            )
                        }
                        itemView.llDropDown.visible()
                        itemView.llEdit.gone()

                        val charValue = mutableListOf<String>()
                        charValue.add(0, itemView.context.getString(R.string.select))
                        if (it1.nameChar.equals("LOBM_UDCODE")) {
                            charValue.addAll(qualityParameter.qualitative!!.filter {
                                it.materialCode == it1.materialCode && it.descValue.contains("Accept")
                            }.map { data -> data.charValue })
                            itemView.spItem.isEnabled = false
                        } else {
                            itemView.spItem.isEnabled = true
                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })
                        }
                        val charValueAdapter =
                            ArrayAdapter(itemView.spItem.context, android.R.layout.simple_list_item_1, charValue)
                        itemView.spItem.adapter = charValueAdapter
                        if (it1.qualityParameterValue?.isNotEmpty()!!)
                            itemView.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                        itemView.spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                updateAdapter(
                                    if (pos > 0) charValue[pos] else "",
                                    adapterPosition,
                                    it1.unitsOfMeasure
                                )
                            }
                        }

                    } else {
                        handleScroll(itemView.edQualityValue)

                        when {
                            it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> itemView.edQualityValue.isEnabled =
                                false
                            else -> itemView.edQualityValue.isEnabled = true
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

                        if (it1.mandatory?.equals(1)!!) itemView.llEdit.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                                )
                            )
                        }
                        else itemView.llEdit.let {
                            ViewCompat.setBackground(
                                it,
                                ContextCompat.getDrawable(
                                    itemView.context,
                                    com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                )
                            )
                        }

                        itemView.llEdit.visible()
                        itemView.llDropDown.gone()

                        itemView.edQualityValue.setText(it1.qualityParameterValue, TextView.BufferType.EDITABLE)
                        itemView.tvUnit.text = it1.unitsOfMeasure
                        when (it1.nameChar.contains("PESTAR") || it1.nameChar.contains("PESNET") ||
                                it1.nameChar.contains("PESBRT") || it1.nameChar.contains("BAGCOUNT") || it1.nameChar.contains(
                            "PLANT"
                        ) || it1.nameChar.contains("GRNPRICE1") || it1.nameChar.contains("GRNQTY1") || (it1.nameChar == "Vega_TRANSACTION_NO" && it1.qualityParameterValue?.isNotEmpty() ?: false)) {
                            true -> itemView.edQualityValue.isEnabled = false
                            false -> itemView.edQualityValue.isEnabled = true
                        }

                        val numFrom = if (!it1.numValFm.isNullOrEmpty()) it1.numValFm!!.trim().replace(
                            ",",
                            "."
                        ).toDouble() else 0.0
                        val numTo = if (!it1.numValTo.isNullOrEmpty()) it1.numValTo!!.trim().replace(
                            ",",
                            "."
                        ).toDouble() else 0.0
                        val beforeDec =
                            if (!it1.numberDigits.isNullOrEmpty()) it1.numberDigits.toString()
                                .toInt() - it1.numberDecimals.toString().trim().toInt() else 0
                        val afterDec =
                            if (!it1.numberDecimals.isNullOrEmpty()) it1.numberDecimals.toString().trim().toInt() else 0

                        itemView.edQualityValue.onChange {
                            try {
                                if (it.isEmpty()) {
                                    qualityParameter.qualityParameter.qualityParameterValue = ""
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
                                            } else {
                                                itemView.edQualityValue.setError("Decimal out of range!", null)
                                            }
                                        } else {
                                            updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                        }
                                    } else {
                                        itemView.edQualityValue.setError("value out of range!", null)
                                    }
                                } else if (afterDec != 0 && beforeDec != 0) {
                                    if (validateParamValue(editVal, beforeDec, afterDec)) {
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    } else {
                                        itemView.edQualityValue.setError("value out of range!", null)
                                    }
                                } else {
                                    updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                }
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    itemView.tvQualityName.text = spannable
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    }
}

