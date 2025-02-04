package com.olam.warehouse.odquality.ui.params

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import com.olam.warehouse.master.dorigin.model.DOQualityParamsWithQualitative
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.DOQualitySavedResponse
import com.olam.warehouse.presentation.enums.CountryCode
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.*
import kotlinx.android.synthetic.main.item_do_quality_params.view.*

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOQualityParamsAdapter(private val onClick: (List<DOQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<DOQualityParamsAdapter.ParamsViewHolder>() {

    var isProceedBtnClicked: Boolean = false
    private var savedQualityData: DOQualitySavedResponse? = null
    private var value: Double = 0.0
    private var isAdapterCalledFirstTime: Boolean = true

    private var itemViews = mutableListOf<View>()
    private var mQtyParamsListNonModify = arrayListOf<DOQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<DOQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var grossWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var avgBagWeight: String? = ""

    private var mouldWeight: Double = 0.0
    private var sampleWeight: Double = 0.0
    private var sampleWeightGR: Double = 0.0
    private var fmWeight: Double = 0.0
    private var density: Double = 0.0
    private var densityOne: Double = 0.0
    private var densityTwo: Double = 0.0
    private var densityThree: Double = 0.0
    private var densityFour: Double = 0.0
    private var moistureOne: Double = 0.0
    private var moistureTwo: Double = 0.0
    private var moistureThree: Double = 0.0
    private var mould: Double = 0.0
    private var foreignMatter: Double = 0.0
    private var moisture: Double = 0.0
    private var noOfBeans: Double = 0.0
    private var beanCount: Double = 0.0
    private var beanWeight: Double = 0.0
    private var bMoist: Double = 1.0
    private var brokenWeightGR: Double = 0.0
    private var brokenPercentage: Double = 0.0
    private var flatWeightGR: Double = 0.0
    private var flatPercentage: Double = 0.0
    private var clusterWeightGR: Double = 0.0
    private var clusterPercentage: Double = 0.0
    private var otherWasteGR: Double = 0.0
    private var otherPercentage: Double = 0.0
    private var totalWasteGR: Double = 0.0
    private var totalPercentage: Double = 0.0
    private var map = mutableMapOf<String, Int>()

    private val DENSITY1 = "ID_DENSITY1"
    private val DENSITY2 = "ID_DENSITY2"
    private val DENSITY3 = "ID_DENSITY3"
    private val DENSITY4 = "ID_DENSITY4"
    private val SAMPLEWEIGHT = "ID_SAMPLEWEIGHT"
    private val FMWEIGHT = "ID_FMWEIGHT"
    private val MOULDWEIGHT = "ID_MOULDWEIGHT"
    private val MOISTURE1 = "ID_MOISTURE1"
    private val MOISTURE2 = "ID_MOISTURE2"
    private val MOISTURE3 = "ID_MOISTURE3"
    private val BROKENWEIGHT = "ID_BROKENWEIGHT"
    private val CLUSTERWEIGHT = "ID_CLUSTERWEIGHT"
    private val FLATWEIGHT = "ID_FLATWEIGHT"
    private val OTHERWASTE = "ID_OTHERWASTE"
    private val TOTALWASTE = "ID_TOTALWASTE"
    private val BEANS = "ID_BEANS"
    private val BEANWEIGHT = "ID_BEANWEIGHT"
    private val BROKENPERCENT = "ID_BROKENPERCENT"
    private val CLUSTERPERCENT = "ID_CLUSTERPERCENT"
    private val FLATPERCENT = "ID_FLATPERCENT"
    private val OTHERWASTEPERCENT = "ID_OTHERWASTEPERCENT"

    private var countryCode = PreferenceHelper.get(Constants.COUNTRY_CODE, "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_do_quality_params, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyParamsList.size
    }

    private fun getItem(position: Int) = mQtyParamsList[position]

    fun updateFilter(items: List<DOQualityParamsWithQualitative?>) {
        val data1 = items.filter { it?.qualityParameter?.entryObligatory?.isNotEmpty()!! }
        val data2 = items.filter { it?.qualityParameter?.entryObligatory?.isEmpty()!! }
        val data3 = data2.filter { it?.qualityParameter?.doMandatory?.isNotEmpty()!! }
        val data4 = data2.filter { it?.qualityParameter?.doMandatory?.isEmpty()!! }
        mQtyParamsList.clear()
        mQtyParamsList.addAll(data1)
        mQtyParamsList.addAll(data3)
        mQtyParamsList.addAll(data4)
        notifyItemRangeChanged(0, mQtyParamsList.size)
    }

    fun sortAlphabetically(items: List<DOQualityParamsWithQualitative?>, isAscending: Boolean) {
        val data1 = items.sortedBy { it?.qualityParameter?.descrChar }
        mQtyParamsList.clear()
        if(isAscending) mQtyParamsList.addAll(data1)
        else mQtyParamsList.addAll(data1.asReversed())
        notifyItemRangeChanged(0, mQtyParamsList.size)
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        try {
            if (!itemViews.contains(holder.itemView)) {
                itemViews.add(holder.itemView)
            }
            map[mQtyParamsList[position]!!.qualityParameter.nameChar] = position
            holder.bindItems(mQtyParamsList[position], position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addItems(
        qualityParameter: List<DOQualityParamsWithQualitative>,
        tarWeight: String?,
        netWeight: String?,
        grossWeight: String?,
        challanNo: String?,
        avgBagWeight: String?,
        unitsOfMeasure: String?,
        savedQualityData: DOQualitySavedResponse? = null
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.grossWeight = grossWeight
        this.challanNo = challanNo
        this.avgBagWeight = avgBagWeight

        this.savedQualityData = savedQualityData

        qualityParameter.let {
            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
        }

        mQtyParamsList.forEach {
            savedQualityData?.qualityParameters?.forEach { qualityParam ->
                it?.qualityParameter?.nameChar?.let { it1 ->
                    if (it1 == qualityParam.nameChar) {
                        Log.d("qualityAppFormula", it.qualityParameter.appFormula + ".")
                        Log.d("qualityAppFormulaName", it.qualityParameter.nameChar + ".")
                        Log.d("qualityAppFormulaValue", qualityParam.qualityParameterValue + ".")

                        it.qualityParameter.qualityParameterValue =
                            formatQtyParamValue(qualityParam.qualityParameterValue!!)
                    }
                }
            }

            it?.qualityParameter?.nameChar?.let { it1 ->
                if (it1.contains("PESTAR") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    it.qualityParameter.qualityParameterValue =
                        String.format("%.2f", if (tarWeight?.isNotEmpty()!!) tarWeight.toDouble() else 0.0)
                            .replace(",", ".")
                } else if (it1.contains("PESNET") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    var mGrossweight =
                        String.format("%.2f", grossWeight?.toDouble()).replace(",", ".")
                    var mTarWeight = String.format("%.2f", tarWeight?.toDouble()).replace(",", ".")
                    it.qualityParameter.qualityParameterValue =
                        String.format(
                            "%.2f",
                            if (grossWeight?.isNotEmpty()!! && tarWeight?.isNotEmpty()!!) mGrossweight.toDouble()
                                .minus(
                                    mTarWeight.toDouble()
                                ) else 0.0
                        )
                            .replace(",", ".")
                } else if (it1.contains("PESBRT") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    it.qualityParameter.qualityParameterValue =
                        String.format(
                            "%.2f", if (grossWeight?.isNotEmpty()!!) weightConverter(
                                unitsOfMeasure,
                                it.qualityParameter.unitsOfMeasure,
                                grossWeight.toDouble()
                            ) else 0.0
                        ).replace(",", ".")
                } else if (it1.contains("B_NETWEIGHT2") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    val netWt = if (netWeight?.isNotEmpty()!!) netWeight.toDouble() else 0.0
                    it.qualityParameter.qualityParameterValue = String.format("%.2f", (netWt)).replace(",", ".")
                } else if (it1.contains("DO_TRANSACTION_NO") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    it.qualityParameter.qualityParameterValue = challanNo
                } else if (it1.contains("GH_CASHEW_AVG_BAG_WT") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    it.qualityParameter.qualityParameterValue = avgBagWeight
                } else if (it1.contains("LOBM_UDCODE") && !it.qualityParameter.formulaParam?.contains("X")!!) {
                    it.qualitative?.forEach { qualitative ->
                        if (qualitative.descValue?.contains("Accept")!!)
                            it.qualityParameter.qualityParameterValue = qualitative.charValue
                    }
                }
            }
        }

        notifyDataSetChanged()
    }

    private fun formatQtyParamValue(value: String): String {
        if (value.contains(" ")) {
            return value.split(" ")[0].replace(",", "")
        }
        return value.replace(",", "")
    }

    fun removeItems() {
        mQtyParamsList.clear()
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<DOQualityParamsWithQualitative?> {
        return mQtyParamsList
    }

    fun updateMissedPos(missedPos: MutableList<Int>, data: ArrayList<DOQualityParamsWithQualitative?>) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(qualityParameter: DOQualityParamsWithQualitative?, position: Int) {
            try {
                qualityParameter?.qualityParameter?.let { it1 ->
                    val spannable = SpannableStringBuilder(it1.descrChar)
                    when (it1.entryObligatory?.contains("X")!! || it1.doMandatory?.contains("X")!!) {
                        true -> {
                            spannable.insert(spannable.length, "*")
                            spannable.setSpan(
                                ForegroundColorSpan(Color.RED),
                                it1.descrChar?.length!!,
                                it1.descrChar?.length!! + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                        }
                        false -> spannable.insert(spannable.length, "")
                    }

                    when {
                        it1.formulaParam.equals("X") -> {
                            itemView.edQualityValue.isFocusable = false
                            itemView.edQualityValue.isCursorVisible = false
                            itemView.edQualityValue.isEnabled = false
                            itemView.edQualityValue.isClickable = false
                            itemView.spItem.isEnabled = false
                            itemView.spItem.isClickable = false
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

                    if (qualityParameter.qualitative!!.isNotEmpty()) {

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
                        val descValue = mutableListOf<String>()
                        charValue.add(0, itemView.context.getString(R.string.select))
                        descValue.add(0,itemView.context.getString(R.string.select))
                        if (it1.nameChar.equals("LOBM_UDCODE")) {
                            charValue.addAll(qualityParameter.qualitative!!.filter {
                                it.materialCode == it1.materialCode && it.descValue!!.contains("Accept")
                            }.map { data -> data.charValue.toString() })
                            descValue.addAll(qualityParameter.qualitative!!.filter {
                                it.materialCode == it1.materialCode && it.descValue!!.contains("Accept")
                            }.map { data -> data.charValue.toString() })
                            itemView.spItem.isEnabled = false
                        } else {
                            itemView.spItem.isEnabled = true
                            if (countryCode.equals(CountryCode.COLUMBIA.code) && (it1.descrChar.equals("Perfil de Taza (FD)") || it1.descrChar.equals(
                                    "Concepto Taza (FD)"
                                ))
                            ) {
                                charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                    .map { data ->
                                        data.charValue.toString().substringAfterLast(" ", data.charValue.toString())
                                    })
                            } else {
                                charValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                    .map { data -> data.descValue.toString() })
                            }
                            descValue.addAll(qualityParameter.qualitative!!.filter { it.materialCode == it1.materialCode }
                                .map { data -> data.charValue.toString() })
                        }
                        /*val chValue = qualityParameter.qualitative!!.filter { it.materialCode==it1.materialCode }.filter { it.descValue?.equals(it1.qualityParameterValue?:"")==true }
                        val chValueDesc = if(chValue.size>0) chValue[0].descValue else ""
*/

                        val charValueAdapter =
                            ArrayAdapter(itemView.spItem.context, android.R.layout.simple_list_item_1, charValue)
                        itemView.spItem.adapter = charValueAdapter
                        if (it1.nameChar.equals("LOBM_UDCODE")) itemView.spItem.setSelection(1)
                        if (it1.qualityParameterValue?.isNotEmpty()!!) itemView.spItem.setSelection(
                            descValue.indexOf(
                                it1.qualityParameterValue!!
                            )
                        )
                        itemView.spItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                                if (it1.nameChar.equals("LOBM_UDCODE")) {
                                    updateAdapter(
                                        if (pos > 0) charValue[pos] else "",
                                        adapterPosition,
                                        it1.unitsOfMeasure
                                    )
                                } else {
                                    if (countryCode.equals(CountryCode.COLUMBIA.code) && (it1.descrChar.equals("Perfil de Taza (FD)") || it1.descrChar.equals(
                                            "Concepto Taza (FD)"
                                        ))
                                    ) {
                                        var data =
                                            qualityParameter.qualitative!!.filter {
                                                it.charValue.toString()
                                                    .substringAfterLast(" ", it.charValue.toString())
                                                    .equals(charValue[pos])
                                            }
                                        updateAdapter(
                                            if (pos > 0) data[0].charValue else "",
                                            adapterPosition,
                                            it1.unitsOfMeasure
                                        )
                                    } else {
                                        var data =
                                            qualityParameter.qualitative!!.filter { it.descValue == charValue[pos] }
                                        updateAdapter(
                                            if (pos > 0) data[0].charValue else "",
                                            adapterPosition,
                                            it1.unitsOfMeasure
                                        )
                                    }
                                }


                            }
                        }

                    } else {
                        handleScroll(itemView.edQualityValue)
                        if (!it1.numValFm.isNullOrEmpty()) {
                            spannable.insert(
                                spannable.length,
                                "\n(".plus(it1.numValFm?.trim()).plus(" - ").plus(it1.numValTo?.trim()).plus(")")
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(Color.LTGRAY),
                                it1.descrChar?.length!! + 1,
                                spannable.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                            )
                        }

                        itemView.llEdit.visible()
                        itemView.llDropDown.gone()

                        itemView.edQualityValue.setText(it1.qualityParameterValue, TextView.BufferType.EDITABLE)
                        itemView.tvUnit.text = it1.unitsOfMeasure
                        when (it1.nameChar.contains("PESTAR")
                                || it1.nameChar.contains("PESNET")
                                || it1.nameChar.contains("PESBRT")
                                || it1.nameChar.contains("B_NETWEIGHT2")
                                || (it1.nameChar.equals("DO_TRANSACTION_NO")
                                && it1.qualityParameterValue?.isNotEmpty() ?: false)
                                || (it1.nameChar.equals("GH_CASHEW_AVG_BAG_WT")
                                && it1.qualityParameterValue?.isNotEmpty() ?: false)) {
                            true -> {
                                if (it1.nameChar.contains("PESBRT")&&countryCode.equals(CountryCode.HONDURAS.code) &&  (it1.materialCode.endsWith("100000018020")||it1.materialCode.endsWith("100000018031")))
                                    itemView.edQualityValue.isEnabled = true
                                else
                                    itemView.edQualityValue.isEnabled = false
                        }
                            false -> {
                                itemView.edQualityValue.isEnabled = true
                            }

                        }

                        val beforeDec = if (!it1.numberDigits.isNullOrEmpty()) it1.numberDigits.toString().toInt() - it1.numberDecimals.toString().trim().toInt() else 0
                        val afterDec = if (!it1.numberDecimals.isNullOrEmpty()) it1.numberDecimals.toString().trim().toInt() else 0

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

                        Log.d("NameChar", it1.nameChar)

                        if (isAdapterCalledFirstTime) {
                            isAdapterCalledFirstTime = false
                            if (savedQualityData != null) {
                                savedQualityData?.qualityParameters?.forEach { qtyParam ->
                                    value =
                                        if (qtyParam.qualityParameterValue != "" && formatQtyParamValue(qtyParam.qualityParameterValue!!).toDouble() != null) formatQtyParamValue(
                                            qtyParam.qualityParameterValue!!
                                        ).toDouble() else 0.0
                                    if (qtyParam.nameChar == "ID_CR_MOULDY") {
                                        mould = value
                                    } else if (qtyParam.nameChar == "ID_DENSITY") {
                                        density = value
                                    } else if (qtyParam.nameChar == "ID_FOREIGN_MATTER") {
                                        foreignMatter = value
                                    } else if (qtyParam.nameChar == "ID_MOISTURE") {
                                        moisture = value
                                    } else if (qtyParam.nameChar == TOTALWASTE) {
                                        totalWasteGR = value
                                    } else if (qtyParam.nameChar == "B_MOIST") {
                                        bMoist = value
                                    } else if (qtyParam.nameChar == BROKENPERCENT) {
                                        brokenPercentage = value
                                    } else if (qtyParam.nameChar == FLATPERCENT) {
                                        flatPercentage = value
                                    } else if (qtyParam.nameChar == CLUSTERPERCENT) {
                                        clusterPercentage = value
                                    } else if (qtyParam.nameChar == OTHERWASTEPERCENT) {
                                        otherPercentage = value
                                    } else if (qtyParam.nameChar == "WASTE") {
                                        totalPercentage = value
                                    } else if (qtyParam.nameChar == BEANWEIGHT) {
                                        beanWeight = value
                                    } else if (qtyParam.nameChar == "B_BEANCOUNT") {
                                        beanCount = value
                                    } else if (qtyParam.nameChar == BROKENWEIGHT) {
                                        brokenWeightGR = value
                                    } else if (qtyParam.nameChar == FLATWEIGHT) {
                                        flatWeightGR = value
                                    } else if (qtyParam.nameChar == CLUSTERWEIGHT) {
                                        clusterWeightGR = value
                                    } else if (qtyParam.nameChar == OTHERWASTE) {
                                        otherWasteGR = value
                                    } else if (qtyParam.nameChar == BEANS) {
                                        noOfBeans = value
                                    } else if (qtyParam.nameChar == MOULDWEIGHT) {
                                        mouldWeight = value
                                    } else if (qtyParam.nameChar == "B_FFA1") {
//                                            brokenWeightGR = value
                                    } else if (qtyParam.nameChar == SAMPLEWEIGHT) {
                                        if (savedQualityData?.materialNumber!!.endsWith("100000030871")) {
                                            // PEPPER
                                            sampleWeight = value
                                        } else {
                                            // COCOA
                                            sampleWeightGR = value
                                        }
                                    } else if (qtyParam.nameChar == DENSITY1) {
                                        densityOne = value
                                    } else if (qtyParam.nameChar == DENSITY2) {
                                        densityTwo = value
                                    } else if (qtyParam.nameChar == DENSITY3) {
                                        densityThree = value
                                    } else if (qtyParam.nameChar == DENSITY4) {
                                        densityFour = value
                                    } else if (qtyParam.nameChar == FMWEIGHT) {
                                        fmWeight = value
                                    } else if (qtyParam.nameChar == MOISTURE1) {
                                        moistureOne = value
                                    } else if (qtyParam.nameChar == MOISTURE2) {
                                        moistureTwo = value
                                    } else if (qtyParam.nameChar == MOISTURE3) {
                                        moistureThree = value
                                    }
                                }
                            }
                        }

                        if (it1.appFormula != null && it1.appFormula != "" && it1.appFormula!!.isNotEmpty()) {

                            itemView.edQualityValue.isEnabled = false

                            if (it1.nameChar == "ID_CR_MOULDY") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, mould))
                            } else if (it1.nameChar == "ID_DENSITY") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, density))
                            } else if (it1.nameChar == "ID_FOREIGN_MATTER") {
//                                itemView.edQualityValue.setText(String.format("%.2f", foreignMatter))
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, foreignMatter))
                            } else if (it1.nameChar == "ID_MOISTURE") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, moisture))
                            } else if (it1.nameChar == TOTALWASTE) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, totalWasteGR))
                            } else if (it1.nameChar == "B_MOIST") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, bMoist))
                            } else if (it1.nameChar == BROKENPERCENT) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, brokenPercentage))
                            } else if (it1.nameChar == FLATPERCENT) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, flatPercentage))
                            } else if (it1.nameChar == CLUSTERPERCENT) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, clusterPercentage))
                            } else if (it1.nameChar == OTHERWASTEPERCENT) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, otherPercentage))
                            } else if (it1.nameChar == "WASTE") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, totalPercentage))
                            } else if (it1.nameChar == BEANWEIGHT) {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, beanWeight))
                            } else if (it1.nameChar == "B_BEANCOUNT") {
                                itemView.edQualityValue.setText(formatWithDecimals(afterDec, beanCount))
                            }
                        }

                        if (it1.mandatory?.equals(1)!! && isProceedBtnClicked) {
                            if (itemView.edQualityValue.text != null
                                && itemView.edQualityValue?.text?.toString()!!.isNotEmpty()
                                && itemView.edQualityValue?.text?.toString()!!.toDouble() > 0.0) {
                                itemView.llEdit.let {layout ->
                                    ViewCompat.setBackground(
                                        layout,
                                        ContextCompat.getDrawable(
                                            itemView.context,
                                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                        )
                                    )
                                }
                            } else {
                                itemView.llEdit.let {layout ->
                                    ViewCompat.setBackground(
                                        layout,
                                        ContextCompat.getDrawable(
                                            itemView.context,
                                            com.olam.warehouse.presentation.R.drawable.do_custom_edit_text_red
                                        )
                                    )
                                }
                            }
                        } else {
                            itemView.llEdit.let {layout ->
                                ViewCompat.setBackground(
                                    layout,
                                    ContextCompat.getDrawable(
                                        itemView.context,
                                        com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                                    )
                                )
                            }
                        }

                        updateAdapter(itemView.edQualityValue.text?.toString()!!, position, null)

                        itemView.edQualityValue.onChange {
                            if (itemView.edQualityValue.isFocused) {
                                try {
                                    val param = itemView.edQualityValue.text.toString().trim()
                                    val paramValue = if (param != null && param != "" && param.toDoubleOrNull() != null) param.toDouble() else 0.0

                                    if (it1.nameChar == MOULDWEIGHT) {
                                        mouldWeight = paramValue
                                        if (isAutoCalculatedField("ID_CR_MOULDY")) {
                                            mould = calculateMould()
                                            setText("ID_CR_MOULDY", mould)
                                        }
                                    } else if (it1.nameChar == SAMPLEWEIGHT) {
                                        if(it1.materialCode.endsWith("100000030871")) {
                                            sampleWeight = paramValue
                                            if (isAutoCalculatedField("ID_FOREIGN_MATTER")) {
                                                foreignMatter = calculateFM()
                                                setText("ID_FOREIGN_MATTER", foreignMatter)
                                            }
                                            if (isAutoCalculatedField("ID_CR_MOULDY")) {
                                                mould = calculateMould()
                                                setText("ID_CR_MOULDY", mould)
                                            }
                                        } else {
                                            sampleWeightGR = paramValue

                                            if (isAutoCalculatedField(BEANWEIGHT)) {
                                                beanWeight = calculateBeanWeight()
                                                setText(BEANWEIGHT, beanWeight)
                                            }

                                            if (isAutoCalculatedField(BROKENPERCENT)) {
                                                brokenPercentage = calculateBrokenPercentage()
                                                setText(BROKENPERCENT, brokenPercentage)
                                            }

                                            if (isAutoCalculatedField(FLATPERCENT)) {
                                                flatPercentage = calculateFlatPercentage()
                                                setText(FLATPERCENT, flatPercentage)
                                            }

                                            if (isAutoCalculatedField(CLUSTERPERCENT)) {
                                                clusterPercentage = calculateClusterPercentage()
                                                setText(CLUSTERPERCENT, clusterPercentage)
                                            }

                                            if (isAutoCalculatedField(OTHERWASTEPERCENT)) {
                                                otherPercentage = calculateOtherPercentage()
                                                setText(OTHERWASTEPERCENT, otherPercentage)
                                            }

                                            if (isAutoCalculatedField("WASTE")) {
                                                totalPercentage = calculateTotalPercentage()
                                                setText("WASTE", totalPercentage)
                                            }

                                            if (isAutoCalculatedField("B_BEANCOUNT")) {
                                                beanCount = calculateBeanCount()
                                                setText("B_BEANCOUNT", beanCount)
                                            }
                                        }
                                    } else if (it1.nameChar == FMWEIGHT) {
                                        fmWeight = paramValue
                                        if (isAutoCalculatedField("ID_FOREIGN_MATTER")) {
                                            foreignMatter = calculateFM()
                                            setText("ID_FOREIGN_MATTER", foreignMatter)
                                        }
                                    } else if (it1.nameChar == DENSITY1) {
                                        densityOne = paramValue
                                        if (isAutoCalculatedField("ID_DENSITY")) {
                                            density = calculateDensity()
                                            setText("ID_DENSITY", density)
                                        }
                                    } else if (it1.nameChar == DENSITY2) {
                                        densityTwo = paramValue
                                        if (isAutoCalculatedField("ID_DENSITY")) {
                                            density = calculateDensity()
                                            setText("ID_DENSITY", density)
                                        }
                                    } else if (it1.nameChar == DENSITY3) {
                                        densityThree = paramValue
                                        if (isAutoCalculatedField("ID_DENSITY")) {
                                            density = calculateDensity()
                                            setText("ID_DENSITY", density)
                                        }
                                    } else if (it1.nameChar == DENSITY4) {
                                        densityFour = paramValue
                                        if (isAutoCalculatedField("ID_DENSITY")) {
                                            density = calculateDensity()
                                            setText("ID_DENSITY", density)
                                        }
                                    } else if (it1.nameChar == MOISTURE1) {
                                        moistureOne = paramValue
                                        if (isAutoCalculatedField("ID_MOISTURE")) {
                                            moisture = calculateMoisture()
                                            setText("ID_MOISTURE", moisture)
                                        }
                                    } else if (it1.nameChar == MOISTURE2) {
                                        moistureTwo = paramValue
                                        if (isAutoCalculatedField("ID_MOISTURE")) {
                                            moisture = calculateMoisture()
                                            setText("ID_MOISTURE", moisture)
                                        }
                                    } else if (it1.nameChar == MOISTURE3) {
                                        moistureThree = paramValue
                                        if (isAutoCalculatedField("ID_MOISTURE")) {
                                            moisture = calculateMoisture()
                                            setText("ID_MOISTURE", moisture)
                                        }
                                    } else if (it1.nameChar == BEANS) {
                                        noOfBeans = paramValue
                                        if (isAutoCalculatedField("B_BEANCOUNT")) {
                                            beanCount = calculateBeanCount()
                                            setText("B_BEANCOUNT", beanCount)
                                        }
                                    } else if (it1.nameChar == "B_MOIST") {
                                        bMoist = paramValue
                                        if (isAutoCalculatedField("B_BEANCOUNT")) {
                                            beanCount = calculateBeanCount()
                                            setText("B_BEANCOUNT", beanCount)
                                        }
                                    } else if (it1.nameChar == BROKENWEIGHT) {
                                        brokenWeightGR = paramValue

                                        if (isAutoCalculatedField(BROKENPERCENT)) {
                                            brokenPercentage = calculateBrokenPercentage()
                                            setText(BROKENPERCENT, brokenPercentage)
                                        }

                                        if (isAutoCalculatedField(BEANWEIGHT)) {
                                            beanWeight = calculateBeanWeight()
                                            setText(BEANWEIGHT, beanWeight)
                                        }

                                        if (isAutoCalculatedField("B_BEANCOUNT")) {
                                            beanCount = calculateBeanCount()
                                            setText("B_BEANCOUNT", beanCount)
                                        }
                                    } else if (it1.nameChar == CLUSTERWEIGHT) {
                                        clusterWeightGR = paramValue

                                        if (isAutoCalculatedField(CLUSTERPERCENT)) {
                                            clusterPercentage = calculateClusterPercentage()
                                            setText(CLUSTERPERCENT, clusterPercentage)
                                        }

                                        if (isAutoCalculatedField(TOTALWASTE)) {
                                            totalWasteGR = calculateTotalWaste()
                                            setText(TOTALWASTE, totalWasteGR)
                                        }

                                        if (isAutoCalculatedField("WASTE")) {
                                            totalPercentage = calculateTotalPercentage()
                                            setText("WASTE", totalPercentage)
                                        }

                                        if (isAutoCalculatedField(BEANWEIGHT)) {
                                            beanWeight = calculateBeanWeight()
                                            setText(BEANWEIGHT, beanWeight)
                                        }

                                        if (isAutoCalculatedField("B_BEANCOUNT")) {
                                            beanCount = calculateBeanCount()
                                            setText("B_BEANCOUNT", beanCount)
                                        }
                                    } else if (it1.nameChar == FLATWEIGHT) {
                                        flatWeightGR = paramValue

                                        if (isAutoCalculatedField(FLATPERCENT)) {
                                            flatPercentage = calculateFlatPercentage()
                                            setText(FLATPERCENT, flatPercentage)
                                        }

                                        if (isAutoCalculatedField(TOTALWASTE)) {
                                            totalWasteGR = calculateTotalWaste()
                                            setText(TOTALWASTE, totalWasteGR)
                                        }

                                        if (isAutoCalculatedField("WASTE")) {
                                            totalPercentage = calculateTotalPercentage()
                                            setText("WASTE", totalPercentage)
                                        }

                                        if (isAutoCalculatedField(BEANWEIGHT)) {
                                            beanWeight = calculateBeanWeight()
                                            setText(BEANWEIGHT, beanWeight)
                                        }

                                        if (isAutoCalculatedField(BEANS)) {
                                            beanCount = calculateBeanCount()
                                            setText(BEANS, beanCount)
                                        }
                                    } else if (it1.nameChar == OTHERWASTE) {
                                        otherWasteGR = paramValue

                                        if (isAutoCalculatedField(OTHERWASTEPERCENT)) {
                                            otherPercentage = calculateOtherPercentage()
                                            setText(OTHERWASTEPERCENT, otherPercentage)
                                        }

                                        if (isAutoCalculatedField(TOTALWASTE)) {
                                            totalWasteGR = calculateTotalWaste()
                                            setText(TOTALWASTE, totalWasteGR)
                                        }

                                        if (isAutoCalculatedField("WASTE")) {
                                            totalPercentage = calculateTotalPercentage()
                                            setText("WASTE", totalPercentage)
                                        }

                                        if (isAutoCalculatedField(BEANWEIGHT)) {
                                            beanWeight = calculateBeanWeight()
                                            setText(BEANWEIGHT, beanWeight)
                                        }

                                        if (isAutoCalculatedField("B_BEANCOUNT")) {
                                            beanCount = calculateBeanCount()
                                            setText("B_BEANCOUNT", beanCount)
                                        }
                                    }else if (it1.nameChar =="HNPESBRT") {
                                        updateNetWeight(it,position)
                                        //setText("HNPESNET", calculateNeto())
                                    }

//                                    updateAdapter(paramValue.toString(), adapterPosition, null)

                                    if (it.isEmpty()) {
                                        qualityParameter.qualityParameter.qualityParameterValue = ""
                                        return@onChange
                                    }

                                    val editVal: String?
                                    val dot = it.get(0).toString()
                                    editVal = if (dot == ".") {
                                        if (it.length == 1) "0.0" else "0".plus(it)
                                    } else it
                                    if (!it1.numValTo.isNullOrEmpty()) {
                                        val numFrom = it1.numValFm.toString().toDouble()
                                        val numTo = it1.numValTo.toString().toDouble()
                                        if (numTo != 0.0) {
                                            if (validateRangeParamValue(editVal, numFrom, numTo)) {
                                                if (afterDec != 0 || beforeDec != 0) {
                                                    if (validateParamValue(editVal, beforeDec, afterDec)) {
                                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                                    } else {
                                                        itemView.edQualityValue.setError(
                                                            itemView.context.getString(R.string.value_out_of_range),
                                                            null
                                                        )
                                                    }
                                                } else {
                                                    updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                                }
                                            } else {
                                                itemView.edQualityValue.setError(
                                                    itemView.context.getString(R.string.value_out_of_range),
                                                    null
                                                )
                                            }
                                        } else {
                                            if (validateRangeParamValue(editVal, numFrom, numTo)) {
                                                if (afterDec != 0 || beforeDec != 0) {
                                                    if (validateParamValue(editVal, beforeDec, afterDec)) {
                                                        updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                                    } else {
                                                        itemView.edQualityValue.setError(
                                                            itemView.context.getString(R.string.value_out_of_range),
                                                            null
                                                        )
                                                    }
                                                } else {
                                                    updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                                }
                                            } else {
                                                itemView.edQualityValue.setError(
                                                    itemView.context.getString(R.string.value_out_of_range),
                                                    null
                                                )
                                            }
                                        }
                                    } else {
                                        if (afterDec != 0 || beforeDec != 0) {
                                            if (validateParamValue(editVal, beforeDec, afterDec)) {
                                                updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                            } else {
                                                itemView.edQualityValue.setError(
                                                    itemView.context.getString(R.string.value_out_of_range),
                                                    null
                                                )
                                            }
                                        } else {
                                            updateAdapter(editVal, adapterPosition, it1.unitsOfMeasure)
                                        }
                                    }
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    itemView.tvQualityName.text = spannable
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun validateParamValue(id: String?, beforeDec: Int?, afterDec: Int?): Boolean {
            return when {
                id?.isNotEmpty()!! -> {
                    var values = listOf<String?>()
                    values = when (id.contains(".")) {
                        true -> id.split(".").map { it.trim() }
                        else -> values + id
                    }

                    return when {
                        values[0]?.length!! > beforeDec!! || (values.size > 1 && values[1]!!.length > afterDec!!) -> false
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
            editText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, event ->
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

    private fun formatWithDecimals(decimal: Int, number: Double) : String {
        if (decimal != 0) {
//            return "%.${decimal}f".format(number)
            val value = number.formatNDigits(decimal)
            return value
        }
        return number.toInt().toString()
    }

    private fun calculateBeanWeight(): Double {
        return sampleWeightGR - totalWasteGR
    }

    private fun calculateTotalWaste() : Double {
        return clusterWeightGR + flatWeightGR + otherWasteGR
    }

    private fun calculateBeanCount(): Double {
        var beanCount = 0.0
        if (beanWeight != 0.0) {
            var noOfBeansDivByBeanWeight = noOfBeans / beanWeight
            var oneMinusSevenDivByHundred = 0.93
            var bMoistDivByHundred = bMoist / 100
            var oneMinusBMoistDivByHundred = 1 - bMoistDivByHundred
            beanCount = ((noOfBeansDivByBeanWeight * oneMinusSevenDivByHundred) / oneMinusBMoistDivByHundred) * 100
        }
        return beanCount
    }
    private fun isAutoCalculatedField(nameChar: String) : Boolean {
        mQtyParamsList.forEach {
            if (it?.qualityParameter?.nameChar == nameChar && it.qualityParameter.appFormula != null && it.qualityParameter.appFormula != "") {
                return true
            }
        }
        return false
    }

    private fun setText(
        nameChar: String,
        value: Double
    ) {
        var pos = -1
        Log.i("itemViewsSize", itemViews.size.toString())

        mQtyParamsList.forEach {
            pos += 1
            if (it?.qualityParameter?.nameChar == nameChar) {
                if (pos != -1 && itemViews.size > pos) {
                    val afterDec =
                        if (!it.qualityParameter.numberDecimals.isNullOrEmpty()) it.qualityParameter.numberDecimals.toString()
                            .trim().toInt() else 0
                    val formattedValue: String = formatWithDecimals(afterDec, value)
                    itemViews[pos].edQualityValue.setText(formattedValue)
                    itemViews[pos].llEdit.let { layout ->
                        ViewCompat.setBackground(
                            layout,
                            ContextCompat.getDrawable(
                                itemViews[pos].context,
                                com.olam.warehouse.presentation.R.drawable.do_custom_edit_text
                            )
                        )
                    }
                    updateAdapter(formattedValue, pos, null)
                    return
                }
            }
        }
    }

    fun updateAdapter(editValue: String, position: Int, unit: String?) {
        mQtyParamsList[position]?.qualityParameter?.qualityParameterValue = editValue
        onClick(mQtyParamsList)
    }

    private fun calculateBrokenPercentage() : Double {
        var brokenPercentage = 0.0
        brokenPercentage = if (sampleWeightGR != 0.0) ((brokenWeightGR / sampleWeightGR) * 100) else 0.0
        return brokenPercentage
    }

    private fun calculateFlatPercentage() : Double {
        var flatPercentage = 0.0
        flatPercentage = if (sampleWeightGR != 0.0) ((flatWeightGR / sampleWeightGR) * 100) else 0.0
        return flatPercentage
    }

    private fun calculateClusterPercentage() : Double {
        var clusterPercentage = 0.0
        clusterPercentage = if (sampleWeightGR != 0.0) ((clusterWeightGR / sampleWeightGR) * 100) else 0.0
        return clusterPercentage
    }

    private fun calculateOtherPercentage() : Double {
        var otherPercentage = 0.0
        otherPercentage = if (sampleWeightGR != 0.0) ((otherWasteGR / sampleWeightGR) * 100) else 0.0
        return otherPercentage
    }

    private fun calculateTotalPercentage() : Double {
        var totalPercentage = 0.0
        totalPercentage = if (sampleWeightGR != 0.0) ((totalWasteGR / sampleWeightGR) * 100) else 0.0
        return totalPercentage
    }

    private fun calculateFM() : Double {
        var fm = 0.0
        fm = if (!(fmWeight / sampleWeight * 100).isNaN()) fmWeight / sampleWeight * 100 else 0.0
        return fm
    }

    private fun calculateMould() : Double {
        var mould = 0.0
        if (sampleWeight != 0.0) {
            mould = if (!(mouldWeight / sampleWeight * 100).isNaN()) mouldWeight / sampleWeight * 100 else 0.0
        }
        return mould
    }

    private fun calculateDensity() : Double {
        var density = 0.0
        if (densityFour == 0.0) {
            density = (densityOne + densityTwo + densityThree) / 3
        } else {
            density = (densityOne + densityTwo + densityThree + densityFour) / 4
        }
        return density
    }

    private fun calculateMoisture() : Double {
        var moisture = 0.0
        if (moistureThree == 0.0) {
            moisture = (moistureOne + moistureTwo) / 2
        } else {
            moisture = (moistureOne + moistureTwo + moistureThree) / 3
        }
        return moisture
    }

     private fun updateNetWeight(grossWeight: String?,position: Int){
        var netPosition=0
        mQtyParamsList.forEachIndexed { index, it -> if(it?.qualityParameter?.nameChar?.contains("PESNET")==true)netPosition=index }
        var mGrossweight =String.format("%.2f",grossWeight?.toDouble()).replace(",", ".")
        var mTarWeight =String.format("%.2f",tarWeight?.toDouble()).replace(",", ".")
        var netWeight =String.format(
            "%.2f",
            if (grossWeight?.isNotEmpty()!! && tarWeight?.isNotEmpty()!!) mGrossweight.toDouble()
                .minus(
                    mTarWeight.toDouble()
                ) else 0.0
        )
            .replace(",", ".")
        mQtyParamsList.find{ it?.qualityParameter?.nameChar?.contains("PESNET")==true}
            ?.apply { qualityParameter.qualityParameterValue =netWeight }
        onClick(mQtyParamsList)
        notifyItemChanged(netPosition)
    }
}
