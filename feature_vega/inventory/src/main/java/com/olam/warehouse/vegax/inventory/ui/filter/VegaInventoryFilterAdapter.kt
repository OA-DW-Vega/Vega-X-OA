package com.olam.warehouse.vegax.inventory.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.inventory.data.domain.model.FilterList
import com.olam.warehouse.vegax.inventory.databinding.ItemVegaInventoryFilterLayoutBinding
import com.olam.warehouse.vegax.inventory.ui.UnCheckListener

class VegaInventoryFilterAdapter(private val items: List<FilterList>, var listener: UnCheckListener) :
    RecyclerView.Adapter<VegaInventoryFilterAdapter.FilterViewHolder>() {

    class FilterViewHolder(bind: ItemVegaInventoryFilterLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val viewHolder =
            ItemVegaInventoryFilterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return FilterViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.binding.tvInventoryFilter.text = items[position].value
        holder.itemView.setOnClickListener {
            items[position].isSelected = !items[position].isSelected
            if (!items[position].isSelected) listener.isAllSelected(0)
            notifyItemChanged(position)
        }
        ViewCompat.setBackground(
            holder.binding.tvInventoryFilter,
            ContextCompat.getDrawable(
                holder.binding.main.context,
                if (items[position].isSelected) com.olam.warehouse.presentation.R.drawable.item_selector_green else
                    com.olam.warehouse.presentation.R.drawable.item_deselected_white
            )
        )

        holder.binding.tvInventoryFilter.setTextColor(
            ContextCompat.getColor(
                holder.binding.main.context,
                if (items[position].isSelected) com.olam.warehouse.presentation.R.color.white else
                    com.olam.warehouse.presentation.R.color.black
            )
        )
    }
}
