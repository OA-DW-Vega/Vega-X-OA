package com.olam.warehouse.vegax.qualityapproveindiacoffee.ui.quality

import android.R
import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.Constants.COMPLAINCE_PARAMS
import com.olam.warehouse.presentation.utils.Constants.SOURCE_LOT_PARAMS
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityapproveindiacoffee.databinding.ItemVegaQualtyApproveIndiaCoffeeParamsBinding
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.QUALITY
import com.olam.warehouse.vegax.qualityapproveindiacoffee.utils.getColor


class VegaIndiaCoffeeQualityApproveParamsAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaIndiaCoffeeQualityApproveParamsAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var filterList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var primaryRefraction: Double = 0.0
    private var secondaryRefraction: Double = 0.0
    private var grnQty: Double = 0.0
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        /* val v = LayoutInflater.from(parent.context)
             .inflate(R.layout.item_vega_qualty_approve_india_coffee_params, parent, false)*/
        val v = ItemVegaQualtyApproveIndiaCoffeeParamsBinding.inflate(
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

    fun upadteFilter(items: List<VegaQualityParamsWithQualitative?>) {
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
        context: Context?
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.challanNo = challanNo
        this.context = context
        qualityParameter.let {
            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
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

    fun updateMissedPos(
        missedPos: MutableList<Int>,
        data: ArrayList<VegaQualityParamsWithQualitative?>
    ) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: ItemVegaQualtyApproveIndiaCoffeeParamsBinding) :
        RecyclerView.ViewHolder(itemView.root), VegaSingleSelectCommonListener {
        val binding = itemView
        private var customDialog1: VegaCommonSingleSelectDialogWithSearch? = null
        var i = 0
        fun bindItems(qualityParameter: VegaQualityParamsWithQualitative?, position: Int) {
            try {
                if (getCurrentKey().contains("VEGA_IN")) {
                    binding.llParam1.visible()
                    binding.llParam2.gone()
                    qualityParameter?.qualityParameter?.let { it1 ->
                        i++
                        if (i % 2 == 0) {
                            this.itemView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                        } else {
                            this.itemView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                        }
                        when (it1.descrChar) {
                            "Bean Smoky" -> {
                                binding.tvQualityNameApprove.text = it1.descrChar
                                if (it1.qualityParameterValue == "IR-AB/PR 003")
                                    binding.tvUnitApprove.setText("Absence")
                                else if (it1.qualityParameterValue == "IR-AB/PR 004")
                                    binding.tvUnitApprove.setText("Presence")
                            }

                            "Classment" -> {
                                binding.tvQualityNameApprove.text = it1.descrChar
                                if (it1.qualityParameterValue == "CLASCOCO 0001")
                                    binding.tvUnitApprove.setText("GF-G1")
                                else if (it1.qualityParameterValue == "CLASCOCO 0002")
                                    binding.tvUnitApprove.setText("FF-G2")
                                else if (it1.qualityParameterValue == "CLASCOCO 0003")
                                    binding.tvUnitApprove.setText("FAG-H/S")
                            }

                            else -> {
                                binding.tvQualityNameApprove.text = it1.descrChar
                                binding.tvUnitApprove.setText(it1.qualityParameterValue)
                            }
                        }
//                    binding.tvQualityNameApprove.text = it1.descrChar
//                    binding.tvUnitApprove.text = it1.qualityParameterValue
                    }
                } else {
                    binding.llParam1.gone()
                    binding.llParam2.visible()
                    mQtyParamsList[adapterPosition]?.qualityParameter?.let { it1 ->
                        val spannable: SpannableStringBuilder?
                        when {
                            it1.qualityParamLabel.isNullOrEmpty() -> {
                                spannable = SpannableStringBuilder(it1.descrChar)
                                spannable.insert(spannable.length, "")
                                if (it1.vegaMandatory.isNullOrEmpty() || it1.vegaMandatory?.contains("Q") == false) {
                                    spannable.insert(spannable.length, "")
                                } else {
                                    if (it1.vegaMandatory?.contains("Q")!! || it1.entryObligatory?.equals(
                                            "X"
                                        )!! || it1.preSampling?.equals(
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
                                if (/*it1.vegaMandatory.isNullOrEmpty()*/it1.vegaMandatory?.contains("Q") == false) {
                                    spannable.insert(spannable.length, "")
                                } else {
                                    if (it1.vegaMandatory?.contains("Q")!! /*|| it1.entryObligatory?.equals(
                                        "X"
                                    )!!*/ || it1.preSampling?.equals(
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
                            "NUM" -> binding.edQualityValue.inputType =
                                InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

                            else -> binding.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
                        }

                        when (it1.unitsOfMeasure.isNullOrEmpty()) {
                            true -> binding.tvUnit.visibility = View.INVISIBLE
                            false -> binding.tvUnit.visibility = View.VISIBLE
                        }

                        when {
                            it1.nameChar.equals(SOURCE_LOT_PARAMS) ||it1.nameChar.equals(COMPLAINCE_PARAMS) -> {
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
                                /*binding.edQualityValue.isFocusable = false
                                binding.edQualityValue.isCursorVisible = false
                                itemView.alpha = 0.5f
                                binding.edQualityValue.isEnabled = false
                                binding.edQualityValue.isClickable = false
                                binding.spItem.isEnabled = false
                                binding.spItem.isClickable = false*/
                                binding.edQualityValue.setText(it1.qualityParameterValue?.trim())
                            }

                            else -> {
                                binding.edQualityValue.setText(it1.qualityParameterValue?.trim())
                            }
                        }
                        if (qualityParameter?.qualitative!!.isNotEmpty()) {

                            /*when {
                                it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> binding.spItem.isEnabled =
                                    false
                                else -> binding.spItem.isEnabled = true
                            }*/

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
                            //charValue.add(0, "Select")

                            /* if(it1.nameChar.contains("GTRM0062") || it1.nameChar.contains("GTRM0063")) {
                                 charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                     .map { data -> data.descValue })
                             } else*/
                            /* charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                 .map { data -> data.descValue.plus(" - ").plus(data.charValue) })*/

                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.descValue })
                            binding.spItem.setOnClickListener {
                                showSingleSelectDialog(
                                    "Select Item".plus(it1.additionalValIndicator),
                                    QUALITY,
                                    charValue
                                )
                            }
                            if (it1.qualityParameterValue?.isNotEmpty() == true) {
                                if (!it1.preSampling.equals("E") &&
                                    !getCurrentKey().contains("VEGA_MX") && !getCurrentKey().contains("VEGA_CO") && !getCurrentKey().contains(
                                        "VEGA_PE"
                                    ) && !getCurrentKey().contains("VEGA_HN")
                                ) {
//                                    binding.spItem.isEnabled = false
//                                    binding.spItem.isClickable = false
                                }

                                binding.spItem.text = it1.qualityParameterValue!!

                            }
                            //if(it1.nameChar == "CERTCOFFEE" && getCurrentKey().contains("VEGA_PE")) binding.spItem.text = it1.qualityParameterValue!!
                            /*val charValueAdapter =
                                ArrayAdapter(
                                    itemView.spItem.context,
                                    android.R.layout.simple_list_item_1,
                                    charValue
                                )
                            itemView.spItem.adapter = charValueAdapter
                            if (it1.qualityParameterValue?.isNotEmpty() == true) {
                                itemView.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                                if (!it1.preSampling.equals("E")) {
                                    itemView.spItem.isEnabled = false
                                    itemView.spItem.isClickable = false
                                }
                            }

                            itemView.spItem.onItemSelectedListener =
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
                                }*/

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
                                    "\n(".plus(it1.numValFm?.trim()).plus(" - ")
                                        .plus(it1.numValTo?.trim()).plus(")")
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

                            binding.edQualityValue.setText(
                                it1.qualityParameterValue?.trim(),
                                TextView.BufferType.EDITABLE
                            )
                            binding.tvUnit.text = it1.unitsOfMeasure
                            when (it1.nameChar.contains("BAG_QTY") && it1.qualityParameterValue?.isNotEmpty() ?: false) {
                                true -> binding.edQualityValue.isEnabled = false
                                false -> binding.edQualityValue.isEnabled = true
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
                                if (!it1.numberDigits.isNullOrEmpty()) it1.numberDigits.toString()
                                    .trim()
                                    .toInt() - it1.numberDecimals.toString().trim().toInt() else 0
                            val afterDec =
                                if (!it1.numberDecimals.isNullOrEmpty()) it1.numberDecimals.toString()
                                    .trim().toInt() else 0

                            binding.edQualityValue.onChange {
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
                                                    updateAdapter(
                                                        editVal,
                                                        adapterPosition,
                                                        it1.unitsOfMeasure
                                                    )
                                                } else {
                                                    updateAdapter("", adapterPosition, it1.unitsOfMeasure)
                                                    binding.edQualityValue.setError(
                                                        context?.getString(com.olam.warehouse.vegax.qualityapproveindiacoffee.R.string.decimal_out_of_range),
                                                        null
                                                    )
                                                }
                                            } else {
                                                updateAdapter(
                                                    editVal,
                                                    adapterPosition,
                                                    it1.unitsOfMeasure
                                                )
                                            }
                                        } else {
                                            updateAdapter("", adapterPosition, it1.unitsOfMeasure)
                                            binding.edQualityValue.setError(
                                                context?.getString(com.olam.warehouse.vegax.qualityapproveindiacoffee.R.string.value_out_of_range),
                                                null
                                            )
                                        }
                                    } else if (afterDec != 0 && beforeDec != 0) {
                                        if (validateParamValue(editVal, beforeDec, afterDec)) {
                                            updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                        } else {
                                            updateAdapter("", adapterPosition, it1.unitsOfMeasure)
                                            binding.edQualityValue.setError(
                                                context?.getString(com.olam.warehouse.vegax.qualityapproveindiacoffee.R.string.value_out_of_range),
                                                null
                                            )
                                        }
                                    } else {
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    }
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        binding.tvQualityName.text = spannable
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

        private fun showSingleSelectDialog(
            title: String,
            currentFlag: String,
            charValue: MutableList<String>
        ) {
            val list = java.util.ArrayList<String>()
            when (currentFlag) {
                QUALITY -> {
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
                QUALITY -> {
                    binding.spItem.text = data
                    updateAdapter(
                        data,
                        adapterPosition,
                        ""
                    )

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
        private fun handleScroll(editText: TextView) {
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
