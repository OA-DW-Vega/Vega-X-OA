package com.olam.warehouse.vegax.inventorycameroon.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventorycameroon.R
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventorycameroon.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventorycameroon.databinding.ItemVegaCameroonInventoryChildBinding
import com.olam.warehouse.vegax.inventorycameroon.databinding.ItemVegaCameroonInventoryParentBinding
import com.olam.warehouse.vegax.inventorycameroon.databinding.ItemVegaCameroonMaterialDetailsBinding

class VegaCameroonInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaCameroonInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaCameroonInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaCameroonInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaCameroonInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            holder.adapterPosition == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_cameroon)
                colorCode = R.color.card_pink
            }
            holder.adapterPosition % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_cameroon)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            holder.adapterPosition % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_cameroon)
                colorCode = R.color.card_pink
            }
            holder.adapterPosition % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_cameroon)
                colorCode = R.color.card_orange
            }

            holder.adapterPosition % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_cameroon)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_cameroon)
                colorCode = R.color.card_blue
            }
        }

        holder.binding.tvHumidityData.text =
            if (!items[holder.adapterPosition].beanMoistureRange.isNullOrEmpty()) items[holder.adapterPosition].beanMoistureRange else ""
        holder.binding.tvWeightData.text =
            if (!items[holder.adapterPosition].weight.isNullOrEmpty()) items[holder.adapterPosition].weight.toDouble().formatThreeDigits().plus(" ")
                .plus("KG") else ""
        holder.binding.tvWhName.text = items[holder.adapterPosition].warehouse.warehouseName
        holder.binding.expandLayout.setExpand(items[holder.adapterPosition].isExpanded)
        if (items[holder.adapterPosition].isExpanded) {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, com.olam.warehouse.presentation.R.drawable.ic_arrow_up
                )
            )
        } else {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_cameroon
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaCameroonInventoryExpandableLayout.OnExpandListener {
            override fun onExpand(expanded: Boolean) {

                if (currentExpandPosition > -1 && currentExpandPosition != holder.adapterPosition) {
                    items[currentExpandPosition].isExpanded = false
                    notifyItemChanged(currentExpandPosition)
                }
                currentExpandPosition = holder.adapterPosition
                items[holder.adapterPosition].isExpanded = !items[holder.adapterPosition].isExpanded
                if (items[holder.adapterPosition].isExpanded) {
                    holder.binding.expandLayout.setExpand(items[holder.adapterPosition].isExpanded)
                }
            }
        })

        val layout = LinearLayoutManager(holder.itemView.context)
        layout.orientation = LinearLayoutManager.VERTICAL
        holder.binding.rvChild.layoutManager = layout
        holder.binding.rvChild.adapter =
            VegaEcuadorInventoryChildAdapter(
                items[holder.adapterPosition].storageLoc,
                listener,
                colorCode,
                materialList
            )
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
    var listener: VegaCameroonInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaEcuadorInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaCameroonInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChind = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaCameroonInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items[position].materialDetails as MutableList<MaterialDetails>
        holder.bindChind.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationName.plus(" ")
                .plus(items[position].warehouseLocation.procureLocationCode)

        holder.bindChind.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_cameroon_material_details,
            ItemVegaCameroonMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                if (it.totalWeight.toDouble() != 0.0) {
                    bindItem.tvHumidityValue.text =
                        if (!it.averageBeanMoisture.isNullOrEmpty()) it.averageBeanMoisture.toDouble()
                            .formatThreeDigits() else ""
                    bindItem.tvWeightValue.text =
                        it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
                    materialList.forEach { item ->
                        if (it.materialCode.contains(item.materialCode)) bindItem.tvMaterialValue.text =
                            item.materialName
                    }
                }
            },
            itemClick = {
                listener.navigateToDetails(items[position], code)
            })
        holder.bindChind.cvLotDetail.setOnClickListener {
            listener.navigateToDetails(
                items[position],
                code
            )
        }
        holder.bindChind.view.background =
            ContextCompat.getDrawable(holder.itemView.rootView.context, code)
    }
}
