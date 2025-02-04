package com.olam.warehouse.vegax.inventory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventory.R
import com.olam.warehouse.vegax.inventory.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventory.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventory.databinding.ItemVegaInventoryChildBinding
import com.olam.warehouse.vegax.inventory.databinding.ItemVegaInventoryParentBinding
import kotlinx.android.synthetic.main.item_vega_inventory_child.view.*
import kotlinx.android.synthetic.main.item_vega_inventory_parent.view.*

class VegaInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    var listener: NavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder = ItemVegaInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card)
                colorCode = com.olam.warehouse.presentation.R.color.green
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4)
                colorCode = R.color.card_blue
            }
        }

        holder.binding.tvKorData.text = if(!items[position].averageKor.isNullOrEmpty())items[position].averageKor.toDouble().formatThreeDigits() else ""
        holder.binding.tvWeightData.text = if(!items[position].weight.isNullOrEmpty())items[position].weight.toDouble().formatThreeDigits().plus(" ").plus("Kg") else ""
        holder.binding.tvWhName.text = items[position].warehouse.warehouseName
        holder.binding.expandLayout.setExpand(items[position].isExpanded)
        if (items[position].isExpanded) {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, com.olam.warehouse.presentation.R.drawable.ic_arrow_up
                )
            )
        } else {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            ExpandableLayout.OnExpandListener {
            override fun onExpand(expanded: Boolean) {

                if (currentExpandPosition > -1 && currentExpandPosition != position) {
                    items[currentExpandPosition].isExpanded = false
                    notifyItemChanged(currentExpandPosition)
                }
                currentExpandPosition = position
                items[position].isExpanded = !items[position].isExpanded
                if (items[position].isExpanded) {
                    holder.itemView.expand_layout.setExpand(items[position].isExpanded)
                }
            }
        })

        val layout = LinearLayoutManager(holder.itemView.context)
        layout.orientation = LinearLayoutManager.VERTICAL
        holder.itemView.rv_child.layoutManager = layout
        holder.itemView.rv_child.adapter = VegaInventoryChildAdapter(items[position].storageLoc, listener, colorCode)
    }

    private fun updateCard(holder: ParentViewHolder, id: Int) {
        holder.binding.ivWhLogo.setImageDrawable(
            ContextCompat.getDrawable(
                holder.binding.ivToggle.context, id
            )
        )
    }
}

class VegaInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: NavigateToDetailsListener, val code: Int
) :
    RecyclerView.Adapter<VegaInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder = ItemVegaInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.itemView.tv_kor_value.text =
            if (items[position].averageKor == null || items[position].averageKor?.isEmpty()!!) " "
        else items[position].averageKor?.toDouble()?.formatThreeDigits()
        holder.itemView.tv_origin_value.text = items[position].origin
        holder.itemView.tv_weight_value.text = if(!items[position].weight.isNullOrEmpty())items[position].weight.toDouble().formatThreeDigits().plus(" Kg") else ""
        holder.itemView.tv_storage_location_value.text =
            items[position].warehouseLocation.procureLocationName + " " + items[position].warehouseLocation.procureLocationCode
        holder.itemView.cvLotDetail.setOnClickListener { listener.navigateToDetails(items[position], code) }
        holder.itemView.view.background = ContextCompat.getDrawable(holder.itemView.view.context, code)

    }
}
