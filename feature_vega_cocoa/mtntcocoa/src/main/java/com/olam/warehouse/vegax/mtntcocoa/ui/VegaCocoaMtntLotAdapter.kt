package com.olam.warehouse.vegax.mtntcocoa.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.mtntcocoa.R
import com.olam.warehouse.vegax.mtntcocoa.databinding.LayoutLotSummaryBinding

class VegaCocoaMtntLotAdapter(
    var data: ArrayList<VegaCocoaDispatchLots>,
    var isEdit: Boolean = false,
    var isClose: Boolean = false,
    var listener: ItemRemoveListener,
    var isThirdPartyMaterial: Boolean = false
) :
    RecyclerView.Adapter<VegaCocoaMtntLotAdapter.LotViewHolder>() {

    class LotViewHolder(bind: LayoutLotSummaryBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
        val viewHolder =
            LayoutLotSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        if (!isThirdPartyMaterial) {
            holder.binding.llVendor.visibility = View.GONE
        } else holder.binding.llVendor.visibility = View.VISIBLE
        holder.binding.tvVendorValue.text =
            data[position].vendor?.plus(" - ").plus(data[position].vendorName)
        if (isClose && data[position].editedWeight.isNullOrBlank()) {
            data[position].editedWeight = data[position].weight
        }
        holder.binding.ivClose.setOnClickListener {
            if (isClose) {
                showConformationDialog(position, holder.binding.ivClose)
            } else {
                listener.itemRemoved(data[position])
            }
        }
        if (isClose) {
            holder.binding.tvWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")
                    ?.plus(data[position].unitOfMeasure)
        } else holder.binding.tvWeightValue.text =
            data[position].editedWeight?.toDouble()?.formatThreeDigits()?.plus(" ")
                ?.plus(data[position].unitOfMeasure)
        holder.binding.tvGradeValue.text = data[position].materialName
        holder.binding.tvEditWeight.visibility =
            if (isEdit) View.VISIBLE else View.GONE
        holder.binding.tvUnit.visibility =
            if (isEdit) View.VISIBLE else View.GONE
        holder.binding.etWeight.visibility = if (isEdit) View.VISIBLE else View.GONE
        holder.binding.etWeight.setText(
            data[position].editedWeight?.toDouble()?.formatThreeDigits()
        )
        holder.binding.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isClose) com.olam.warehouse.presentation.R.drawable.ic_close_black else R.drawable.ic_edit_gray))

        if (isClose) {
            holder.binding.etWeight.onChange {
                if (it.isNotEmpty()) {
                    data[position].editedWeight = it
                    val come: Int? =
                        it.toDouble().compareTo(data[position].weight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        data[position].isLowerWeight = true
                    } else {
                        data[position].isLowerWeight = false
                        holder.binding.etWeight.error =
                            holder.itemView.context.getString(R.string.less_weight_error)
                    }
                }
            }
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
                view.context.getString(R.string.cancel),
                {
                    try {
                        listener.itemRemoved(data[position])
                        data.removeAt(position)
                        notifyDataSetChanged()
                    }catch (e:IndexOutOfBoundsException){e.printStackTrace()}

                },
                { dismiss() })
        }
    }
}
