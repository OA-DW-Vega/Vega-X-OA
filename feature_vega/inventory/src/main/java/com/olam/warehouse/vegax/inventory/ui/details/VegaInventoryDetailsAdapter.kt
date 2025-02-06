package com.olam.warehouse.vegax.inventory.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventory.data.domain.model.Inventory
import com.olam.warehouse.vegax.inventory.databinding.ItemInventoryDetailsLayoutBinding

class VegaInventoryDetailsAdapter(
    private val items: List<Inventory>,
    private val colorCode: Int,
    private val materialList: MutableList<VegaMaterial>
) :
    RecyclerView.Adapter<VegaInventoryDetailsAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemInventoryDetailsLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemInventoryDetailsLayoutBinding.inflate(
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
        holder.binding.tvMoisureValue.text = items[position].moisture
        holder.binding.tvWeightData.text =
            items[position].stockQty.toDouble().formatThreeDigits().plus(" Kg")
        holder.binding.tvKorValue.text =
            if (items[position].kor == null || items[position].kor?.isEmpty()!!) " "
            else items[position].kor?.toDouble()?.formatThreeDigits()
        holder.binding.orginValue.text = items[position].origin
        holder.binding.divideView.background =
            ContextCompat.getDrawable(holder.binding.divideView.context, colorCode)
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.binding.tvMaterialName.text =
                item.materialName
        }

    }
}
