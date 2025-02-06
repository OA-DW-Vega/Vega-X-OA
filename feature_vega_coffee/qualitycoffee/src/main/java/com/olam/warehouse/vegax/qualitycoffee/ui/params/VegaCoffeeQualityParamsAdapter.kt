package com.olam.warehouse.vegax.qualitycoffee.ui.params

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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaQualitative
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.presentation.ui.widget.VegaCommonSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectCommonListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualitycoffee.databinding.ItemVegaCoffeeQualityParamsBinding
import com.olam.warehouse.vegax.qualitycoffee.utils.*
import java.util.*

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class VegaCoffeeQualityParamsAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaCoffeeQualityParamsAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var materialName: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_coffee_quality_params, parent, false)
        val v = ItemVegaCoffeeQualityParamsBinding.inflate(
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
        materialName: String?
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.challanNo = challanNo
        this.materialName = materialName
        if (getCurrentKey().split("_")[1].contains("NI"))
            qualityParameter.let {

                mQtyParamsListNonModify.addAll(qualityParameter)
                mQtyParamsList.addAll(qualityParameter)
            }
        else
            qualityParameter.let {
                mQtyParamsListNonModify.addAll(qualityParameter.filter {
                    !it.qualityParameter.nameChar.equals(
                        "CI_COFFEE_TRANS_ORIGIN"
                    )
                }.filter { !it.qualityParameter.nameChar.equals("CI_COFFEE_TRANS_DEPT") })
                mQtyParamsList.addAll(qualityParameter.filter {
                    !it.qualityParameter.nameChar.equals(
                        "CI_COFFEE_TRANS_ORIGIN"
                    )
                }.filter { !it.qualityParameter.nameChar.equals("CI_COFFEE_TRANS_DEPT") })
            }
        /*qualityParameter.let {x

            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
        }*/


        if (getCurrentKey().split("_")[1].contains("NI")) {
            mQtyParamsList.forEach {
                if (it?.qualityParameter?.vegaValueMandatory.equals(
                        "X",
                        true
                    )
                ) it?.qualityParameter?.entryObligatory =
                    "X"
                it?.qualityParameter?.nameChar?.let { it1 ->
                    if (it1.contains("NISACOS") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue = BAG_COUNT
                    } else if (it1.contains("STOCKBAGS") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue = BAG_COUNT
                    } else if (it1.contains("NIPESNET") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue =
                            String.format(
                                Locale.ENGLISH,
                                "%.2f",
                                if (netWeight?.isNotEmpty()!!) netWeight.toDouble() else 0.0
                            )
                    } else if (it1.contains("NIPESBRT") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue =
                            String.format(
                                Locale.ENGLISH,
                                "%.2f",
                                if (GROSS_WEIGHT.isNotEmpty()) GROSS_WEIGHT.toDouble() else 0.0
                            )
                    } else if (it1.contains("STOCKBRUTO") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue =
                            String.format(
                                Locale.ENGLISH,
                                "%.2f",
                                if (GROSS_WEIGHT.isNotEmpty()) GROSS_WEIGHT.toDouble() else 0.0
                            )
                    } else if (it1.contains("NIPESTAR") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue =
                            String.format(
                                Locale.ENGLISH,
                                "%.2f",
                                if (tarWeight?.isNotEmpty()!!) tarWeight.toDouble() else 0.0
                            )
                    } else if (it1.contains("STOCKTARA") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue =
                            String.format(
                                Locale.ENGLISH,
                                "%.2f",
                                if (tarWeight?.isNotEmpty()!!) tarWeight.toDouble() else 0.0
                            )
                    } else if (it1.contains("NIPOSITI") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue = grade

                    } else if (it1.contains("NICERTI") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        var tallySequence = PreferenceHelper.get(Constants.TALLY_SEQUENCE, "")

                        if (tallySequence.isNotEmpty() && tallysheet.isNotEmpty()) {
                            it.qualityParameter.qualityParameterValue = tallysheet
                        }
                    } else if (it1.contains("NIFG0014") && it.qualityParameter.formulaParam.isNullOrEmpty()) {
                        it.qualityParameter.qualityParameterValue = certificate
                    } else {
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

    fun updateMissedPos(
        missedPos: MutableList<Int>,
        data: ArrayList<VegaQualityParamsWithQualitative?>
    ) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: ItemVegaCoffeeQualityParamsBinding) :
        RecyclerView.ViewHolder(itemView.root),
        VegaSingleSelectCommonListener {
        val binding = itemView

        private var customDialog1: VegaCommonSingleSelectDialogWithSearch? = null

        fun bindItems(qualityParameter: VegaQualityParamsWithQualitative?, position: Int) {
            try {

                qualityParameter?.qualityParameter?.let { it1 ->
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
                                    if (getCurrentKey().split("_")[1].contains("NI")) {
                                        if (it1.nameChar.equals("NIDANO") || it1.nameChar.equals("NIFGICO") || (it1.nameChar.equals(
                                                "NIFG0014"
                                            ) && materialName?.contains("Certificado") == false)
                                        ) {
                                            spannable.insert(spannable.length, "")
                                        } else {
                                            spannable.insert(spannable.length, "*")
                                            spannable.setSpan(
                                                ForegroundColorSpan(Color.RED),
                                                it1.descrChar?.length!!,
                                                it1.descrChar?.length!! + 1,
                                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                            )

                                        }
                                    } else {
                                        spannable.insert(spannable.length, "*")
                                        spannable.setSpan(
                                            ForegroundColorSpan(Color.RED),
                                            it1.descrChar?.length!!,
                                            it1.descrChar?.length!! + 1,
                                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                                        )
                                    }
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
                        //"CHAR" -> binding.edQualityValue.inputType = InputType.TYPE_CLASS_TEXT
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
                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            binding.llDropDown.visible()
                            binding.tvspItem.visible()
                            binding.spItem.gone()
                            binding.llEdit.gone()

                        } else {
                            binding.llDropDown.visible()
                            binding.tvspItem.gone()
                            binding.spItem.visible()
                            binding.llEdit.gone()

                        }
                        val charValue = mutableListOf<String>()
                        val descValue = mutableListOf<String>()
                        var charValueAdapter: ArrayAdapter<String>? = null
                        if (getCurrentKey().split("_")[1].contains("NI") && it1.nameChar.contains("NIPOSITI")) {
                            var gradeListFilter = mutableListOf<VegaQualitative>()
                            var gradeList =
                                qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                            materialQualityGradeList.forEach { qualityGrade ->
                                gradeListFilter.addAll(gradeList.filter {
                                    it.charValue.split(" ")
                                        .get(it.charValue.split(" ").size - 1) == qualityGrade.gradeCode
                                })
                            }
                            charValue.addAll(gradeListFilter.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })
                            descValue.addAll(gradeListFilter.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue.plus(" - ".plus(data.descValue)) })


                            binding.tvspItem.setOnClickListener {
                                showSingleSelectDialog(
                                    "Select Item",
                                    QUALITYGRADEDESC,
                                    descValue
                                )
                            }
                            /*         charValueAdapter =
                                         ArrayAdapter(
                                             binding.spItem.context,
                                             android.R.layout.simple_list_item_1,
                                             descValue
                                         )*/
                        } else if (getCurrentKey().split("_")[1].contains("NI")) {
                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })
                            binding.tvspItem.setOnClickListener {
                                showSingleSelectDialog(
                                    "Select Item",
                                    QUALITY,
                                    charValue
                                )
                            }
                        } else {
                            charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue })
                            charValueAdapter =
                                ArrayAdapter(
                                    binding.spItem.context,
                                    android.R.layout.simple_list_item_1,
                                    charValue
                                )
                            binding.spItem.adapter = charValueAdapter
                        }


                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            /* if (it1.nameChar.equals("NIPOSITI") *//*|| it1.nameChar.equals("NIDANO")*//*) {
                                binding.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                                spEnable()
                            } else if (it1.nameChar.equals("NIFG0014")) {
                                if (it1.qualityParameterValue?.isNotEmpty()!!) {
                                    binding.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                                    spDisable()
                                } else {
                                    spEnable()
                                }
                            } else {
                                if (it1.qualityParameterValue?.isNotEmpty()!!) {
                                    binding.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                                    spDisable()
                                }
                            }*/
                            if (it1.qualityParameterValue?.isNotEmpty()!!)
                                if (it1.nameChar.contains("NIPOSITI"))
                                    binding.tvspItem.setText(getQualityDescriptionFromCode(it1.materialCode,it1.qualityParameterValue!!))
                                else
                                    binding.tvspItem.setText(it1.qualityParameterValue!!)

                        } else {
                            if (it1.qualityParameterValue?.isNotEmpty()!!) {
                                binding.spItem.setSelection(charValue.indexOf(it1.qualityParameterValue!!))
                                spDisable()
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
                            it1.qualityParameterValue = it1.qualityParameterValue?.replace("Kg", "")
                            it1.qualityParameterValue = it1.qualityParameterValue?.trim()
                        }
                        binding.edQualityValue.setText(
                            it1.qualityParameterValue,
                            TextView.BufferType.EDITABLE
                        )
                        binding.tvUnit.text = it1.unitsOfMeasure
                        when (it1.nameChar.contains("PESTAR") || it1.nameChar.contains("PESNET") ||
                                it1.nameChar.contains("PESBRT") || (it1.nameChar == "Vega_TRANSACTION_NO" && it1.qualityParameterValue?.isNotEmpty() ?: false)) {
                            true -> binding.edQualityValue.isEnabled = false
                            false -> binding.edQualityValue.isEnabled = true
                        }
                        if (getCurrentKey().split("_")[1].contains("NI")) {
                            when (it1.nameChar.contains("NIPESTAR") || it1.nameChar.contains("NIPESNET") ||
                                    it1.nameChar.contains("NIPESBRT") || it1.nameChar.contains("NISACOS") || it1.nameChar.contains(
                                "NICERTI"
                            ) || it1.nameChar.contains("PLANT") || it1.nameChar.contains(
                                "STOCKBAGS"
                            ) || it1.nameChar.contains("STOCKBRUTO") || it1.nameChar.contains("STOCKTARA") || it1.nameChar.contains(
                                "NIEXPOP"
                            ) || it1.nameChar.contains("NICASCAB") || it1.nameChar.contains("NIRM0003") || it1.nameChar.contains(
                                "NIRM0010"
                            ) || it1.nameChar.contains("NIIMP") || it1.nameChar.contains("NIDESA") || it1.nameChar.contains(
                                "NIDESC"
                            ) || it1.nameChar.contains("NIDESD")) {
                                true -> binding.edQualityValue.isEnabled = false
                                false -> binding.edQualityValue.isEnabled = true
                            }
                            /*if (it1.nameChar.equals("NIPESBRT") || it1.nameChar.equals("NIPESTAR") || it1.nameChar.equals(
                                    "NIPESNET"
                                )*/ // || it1.nameChar.equals("NIPESBRT_MER") || it1.nameChar.equals("NIPESTAR_MER"))/*
                            /* binding.llEdit.visible()
                           binding.llEdit.isEnabled = true
                           binding.tvUnit.visibility = View.VISIBLE
                           binding.edQualityValue.isFocusable = true
                           binding.edQualityValue.isCursorVisible = true
                           binding.edQualityValue.isEnabled = true
                           binding.edQualityValue.isClickable = true
                           }*/
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
                                .trim().toInt() - it1.numberDecimals.toString().trim()
                                .toInt() else 0
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
                                                updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                            } else {
                                                binding.edQualityValue.setError(
                                                    "Decimal out of range!",
                                                    null
                                                )
                                            }
                                        } else {
                                            updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                        }
                                    } else {
                                        binding.edQualityValue.setError("value out of range!", null)
                                    }
                                } else if (afterDec != 0 && beforeDec != 0) {
                                    if (validateParamValue(editVal, beforeDec, afterDec)) {
                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                    } else {
                                        binding.edQualityValue.setError("value out of range!", null)
                                    }
                                } else {
                                    updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                }
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    /* if(qualityParameter.qualityParameter.nameChar=="CI_COFFEE_TRANS_ORIGIN"||qualityParameter?.qualityParameter?.nameChar=="CI_COFFEE_TRANS_DEPT")
                     {
                         binding.llLabel.gone()
                         binding.llEdit.gone()
                         binding.llDropDown.gone()
                         qualityParameter.qualityParameter.mandatory=1
                     }*/
                    binding.tvQualityName.text = spannable
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun spDisable() {
            binding.spItem.isEnabled = false
            binding.spItem.isClickable = false
        }

        private fun spEnable() {
            binding.spItem.isEnabled = true
            binding.spItem.isClickable = true
            binding.spItem.isFocusable = true
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
                QUALITYGRADEDESC -> {
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
                    binding.tvspItem.text = data
                    updateAdapter(
                        data,
                        adapterPosition,
                        ""
                    )
                }
                QUALITYGRADEDESC -> {
                    binding.tvspItem.text = data
                    receivingData.gradeDesc = data.split(" - ")[0]
                    updateAdapter(
                        data.split(" - ")[0],
                        adapterPosition,
                        ""
                    )
                }
            }
        }

        private fun getQualityDescriptionFromCode(gradeCode1: String?, gradeDesc: String?): String? {
            receivingData.gradeDesc = gradeDesc
            val desc = materialQualityGradeList.filter { it.materialCode.contains(gradeCode1!!) && gradeDesc!!.contains(it.gradeCode,true)  }.map { it.grade }
            return if (desc.isNotEmpty()) gradeDesc.plus(" - ").plus(desc[0]) else gradeDesc
        }

    }
}
