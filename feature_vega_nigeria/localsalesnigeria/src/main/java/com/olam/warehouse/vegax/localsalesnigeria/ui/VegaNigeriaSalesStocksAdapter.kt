package com.olam.warehouse.vegax.localsalesnigeria.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.localsalesnigeria.databinding.ItemNigeriaSalesLotListDetailBinding
import com.olam.warehouse.vegax.localsalesnigeria.utils.convertMtToKg
import kotlinx.android.synthetic.main.item_nigeria_sales_lot_list_detail.view.*

class VegaNigeriaSalesStocksAdapter(
    private val items: List<VegaCoffeeSalesLots>
) :
    RecyclerView.Adapter<VegaNigeriaSalesStocksAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemNigeriaSalesLotListDetailBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemNigeriaSalesLotListDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        holder.itemView.tvLotId.text = items[position].batchNumber
        holder.itemView.tvGradeValue.text = items[position].materialName
        holder.itemView.tvStLocationValue.text = items[position].storageLocationCode
        when(items[position].unitOfMeasure){
            "KG" -> {
                holder.itemView.tvWeightValue.text =
                    items[position].weight?.toDouble()?.formatThreeDigits().plus(" ").plus(items[position].unitOfMeasure)
            }
            "MT" -> {
                holder.itemView.tvWeightValue.text =
                    convertMtToKg(items[position].weight.toString()).toDouble().formatThreeDigits()
                        .plus(" ").plus("KG")
            }
        }
//        holder.itemView.tvWeightValue.text =
//            items[position].weight?.toDouble()?.formatThreeDigits().plus(" ").plus(items[position].unitOfMeasure)
        holder.itemView.ivSelect.isChecked = items[position].isAdded ?: false
        holder.itemView.llLotItem.setOnClickListener { view ->
            items[position].isAdded = !items[position].isAdded!!
            /*items.forEach { item ->
                if (item.batchNumber.equals(items[position].batchNumber) && item.materialCode.equals(items[position].materialCode)) item.isAdded =
                    items[position].isAdded
            }*/
        }

    }
}
