package com.olam.warehouse.vegax.mtntghana.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacocoa.entity.VegaGhanaCocoaDispatchLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.mtntghana.R
import com.olam.warehouse.vegax.mtntghana.databinding.ItemGhanaLotSummaryBinding
import kotlinx.android.synthetic.main.item_ghana_lot_summary.view.*

class VegaGhanaMtntLotAdapter(
    var data: ArrayList<VegaGhanaCocoaDispatchLots>,
    var isEdit: Boolean = false,
    var isClose: Boolean = false,
    var listener: GhanaMTNTItemRemoveListener
) :
    RecyclerView.Adapter<VegaGhanaMtntLotAdapter.LotViewHolder>() {

    class LotViewHolder(bind: ItemGhanaLotSummaryBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LotViewHolder {
        val viewHolder =
            ItemGhanaLotSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        holder.itemView.ivClose.setOnClickListener {
            showConformationDialog(position, holder.itemView.ivClose)
        }
        holder.itemView.tvUnit.text = data[position].unitOfMeasure
        holder.itemView.tvWeightValue.text =
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(data[position].unitOfMeasure)
        holder.itemView.tvGradeValue.text = data[position].materialName
        val editWeight =
            if (data[position].editedWeight.isNullOrEmpty()) "0.0" else data[position].editedWeight?.toDouble()
                ?.formatThreeDigits()
        holder.itemView.etWeight.setText(editWeight)
        holder.itemView.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isClose) R.drawable.ic_ghana_close_black else R.drawable.ic_ghana_edit_gray))
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
        holder.itemView.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isChecked = isChecked
            if (isChecked) {
                data[position].editedWeight = data[position].weight?.toDouble()?.formatThreeDigits()

            } else {
                data[position].editedWeight = "0.0"
            }
            holder.itemView.etWeight.setText(data[position].editedWeight)
        }
        holder.itemView.cbEndLot.isChecked = data[position].isEndLot ?: false
        holder.itemView.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
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
