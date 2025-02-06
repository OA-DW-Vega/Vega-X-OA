package com.olam.warehouse.vegax.localsalesecuador.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.localsalesecuador.databinding.ItemCoffeeSalesLotListDetailBinding

class VegaEcuadorCocoaSalesStocksAdapter(
    private val items: List<VegaCoffeeSalesLots>
) :
    RecyclerView.Adapter<VegaEcuadorCocoaSalesStocksAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemCoffeeSalesLotListDetailBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemCoffeeSalesLotListDetailBinding.inflate(
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

    fun getItems(): List<VegaCoffeeSalesLots> {
        return items
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.binding.tvLotId.text = items[position].batchNumber
        holder.binding.tvGradeValue.text = items[position].materialName
        holder.binding.tvStLocationValue.text = items[position].storageLocationCode
        holder.binding.tvWeightValue.text =
            items[position].weight?.toDouble()?.formatThreeDigits().plus(" ")
                .plus(items[position].unitOfMeasure)
        holder.binding.ivSelect.isChecked = items[position].isAdded ?: false
        holder.binding.llLotItem.setOnClickListener { view ->
            items[position].isAdded = !items[position].isAdded!!
            /*items.forEach { item ->
                if (item.batchNumber.equals(items[position].batchNumber) && item.materialCode.equals(items[position].materialCode)) item.isAdded =
                    items[position].isAdded
            }*/
        }

    }
}
