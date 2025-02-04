package com.olam.warehouse.vegax.inventoryghana.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryghana.R
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryghana.databinding.ItemVegaGhanaInventoryChildBinding
import com.olam.warehouse.vegax.inventoryghana.databinding.ItemVegaGhanaInventoryParentBinding
import kotlinx.android.synthetic.main.item_vega_ghana_inventory_child.view.*
import kotlinx.android.synthetic.main.item_vega_ghana_inventory_parent.view.*
import kotlinx.android.synthetic.main.item_vega_ghana_material_details.view.*

class VegaGhanaInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaGhanaInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaGhanaInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaGhanaInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaGhanaInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ghana)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_ghana)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ghana)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_ghana)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_ghana)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_ghana)
                colorCode = R.color.card_blue
            }
        }

        holder.binding.tvHumidityData.text =
            if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
        holder.binding.tvWeightData.text =
            if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits().plus(" ")
                .plus("MT") else ""
        holder.binding.tvWhName.text = items[position].warehouse.plant.plantName
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
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_ghana
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaGhanaInventoryExpandableLayout.OnExpandListener {
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
        holder.itemView.rv_child.adapter =
            VegaGhanaInventoryChildAdapter(items[position].storageLoc, listener, colorCode, materialList)
    }

    private fun updateCard(holder: ParentViewHolder, id: Int) {
        holder.binding.ivWhLogo.setImageDrawable(
            ContextCompat.getDrawable(
                holder.binding.ivToggle.context, id
            )
        )
    }
}

class VegaGhanaInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: VegaGhanaInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaGhanaInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaGhanaInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaGhanaInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items[position].materialDetails as MutableList<MaterialDetails>
        holder.itemView.tv_storage_location_value.text =
            items[position].warehouseLocation.procureLocationName.plus(" ")
                .plus(items[position].warehouseLocation.procureLocationCode)

        holder.itemView.rv_material.setUp(materialsList, R.layout.item_vega_ghana_material_details, { it, pos ->
            tvHumidityValue.text = if (!it.averageHumidity.isNullOrEmpty()) it.averageHumidity else ""
            tvWeightValue.text = it.totalWeight.plus(" MT")
            materialList.forEach { item ->
                if (it.materialCode.contains(item.materialCode))
                    tvMaterialValue.text = item.materialName
            }
        }, itemClick = {
            listener.navigateToDetails(items[position], code)
        })
        holder.itemView.cvLotDetail.setOnClickListener { listener.navigateToDetails(items[position], code) }
        holder.itemView.view.background = ContextCompat.getDrawable(holder.itemView.view.context, code)
    }
}
