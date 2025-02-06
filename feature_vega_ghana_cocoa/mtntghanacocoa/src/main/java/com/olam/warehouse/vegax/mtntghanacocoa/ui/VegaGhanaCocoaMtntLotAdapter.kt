package com.olam.warehouse.vegax.mtntghanacocoa.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.mtntghanacocoa.R
import com.olam.warehouse.vegax.mtntghanacocoa.databinding.ItemGhanaCocoaLotSummaryBinding
import com.olam.warehouse.vegax.mtntghanacocoa.utils.BAG
import com.olam.warehouse.vegax.mtntghanacocoa.utils.MATERIAL_CODE

class VegaGhanaCocoaMtntLotAdapter(
    var data: ArrayList<VegaGhanaCocoaDispatchLots>,
    var uomDetails: ArrayList<VegaUomDetails>,
    var isEdit: Boolean = false,
    var isClose: Boolean = false,
    var listenerCocoa: GhanaCocoaMTNTItemRemoveListener
) :
    RecyclerView.Adapter<VegaGhanaCocoaMtntLotAdapter.LotViewHolder>() {

    class LotViewHolder(bind: ItemGhanaCocoaLotSummaryBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
        val viewHolder =
            ItemGhanaCocoaLotSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return LotViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: LotViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.tvLotId.text = data[position].batchNumber
        holder.binding.tvStLocation.text = data[position].storageLocationCode
        holder.binding.ivClose.setOnClickListener {
            showConformationDialog(position, holder.binding.ivClose)
        }

        holder.binding.tvWeightValue.text =
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                ?.plus(data[position].unitOfMeasure)

        var uom =
            ((uomDetails.filter { ((MATERIAL_CODE.plus(it.materialCode)).equals(data[position].materialCode)) }).filter {
                it.fromUom.equals(BAG)
            }).single()

        if (uomDetails.size > 0) {
            holder.binding.tvWeightValue.text = (data[position].weight?.toDouble()
                ?.div(
                    (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)) ?: 1.0
                ))?.formatThreeDigits()
                .plus(" Bags")
        }


        holder.binding.tvGradeValue.text = data[position].materialName

        val editWeight =
            if (data[position].editedWeight.isNullOrEmpty())
                "0"
            else
                (data[position].editedWeight?.toDouble()?.div(
                        (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)) ?: 1.0
                ))?.formatThreeDigits()

        holder.binding.etWeight.setText(editWeight)

        holder.binding.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isClose) R.drawable.ic_ghana_close_black else R.drawable.ic_ghana_edit_gray))

        holder.binding.etWeight.onChange {
            if (it.isNotEmpty()) {
                data[position].editedWeight = it
                var weightTotal = (data[position].weight?.toDouble()?.div(
                    (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)) ?: 1.0
                ))?.formatThreeDigits()
                val come: Int? = it.toDouble().compareTo(weightTotal?.toDouble() ?: 0.0)
                if (come ?: 0 <= 0) {
                    data[position].isLowerWeight = true
                } else {
                    data[position].isLowerWeight = false
                    holder.binding.etWeight.error =
                        holder.itemView.context.getString(R.string.number_of_bags)
                }
            }
        }
        holder.binding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isChecked = isChecked
            if (isChecked) {
                data[position].editedWeight = data[position].weight?.toDouble().toString()
                holder.binding.etWeight.setText(
                    (data[position].weight?.toDouble()?.div(
                        (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!))
                            ?: 1.0
                    ))?.formatThreeDigits()
                )
            } else {
                data[position].editedWeight = "0"
                holder.binding.etWeight.setText(data[position].editedWeight)
            }
        }

        holder.binding.cbEndLot.isChecked = data[position].isEndLot ?: false
        holder.binding.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isEndLot = isChecked
        }
    }

    fun addLotData(lot: VegaGhanaCocoaDispatchLots) {
        data.add(lot)
        notifyDataSetChanged()
    }

    fun removeData() {
        data.clear()
        notifyDataSetChanged()
    }

    fun addAllLots(lots: List<VegaGhanaCocoaDispatchLots>) {
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
                view.context.getString(R.string.cancel),
                {
                    listenerCocoa.itemRemoved(data[position])
                    data.removeAt(position)
                    notifyItemRemoved(position)
                },
                { dismiss() })
        }
    }
}
