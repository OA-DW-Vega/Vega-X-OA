package com.olam.warehouse.vegax.grnnicaragua.ui.spot

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.model.AdvanceLineItemDetails
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceLineItemGrn
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.grnnicaragua.R
import com.olam.warehouse.vegax.grnnicaragua.databinding.ItemVegaNicaraguaGrnAdvanceLineBinding
import com.olam.warehouse.vegax.grnnicaragua.utils.covertToDouble
import java.util.*
import kotlin.collections.ArrayList


class VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor (var ctx: Context, var list: List<AdvanceLineItemDetails>)
    : RecyclerView.Adapter<VegaNicaraguaGrnPriceCalculationAdvanceLineAdaptor.VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder>() {

    private var listener: CallBack? = null
    var selectedList: ArrayList<AdvanceLineItemDetails>? = ArrayList<AdvanceLineItemDetails>()

    interface CallBack {
        fun updatePriceDetails(selectedList: ArrayList<AdvanceLineItemDetails>)
        fun removeItem(model: AdvanceLineItemDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder {
        return VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder( ItemVegaNicaraguaGrnAdvanceLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setAdapterListener(listener: CallBack) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder, position: Int) {


        val model = list[position]

        holder.binding.AdvanceNum.text = model.documentNumber
        holder.binding.tvAdvanceDate.text = model.documentDate
        holder.binding.tvAdvanceAmount.text = formatString(covertToDouble(model.amount!!))
        holder.binding.EtAdvanceKnockAmount.setText(model.advanceKnockAmount)
        holder.binding.EtInterest.setText(model.interestAmount)
        holder.binding.EtComission.setText(model.commissionAmount)
        holder.binding.EtLegalExpense.setText(model.legalExpenseAmount)
        holder.binding.EtCurrecncyDevaluation.setText(model.currencyDevaluationAmount)
        if (model.advanceKnockAmount?.isNotEmpty() == true || model.interestAmount?.isNotEmpty() == true || model.commissionAmount?.isNotEmpty() == true || model.legalExpenseAmount?.isNotEmpty() == true) {
            holder.binding.cbAdvanceInfo.isChecked = true
            holder.binding.EtAdvanceKnockAmount.isEnabled = true
            holder.binding.EtInterest.isEnabled = true
            holder.binding.EtComission.isEnabled = true
            holder.binding.EtLegalExpense.isEnabled = true
            holder.binding.EtCurrecncyDevaluationLayout.isEnabled = true
            holder.binding.amount.text = ctx.getString(R.string.c_doller) + " " + model.totalAdvanceKnockAmount
            if (!selectedList!!.contains(model)) {
                selectedList?.add(model)
            }
            updatePrice(holder, model)
        }

        updateMandatory(holder)

        holder.binding.cbAdvanceInfo.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                holder.binding.EtAdvanceKnockAmount.isEnabled = true
                holder.binding.EtInterest.isEnabled = true
                holder.binding.EtComission.isEnabled = true
                holder.binding.EtLegalExpense.isEnabled = true
                holder.binding.EtCurrecncyDevaluation.isEnabled = true
                if (!selectedList!!.contains(model)) {
                    selectedList?.add(model)
                }
                updatePrice(holder, model)
            } else {
                holder.binding.EtAdvanceKnockAmount.isEnabled = false
                holder.binding.EtInterest.isEnabled = false
                holder.binding.EtComission.isEnabled = false
                holder.binding.EtLegalExpense.isEnabled = false
                holder.binding.EtCurrecncyDevaluation.isEnabled = false
                holder.binding.EtAdvanceKnockAmount.setText("")
                holder.binding.EtInterest.setText("")
                holder.binding.EtComission.setText("")
                holder.binding.EtLegalExpense.setText("")
                holder.binding.EtCurrecncyDevaluation.setText("")
                if (selectedList!!.contains(model)) {
                    selectedList?.remove(model)
                }
                model.advanceKnockAmount = ""
                model.legalExpenseAmount = ""
                model.commissionAmount = ""
                model.interestAmount = ""
                model.totalAdvanceKnockAmount = ""
                model.currencyDevaluationAmount = ""
                listener?.removeItem(model)
                updatePrice(holder, model)
            }
        })

        holder.binding.EtAdvanceKnockAmount.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                if (covertToDouble(model.amount!!) >= covertToDouble(s)) {
                    model.advanceKnockAmount = s
                    updatePrice(holder, model)
                } else {
                    holder.binding.EtAdvanceKnockAmount.error = ctx.getString(R.string.valid_amount_error_info)
                }
            }

        }

        holder.binding.EtLegalExpense.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                if (covertToDouble(model.amount!!) >= covertToDouble(s)) {
                    model.legalExpenseAmount = s
                    updatePrice(holder, model)
                } else {
                    holder.binding.EtLegalExpense.error = ctx.getString(R.string.valid_amount_error_info)
                }
            }

        }
        holder.binding.EtCurrecncyDevaluation.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                if (covertToDouble(model.amount!!) >= covertToDouble(s)) {
                    model.currencyDevaluationAmount = s
                    updatePrice(holder, model)
                } else {
                    holder.binding.EtCurrecncyDevaluation.error = ctx.getString(R.string.valid_amount_error_info)
                }
            }

        }
        holder.binding.EtInterest.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                if (covertToDouble(model.amount!!) >= covertToDouble(s)) {
                    model.interestAmount = s
                    updatePrice(holder, model)
                } else {
                    holder.binding.EtInterest.error =  ctx.getString(R.string.valid_amount_error_info)
                }
            }

        }

        holder.binding.EtComission.onChange { s ->

            if (covertToDouble(s) >= 0.0) {
                if (covertToDouble(model.amount!!) >= covertToDouble(s)) {
                    model.commissionAmount = s
                    updatePrice(holder, model)
                } else {
                    holder.binding.EtComission.error = ctx.getString(R.string.valid_amount_error_info)
                }
            }

        }
    }

    fun updatePrice(holder: VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder, model: AdvanceLineItemDetails) {

        model.totalAdvanceKnockAmount = String.format(Locale.ENGLISH,
            "%.2f",
            covertToDouble(model.advanceKnockAmount!!) + covertToDouble(model.interestAmount!!) + covertToDouble(model.commissionAmount!!) + covertToDouble(
                model.legalExpenseAmount!!
            ) + covertToDouble(model.currencyDevaluationAmount!!)
        )

        selectedList!!.forEachIndexed { index, video ->
            video.takeIf { it.documentNumber == model.documentNumber }?.let {
                selectedList!![index] = model
            }
        }

        holder.binding.amount.text = ctx.getString(R.string.c_doller) + " " + model.totalAdvanceKnockAmount

        listener!!.updatePriceDetails(selectedList!!)

    }

    inner class VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder (bind:ItemVegaNicaraguaGrnAdvanceLineBinding) : RecyclerView.ViewHolder(bind.root){
        val binding = bind


    }

    private fun updateMandatory(holder: VegaNicaraguaInvoicePriceCalculationAdvanceLineViewHolder) {
        holder.binding.EtAdvanceKnockAmountHeader.text =
            with(UIUtils) { with(ctx.resources.getString(R.string.knock_off_amount)) { mandatoryStars() } }
        holder.binding.EtInterestHeader.text =
            with(UIUtils) { with(ctx.resources.getString(R.string.interest)) { mandatoryStars() } }
        holder.binding.EtComissionHeader.text =
            with(UIUtils) { with(ctx.resources.getString(R.string.commission)) { mandatoryStars() } }
    }

    fun updateItem(items: List<VegaNicaraguaAdvanceLineItemGrn>?) {
        items?.let {
            it.forEach { it1 ->
                list.forEach { it2 ->
                    if (it1.documentNumber.equals(it2.documentNumber)) {
                        it2.advanceKnockAmount = it1.advanceKnockAmount
                        it2.totalAdvanceKnockAmount = it1.totalAdvanceKnockAmount
                        it2.interestAmount = it1.interestAmount
                        it2.itemNum = it1.itemNum
                        it2.commissionAmount = it1.commissionAmount
                        it2.legalExpenseAmount = it1.legalExpenseAmount
                        it2.currencyDevaluationAmount = it1.currencyDevaluationAmount
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun formatString(str: Any): String {
        val strFormat = String.format(Locale.ENGLISH, "%.2f", str)
        return ctx.getString(R.string.c_doller) + " " + strFormat.format(this).replace(",", "")
    }

}
