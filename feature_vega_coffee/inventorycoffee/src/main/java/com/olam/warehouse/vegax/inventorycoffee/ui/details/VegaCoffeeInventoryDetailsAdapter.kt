package com.olam.warehouse.vegax.inventorycoffee.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.CoffeeInventory
import com.olam.warehouse.vegax.inventorycoffee.databinding.ItemCoffeeInventoryDetailsLayoutBinding
import kotlinx.android.synthetic.main.item_coffee_inventory_details_layout.view.*

class VegaCoffeeInventoryDetailsAdapter(
    private val items: List<CoffeeInventory>,
    private val colorCode: Int,
    private val materialList: MutableList<VegaMaterial>
) :
    RecyclerView.Adapter<VegaCoffeeInventoryDetailsAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemCoffeeInventoryDetailsLayoutBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemCoffeeInventoryDetailsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.itemView.tv_lot_name.text = items[position].lotId
        holder.itemView.tv_weight_data.text = items[position].stockQty.toDouble().formatThreeDigits().plus(" Kg")
        holder.itemView.tv_kor_value.text =
            if (items[position].CI_MATIERE_ETRANGERE_CAFE.isNullOrEmpty()) items[position].foreignMatter else items[position].CI_MATIERE_ETRANGERE_CAFE
        holder.itemView.orgin_value.text =
            if (items[position].CI_GRAINS_NOIRS_CAFE.isNullOrEmpty()) items[position].blackBeans else items[position].CI_GRAINS_NOIRS_CAFE
        holder.itemView.tv_moisure_value.text =
            if (items[position].CI_BRISSURE_CAFE.isNullOrEmpty()) items[position].brokenBeans else items[position].CI_BRISSURE_CAFE
        if (items[position].grnDate.isNotEmpty() && items[position].grnDate.length == 8) {
            holder.itemView.tvDate.visible()
            holder.itemView.tvDateLbl.visible()
            holder.itemView.tvDate.text =
                items[position].grnDate.substring(4, 6).plus("/").plus(items[position].grnDate.substring(6)).plus("/")
                    .plus(items[position].grnDate.substring(0, 4))
        } else {
            holder.itemView.tvDate.gone()
            holder.itemView.tvDateLbl.gone()
        }
        if (colorCode != 0)
            holder.itemView.divideView.background = ContextCompat.getDrawable(holder.itemView.divideView.context, colorCode)
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.itemView.tv_material_name.text =
                item.materialName
        }
        if (!items[position].storageLocationCode.isNullOrEmpty()) {
            holder.itemView.tvStoLocValue.text = items[position].storageLocationCode
        } else {
            holder.itemView.tvStoLocValue.text = items[position].warehouseLocation.procureLocationCode
        }

    }
}
