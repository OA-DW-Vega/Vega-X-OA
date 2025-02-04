package com.olam.warehouse.vegax.salescocoa.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaSalesLots
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.salescocoa.R
import com.olam.warehouse.vegax.salescocoa.databinding.ItemCocoaSaleLotBinding
import com.olam.warehouse.vegax.salescocoa.databinding.ItemCocoaWeighbridgeLotBinding
import com.olam.warehouse.vegax.salescocoa.utils.getDrawable
import kotlinx.android.synthetic.main.item_cocoa_sale_lot.view.*
import kotlinx.android.synthetic.main.item_cocoa_weighbridge_lot.view.*

class VegaCocoaSalesLotAdapter(
    var data: ArrayList<VegaCocoaSalesLots>,
    var isEdit: Boolean = false,
    private var isAddLot: Boolean = false,
    var isWtPUOM: Boolean = false,
    var isScale: Boolean = false,
    var listener: ItemRemoveListener,
    private var addWeightListener: AddWeightListener? = null, var isRounoff: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class LotViewHolder(bind: ItemCocoaWeighbridgeLotBinding) : RecyclerView.ViewHolder(bind.root)

    class ScaleViewHolder(bind: ItemCocoaSaleLotBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                val viewHolder =
                    ItemCocoaWeighbridgeLotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return LotViewHolder(
                    viewHolder
                )
            }
            else -> {
                val viewHolder =
                    ItemCocoaSaleLotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ScaleViewHolder(
                    viewHolder
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isScale) {
            2
        } else 1
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isScale) {
            holder.itemView.tvScaleLotValue.text = data[position].batchNumber
            holder.itemView.tvStLocationValue.text = data[position].storageLocationCode
            holder.itemView.ivScaleClose.setOnClickListener {
                showConformationDialog(position, holder.itemView.ivScaleClose)
            }
            holder.itemView.tvScaleWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(data[position].unitOfMeasure)
            holder.itemView.tvScaleGradeValue.text = data[position].materialName
            holder.itemView.tvScaleDispatchValue.text =
                data[position].editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                    .plus(data[position].weightToDispatchUOM)
            holder.itemView.tvScaleDispatchValue.isEnabled = isAddLot
            holder.itemView.tv_add_weight.setOnClickListener {
                addWeightListener?.addWeightForLot(position, data[position])
            }

        } else {
            holder.itemView.tvLotId.text = data[position].batchNumber
            holder.itemView.tvStLocationValue1.text = data[position].storageLocationCode
            if (isAddLot && data[position].editedWeight.isNullOrBlank()) {
                data[position].editedWeight = data[position].weight
            }
            holder.itemView.ivClose.setOnClickListener {
                if (isAddLot) {
                    showConformationDialog(position, holder.itemView.ivClose)
                } else {
                    listener.itemRemoved(data[position])
                }
            }
            holder.itemView.tvWeightValue.text =
                data[position].weight?.toDouble()?.formatThreeDigits()?.plus(" ")?.plus(data[position].unitOfMeasure)

            holder.itemView.tvGradeValue.text = data[position].materialName
            holder.itemView.etWeight.visibility =
                if (isEdit) View.VISIBLE else View.GONE
            holder.itemView.tvUnit.visibility =
                if (isEdit) View.VISIBLE else View.GONE
            holder.itemView.tvwtp.visibility =
                if (isEdit) View.VISIBLE else View.GONE
            if (!isAddLot) holder.itemView.etWeight.background = getDrawable(R.drawable.bg_white)
            holder.itemView.tvUnit.text =
                if (isWtPUOM) data[position].weightToDispatchUOM else data[position].unitOfMeasure
            holder.itemView.cbSelectAll.visibility = if (isEdit && isAddLot) View.VISIBLE else View.GONE
            holder.itemView.tvSelectAll.visibility = if (isEdit && isAddLot) View.VISIBLE else View.GONE
            holder.itemView.etWeight.setText(data[position].editedWeight?.toDouble()?.formatThreeDigits())
            holder.itemView.etWeight.isEnabled = isAddLot
            holder.itemView.cbSelectAll.isChecked = data[position].isChecked ?: false
            holder.itemView.ivClose.setImageDrawable(holder.itemView.context.getDrawable(if (isAddLot) R.drawable.ic_close_circle else R.drawable.ic_edit_gray))
            holder.itemView.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                data[position].isChecked = isChecked
                if (isChecked) {
                    data[position].editedWeight = data[position].weight?.toDouble()?.formatThreeDigits()

                } else {
                    data[position].editedWeight = "0.0"
                }
                holder.itemView.etWeight.setText(data[position].editedWeight?.toDouble()?.formatThreeDigits())
            }
            if (isAddLot) {
                holder.itemView.etWeight.onChange {
                    if (it.isNotEmpty()) {
                        data[position].editedWeight = it
                        val come: Int? = it.toDouble().compareTo(data[position].weight?.toDouble() ?: 0.0)
                        if (come ?: 0 <= 0) {
                            data[position].isLowerWeight = true
                            holder.itemView.etWeight.error = null
                        } else {
                            data[position].isLowerWeight = false
                            holder.itemView.etWeight.error =
                                holder.itemView.context.getString(R.string.less_weight_error)
                        }
                    }
                }
            }
            if (isAddLot) {
                holder.itemView.etWeight.setText(data[position].editedWeight?.toDouble()?.formatThreeDigits())
                holder.itemView.tvUnit.visibility = View.VISIBLE
            } else {
                holder.itemView.etWeight.setText(
                    data[position].editedWeight?.toDouble()?.formatThreeDigits().plus(" ")
                        .plus(if (isWtPUOM) data[position].weightToDispatchUOM else data[position].unitOfMeasure)
                )
                holder.itemView.tvUnit.visibility = View.INVISIBLE
            }
        }
    }

    fun addAllLots(lots: List<VegaCocoaSalesLots>) {
        data.clear()
        data.addAll(lots)
        notifyDataSetChanged()
    }

    fun addLot(lot: VegaCocoaSalesLots) {
        data.add(lot)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    /*fun getSize() = data.size > 0*/

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

    fun updateAddWeight(position: Int, weight: String, uom: String) {
        data[position].editedWeight = weight
        data[position].weightToDispatchUOM = uom
        notifyItemChanged(position)
    }
}
