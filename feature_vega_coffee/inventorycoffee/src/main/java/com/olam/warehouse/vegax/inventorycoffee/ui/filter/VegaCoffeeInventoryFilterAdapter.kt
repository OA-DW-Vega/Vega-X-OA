package com.olam.warehouse.vegax.inventory.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventorycoffee.databinding.ItemVegaCoffeeInventoryFilterLayoutBinding
import com.olam.warehouse.vegax.inventorycoffee.ui.UnCheckListener
import kotlinx.android.synthetic.main.item_vega_coffee_inventory_filter_layout.view.*

class VegaCoffeeInventoryFilterAdapter(private val items: List<FilterList>, var listener: UnCheckListener) :
    RecyclerView.Adapter<VegaCoffeeInventoryFilterAdapter.FilterViewHolder>() {

    class FilterViewHolder(bind: ItemVegaCoffeeInventoryFilterLayoutBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val viewHolder =
            ItemVegaCoffeeInventoryFilterLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.itemView.tv_inventory_filter.text = items[position].value
        holder.itemView.setOnClickListener {
            items[position].isSelected = !items[position].isSelected
            if (!items[position].isSelected) listener.isAllSelected(0)
            notifyItemChanged(position)
        }
        ViewCompat.setBackground(
            holder.itemView.tv_inventory_filter,
            ContextCompat.getDrawable(
                holder.itemView.main.context,
                if (items[position].isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                    com.olam.warehouse.presentation.R.drawable.item_deselected_white
            )
        )

        holder.itemView.tv_inventory_filter.setTextColor(
            ContextCompat.getColor(
                holder.itemView.main.context,
                if (items[position].isSelected) com.olam.warehouse.presentation.R.color.white else
                    com.olam.warehouse.presentation.R.color.black
            )
        )
    }
}
