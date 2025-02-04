package com.olam.warehouse.vegax.inventoryecuador.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryecuador.R
import com.olam.warehouse.vegax.inventoryecuador.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventoryecuador.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventoryecuador.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryecuador.databinding.ItemVegaEcuadorInventoryChildBinding
import com.olam.warehouse.vegax.inventoryecuador.databinding.ItemVegaEcuadorInventoryParentBinding
import kotlinx.android.synthetic.main.item_vega_ecuador_inventory_child.view.*
import kotlinx.android.synthetic.main.item_vega_ecuador_inventory_parent.view.*
import kotlinx.android.synthetic.main.item_vega_ecuador_material_details.view.*

class VegaEcuadorInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaEcuadorInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaEcuadorInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaEcuadorInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaEcuadorInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ecu)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_ecu)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ecu)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_ecu)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_ecu)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_ecu)
                colorCode = R.color.card_blue
            }
        }

        holder.binding.tvHumidityData.text =
            if (!items[position].humidityRange.isNullOrEmpty()) items[position].humidityRange else ""
        holder.binding.tvWeightData.text =
            if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits().plus(" ")
                .plus("QE") else ""
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
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_ecu
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaEcuadorInventoryExpandableLayout.OnExpandListener {
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
            VegaEcuadorInventoryChildAdapter(items[position].storageLoc, listener, colorCode, materialList)
    }

    private fun updateCard(holder: ParentViewHolder, id: Int) {
        holder.binding.ivWhLogo.setImageDrawable(
            ContextCompat.getDrawable(
                holder.binding.ivToggle.context, id
            )
        )
    }
}

class VegaEcuadorInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: VegaEcuadorInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaEcuadorInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaEcuadorInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaEcuadorInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        holder.itemView.rv_material.setUp(materialsList, R.layout.item_vega_ecuador_material_details, { it, pos ->
            tvHumidityValue.text = it.averageHumidity.toDouble().formatThreeDigits()
            tvWeightValue.text = it.totalWeight.plus(" QE")
            materialList.forEach { item ->
                if (it.materialCode.contains(item.materialCode)) tvMaterialValue.text = item.materialName
            }
        }, itemClick = {
            listener.navigateToDetails(items[position], code)
        })
        holder.itemView.cvLotDetail.setOnClickListener { listener.navigateToDetails(items[position], code) }
        holder.itemView.view.background = ContextCompat.getDrawable(holder.itemView.view.context, code)
    }
}
