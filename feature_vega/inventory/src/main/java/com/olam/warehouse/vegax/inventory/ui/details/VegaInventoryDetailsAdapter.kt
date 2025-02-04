package com.olam.warehouse.vegax.inventory.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventory.data.domain.model.Inventory
import com.olam.warehouse.vegax.inventory.databinding.ItemInventoryDetailsLayoutBinding
import kotlinx.android.synthetic.main.item_inventory_details_layout.view.*

class VegaInventoryDetailsAdapter(
    private val items: List<Inventory>,
    private val colorCode: Int,
    private val materialList: MutableList<VegaMaterial>
) :
    RecyclerView.Adapter<VegaInventoryDetailsAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemInventoryDetailsLayoutBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemInventoryDetailsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.itemView.tv_lot_name.text = items[position].lotId
        holder.itemView.tv_moisure_value.text = items[position].moisture
        holder.itemView.tv_weight_data.text = items[position].stockQty.toDouble().formatThreeDigits().plus(" Kg")
        holder.itemView.tv_kor_value.text = if (items[position].kor == null || items[position].kor?.isEmpty()!!) " "
        else items[position].kor?.toDouble()?.formatThreeDigits()
        holder.itemView.orgin_value.text = items[position].origin
        holder.itemView.divideView.background = ContextCompat.getDrawable(holder.itemView.divideView.context, colorCode)
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.itemView.tv_material_name.text =
                item.materialName
        }

    }
}
