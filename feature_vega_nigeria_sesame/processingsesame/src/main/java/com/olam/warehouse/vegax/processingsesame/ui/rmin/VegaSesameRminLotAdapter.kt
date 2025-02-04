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
import kotlinx.android.synthetic.main.item_sesame_lot_card_layout.view.*

class VegaSesameRminLotAdapter(
    var data: ArrayList<VegaCoffeeRminLots>,
    var listener: SesameRminItemRemoveListener,
    private var addWeightListener: SesameRminAddWeightListener? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ScaleViewHolder(bind: ItemSesameLotCardLayoutBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val viewHolder =
            ItemSesameLotCardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScaleViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.tvScaleLotValue.text = data[position].batchNumber
        holder.itemView.tvStLocationValue.text = data[position].storageLocationCode
        holder.itemView.ivScaleClose.setOnClickListener {
            showConformationDialog(position, holder.itemView.ivScaleClose)
        }
        holder.itemView.tvScaleWeightValue.text =
            data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus("KG")
        holder.itemView.tvScaleGradeValue.text = data[position].materialName
        holder.itemView.tvScaleDispatchValue.text =
            data[position].editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                .plus("KG")

        holder.itemView.tv_add_weight.setOnClickListener {
            addWeightListener?.addWeightForLot(position, data[position])
        }
        holder.itemView.cbEndLot.isChecked = data[position].isEndLot ?: false
        holder.itemView.cbEndLot.setOnCheckedChangeListener { buttonView, isChecked ->
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
