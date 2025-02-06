package com.olam.warehouse.vegax.processingsesame.ui.rmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.ItemSesameLotCardLayoutBinding

class VegaSesameRminLotAdapter(
    var data: ArrayList<VegaCoffeeRminLots>,
    var listener: SesameRminItemRemoveListener,
    private var addWeightListener: SesameRminAddWeightListener? = null
) :
    RecyclerView.Adapter<VegaSesameRminLotAdapter.ScaleViewHolder>() {

    class ScaleViewHolder(bind: ItemSesameLotCardLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScaleViewHolder {

        val viewHolder =
            ItemSesameLotCardLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ScaleViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ScaleViewHolder, position: Int) {
        holder.binding.tvScaleLotValue.text = data[position].batchNumber
        holder.binding.tvStLocationValue.text = data[position].storageLocationCode
        holder.binding.ivScaleClose.setOnClickListener {
            showConformationDialog(position, holder.binding.ivScaleClose)
        }
        holder.binding.tvScaleWeightValue.text =
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("KG")
        holder.binding.tvScaleGradeValue.text = data[position].materialName
        holder.binding.tvScaleDispatchValue.text =
            data[position].editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                .plus("KG")

        holder.binding.tvAddWeight.setOnClickListener {
            addWeightListener?.addWeightForLot(position, data[position])
        }
        holder.binding.cbEndLot.isChecked = data[position].isEndLot ?: false
        holder.binding.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
            data[position].isEndLot = isChecked
        }
    }

    fun addAllLots(lots: List<VegaCoffeeRminLots>) {
        data.addAll(lots)
        notifyDataSetChanged()
    }

    fun addLot(lot: VegaCoffeeRminLots) {
        data.add(lot)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
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

    fun updateAddWeight(position: Int, weight: String, uom: String) {
        data[position].editedWeight = weight
//        data[position].storageLoss = "1"
        data[position].weightToDispatchUOM = uom
        notifyItemChanged(position)
    }
}
