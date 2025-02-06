package com.olam.warehouse.vegax.processingghana.ui.rmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.processingghana.R
import com.olam.warehouse.vegax.processingghana.databinding.ItemGhanaLotCardLayoutBinding

class VegaGhanaRminLotAdapter(
    var data: ArrayList<VegaCoffeeRminLots>,
    var listener: GhanaRminItemRemoveListener,
    private var addWeightListener: GhanaRminAddWeightListener? = null

) :
    RecyclerView.Adapter<VegaGhanaRminLotAdapter.ScaleViewHolder>() {

    class ScaleViewHolder(bind: ItemGhanaLotCardLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScaleViewHolder {

        val viewHolder =
            ItemGhanaLotCardLayoutBinding.inflate(
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
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("MT")
        holder.binding.tvScaleGradeValue.text = data[position].materialName

        holder.binding.tvScaleDispatchValue.setText(data[position].editedWeight)

        holder.binding.tvScaleDispatchValue.onChange {
//            if(it.isNotEmpty()){
//                val come: Int? = it.toDouble().compareTo(data[position].weight?.toDouble() ?: 0.0)
//                if (come ?: 0 <= 0) {
//                    data[position].isLowerWeight = true
//                    data[position].editedWeight =  holder.binding.tvScaleDispatchValue.text.toString()
//                    addWeightListener?.calculateWtp()
//                }
//                else {
//                    data[position].isLowerWeight = false
//                }
//            }
//            data[position].isLowerWeight = false
            data[position].editedWeight = holder.binding.tvScaleDispatchValue.text.toString()
            addWeightListener?.calculateWtp()
            if(!data[position].editedWeight.isNullOrEmpty())
                data[position].isLowerWeight = data[position].editedWeight?.toDouble()!! <= data[position].weight?.toDouble()!!
        }

        holder.binding.cbSelectAll.isChecked = data[position].isChecked ?: false

        holder.binding.cbSelectAll.setOnCheckedChangeListener { it, isChecked ->
            data[position].isChecked = isChecked
            if (isChecked) {
                holder.binding.tvScaleDispatchValue.setText(data[position].weight?.replace(",", ""))
                data[position].editedWeight = holder.binding.tvScaleDispatchValue.text.toString()
                addWeightListener?.calculateWtp()
            } else {
                holder.binding.tvScaleDispatchValue.setText("")
                data[position].editedWeight = holder.binding.tvScaleDispatchValue.text.toString()
                addWeightListener?.calculateWtp()
            }
        }

//        holder.binding.tvScaleDispatchValue.text =
//            data[position].editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
//                .plus("KG")

//        holder.binding.tv_add_weight.setOnClickListener {
//            addWeightListener?.addWeightForLot(position, data[position])
//        }
//        holder.binding.cbEndLot.isChecked = data[position].isEndLot ?: false
//        holder.binding.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
//            data[position].isEndLot = isChecked
//        }
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
                view.context.getString(com.olam.warehouse.presentation.R.string.confirm),
                view.context.getString(R.string.cancel),
                {
                    listener.itemRemoved(data[position])
                    data.removeAt(position)
                    notifyDataSetChanged()
                },
                { dismiss() })
        }
    }

    fun updateAddWeight(position: Int, weight: String) {
        data[position].editedWeight = weight
//        data[position].storageLoss = "1"
//        data[position].weightToDispatchUOM = uom
//        notifyItemChanged(position)
    }
}
