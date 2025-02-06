package com.olam.warehouse.vegax.mtntcoffee.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.mtntcoffee.R
import com.olam.warehouse.vegax.mtntcoffee.databinding.ItemCoffeeLotSummaryBinding

class VegaCoffeeMtntLotAdapter(
    var data: ArrayList<VegaCocoaDispatchLots>,
    var isEdit: Boolean = false,
    var isClose: Boolean = false,
    var listener: ItemRemoveListener
) :
    RecyclerView.Adapter<VegaCoffeeMtntLotAdapter.LotViewHolder>() {

    class LotViewHolder(bind: ItemCoffeeLotSummaryBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
        val viewHolder =
            ItemCoffeeLotSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LotViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: LotViewHolder, position: Int) {
        holder.binding.tvLotId.text = data[position].batchNumber
        holder.binding.tvStLocation.text = data[position].storageLocationCode
        holder.binding.ivClose.setOnClickListener {
            showConformationDialog(position, holder.binding.ivClose)
        }
        holder.binding.tvUnit.text = data[position].unitOfMeasure
        holder.binding.tvWeightValue.text =
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                ?.plus(data[position].unitOfMeasure)
        holder.binding.tvGradeValue.text = data[position].materialName
        val editWeight =
            if (data[position].editedWeight.isNullOrEmpty()) "0.0" else data[position].editedWeight?.toDouble()
                ?.formatThreeDigits()
        holder.binding.etWeight.setText(editWeight)
        holder.binding.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isClose) R.drawable.ic_coffee_close_black else R.drawable.ic_coffee_edit_gray))
        holder.binding.etWeight.onChange {
            if (it.isNotEmpty()) {
                data[position].editedWeight = it
                val come: Int? = it.toDouble().compareTo(data[position].weight?.toDouble() ?: 0.0)
                if (come ?: 0 <= 0) {
                    data[position].isLowerWeight = true
                } else {
                    data[position].isLowerWeight = false
                    holder.binding.etWeight.error =
                        holder.itemView.context.getString(R.string.less_weight_error)
                }
            }
        }
        holder.binding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isChecked = isChecked
            if (isChecked) {
                data[position].editedWeight = data[position].weight?.toDouble()?.formatThreeDigits()

            } else {
                data[position].editedWeight = "0.0"
            }
            holder.binding.etWeight.setText(data[position].editedWeight)
        }
        holder.binding.cbEndLot.isChecked = data[position].isEndLot ?: false
        holder.binding.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isEndLot = isChecked
        }
    }

    fun addLotData(lot: VegaCocoaDispatchLots) {
        data.add(lot)
        notifyDataSetChanged()
    }

    fun removeData() {
        data.clear()
        notifyDataSetChanged()
    }

    fun addAllLots(lots: List<VegaCocoaDispatchLots>) {
        data.addAll(lots)
        notifyDataSetChanged()
    }

    fun getSize() = data.size > 0

    fun updateEditState(isEdit: Boolean) {
        this.isEdit = isEdit
    }

    private fun showConformationDialog(position: Int, view: View) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(
                this,
                view.context.getString(R.string.proceed),
                view.context.getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    listener.itemRemoved(data[position])
                    data.removeAt(position)
                    notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }
}
