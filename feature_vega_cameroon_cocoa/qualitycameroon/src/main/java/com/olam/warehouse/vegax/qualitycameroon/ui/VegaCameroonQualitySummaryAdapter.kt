package com.olam.warehouse.vegax.qualitycameroon.ui

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
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualitycameroon.R
import kotlinx.android.synthetic.main.item_vega_cameroon_quality_params.view.*

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class VegaCameroonQualitySummaryAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaCameroonQualitySummaryAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_cameroon_quality_params, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyParamsList.size
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
                        }
                    }



                    if (qualityParameter.qualitative!!.isNotEmpty()) {

                        when {
                            it1.formulaParam.equals("X") || it1.preSampling.equals("X") -> itemView.spItem.isEnabled =
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
                        var preValue = qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                            .filter { it.charValue == it1.qualityParameterValue }
                        it1.qualityParameterValue = preValue[0].descValue
                        val charValue = mutableListOf<String>()
                        charValue.add(0, "Select")
                        charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                            .map { data -> data.descValue })
                        val charValueAdapter =
                            ArrayAdapter(itemView.spItem.context, android.R.layout.simple_list_item_1, charValue)
                        itemView.spItem.adapter = charValueAdapter
                        if (it1.qualityParameterValue?.isNotEmpty()!!)
                            itemView.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                        itemView.spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                // Commenting for empty function SonarQube fix
                            }

                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                updateAdapter(if (pos > 0) charValue[pos] else "", adapterPosition, it1.unitsOfMeasure)
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
                                it1.nameChar.contains("PESBRT") || (it1.nameChar == "Vega_TRANSACTION_NO" && it1.qualityParameterValue?.isNotEmpty() ?: false)) {
                            true -> itemView.edQualityValue.isEnabled = false
                            false -> itemView.edQualityValue.isEnabled = true
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


        @SuppressLint("WrongConstant")
        private fun handleScroll(editText: EditText) {
            editText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    val view = textView.focusSearch(View.FOCUS_FORWARD)
                    if (view != null && (!view.requestFocus(View.FOCUS_FORWARD))) {
                        return@OnEditorActionListener true
                    }
                    return@OnEditorActionListener false
                }
                false
            })
        }
    }
}
