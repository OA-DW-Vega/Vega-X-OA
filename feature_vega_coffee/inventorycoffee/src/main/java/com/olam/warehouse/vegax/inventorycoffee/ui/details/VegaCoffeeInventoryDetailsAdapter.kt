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

class VegaCoffeeInventoryDetailsAdapter(
    private val items: List<CoffeeInventory>,
    private val colorCode: Int,
    private val materialList: MutableList<VegaMaterial>
) :
    RecyclerView.Adapter<VegaCoffeeInventoryDetailsAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemCoffeeInventoryDetailsLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemCoffeeInventoryDetailsLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DetailsViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.binding.tvLotName.text = items[position].lotId
        holder.binding.tvWeightData.text =
            items[position].stockQty.toDouble().formatThreeDigits().plus(" Kg")
        holder.binding.tvKorValue.text =
            if (items[position].CI_MATIERE_ETRANGERE_CAFE.isNullOrEmpty()) items[position].foreignMatter else items[position].CI_MATIERE_ETRANGERE_CAFE
        holder.binding.orginValue.text =
            if (items[position].CI_GRAINS_NOIRS_CAFE.isNullOrEmpty()) items[position].blackBeans else items[position].CI_GRAINS_NOIRS_CAFE
        holder.binding.tvMoisureValue.text =
            if (items[position].CI_BRISSURE_CAFE.isNullOrEmpty()) items[position].brokenBeans else items[position].CI_BRISSURE_CAFE
        if (items[position].grnDate.isNotEmpty() && items[position].grnDate.length == 8) {
            holder.binding.tvDate.visible()
            holder.binding.tvDateLbl.visible()
            holder.binding.tvDate.text =
                items[position].grnDate.substring(4, 6).plus("/")
                    .plus(items[position].grnDate.substring(6)).plus("/")
                    .plus(items[position].grnDate.substring(0, 4))
        } else {
            holder.binding.tvDate.gone()
            holder.binding.tvDateLbl.gone()
        }
        if (colorCode != 0)
            holder.binding.divideView.background =
                ContextCompat.getDrawable(holder.binding.divideView.context, colorCode)
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.binding.tvMaterialName.text =
                item.materialName
        }
        if (!items[position].storageLocationCode.isNullOrEmpty()) {
            holder.binding.tvStoLocValue.text = items[position].storageLocationCode
        } else {
            holder.binding.tvStoLocValue.text =
                items[position].warehouseLocation.procureLocationCode
        }

    }
}
