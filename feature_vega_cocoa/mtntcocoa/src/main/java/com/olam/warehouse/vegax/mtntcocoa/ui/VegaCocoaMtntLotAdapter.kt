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
import kotlinx.android.synthetic.main.layout_lot_summary.view.*

class VegaCocoaMtntLotAdapter(
    var data: ArrayList<VegaCocoaDispatchLots>,
    var isEdit: Boolean = false,
    var isClose: Boolean = false,
    var listener: ItemRemoveListener,
    var isThirdPartyMaterial: Boolean = false
) :
    RecyclerView.Adapter<VegaCocoaMtntLotAdapter.LotViewHolder>() {

    class LotViewHolder(bind: LayoutLotSummaryBinding) : RecyclerView.ViewHolder(bind.root)

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
        holder.itemView.tvLotId.text = data[position].batchNumber
        holder.itemView.tvStLocation.text = data[position].storageLocationCode

        if (!isThirdPartyMaterial) {
            holder.itemView.llVendor.visibility = View.GONE
        } else holder.itemView.llVendor.visibility = View.VISIBLE
        holder.itemView.tvVendorValue.text = data[position].vendor?.plus(" - ").plus(data[position].vendorName)
        if (isClose && data[position].editedWeight.isNullOrBlank()) {
            data[position].editedWeight = data[position].weight
        }
        holder.itemView.ivClose.setOnClickListener {
            if (isClose) {
                showConformationDialog(position, holder.itemView.ivClose)
            } else {
                listener.itemRemoved(data[position])
            }
        }
        if (isClose) {
            holder.itemView.tvWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(data[position].unitOfMeasure)
        } else holder.itemView.tvWeightValue.text =
            data[position].editedWeight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(data[position].unitOfMeasure)
        holder.itemView.tvGradeValue.text = data[position].materialName
        holder.itemView.tvEditWeight.visibility =
            if (isEdit) View.VISIBLE else View.GONE
        holder.itemView.tvUnit.visibility =
            if (isEdit) View.VISIBLE else View.GONE
        holder.itemView.etWeight.visibility = if (isEdit) View.VISIBLE else View.GONE
        holder.itemView.etWeight.setText(data[position].editedWeight?.toDouble()?.formatThreeDigits())
        holder.itemView.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isClose) R.drawable.ic_close_black else R.drawable.ic_edit_gray))

        if (isClose) {
            holder.itemView.etWeight.onChange {
                if (it.isNotEmpty()) {
                    data[position].editedWeight = it
                    val come: Int? = it.toDouble().compareTo(data[position].weight?.toDouble() ?: 0.0)
                    if (come ?: 0 <= 0) {
                        data[position].isLowerWeight = true
                    } else {
                        data[position].isLowerWeight = false
                        holder.itemView.etWeight.error = holder.itemView.context.getString(R.string.less_weight_error)
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
                    listener.itemRemoved(data[position])
                    data.removeAt(position)
                    notifyDataSetChanged()
                },
                { dismiss() })
        }
    }
}
