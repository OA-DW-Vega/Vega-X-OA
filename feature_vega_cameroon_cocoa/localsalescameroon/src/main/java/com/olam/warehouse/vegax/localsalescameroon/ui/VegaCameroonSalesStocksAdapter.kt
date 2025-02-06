package com.olam.warehouse.vegax.localsalescameroon.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.localsalescameroon.databinding.ItemCameroonSalesLotListDetailBinding
import com.olam.warehouse.vegax.localsalescameroon.utils.convertMtToKg

class VegaCameroonSalesStocksAdapter(
    private val items: List<VegaCoffeeSalesLots>
) :
    RecyclerView.Adapter<VegaCameroonSalesStocksAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemCameroonSalesLotListDetailBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemCameroonSalesLotListDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DetailsViewHolder(
            viewHolder
        )
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return items.size
    }



    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.binding.tvLotId.text = items[position].batchNumber
        holder.binding.tvGradeValue.text = items[position].materialName
        holder.binding.tvStLocationValue.text = items[position].storageLocationCode
        when (items[position].unitOfMeasure) {
            "KG" -> {
                holder.binding.tvWeightValue.text =
                    items[position].weight?.toDouble()?.formatThreeDigits().plus(" ")
                        .plus(items[position].unitOfMeasure)
            }
            "MT" -> {
                holder.binding.tvWeightValue.text =
                    convertMtToKg(items[position].weight.toString()).toDouble().formatThreeDigits()
                        .plus(" ").plus("KG")
            }
        }

        holder.binding.ivSelect.isChecked = items[position].isAdded ?: false
        holder.binding.llLotItem.setOnClickListener { view ->
            items[position].isAdded = !items[position].isAdded!!

        }

    }
}
