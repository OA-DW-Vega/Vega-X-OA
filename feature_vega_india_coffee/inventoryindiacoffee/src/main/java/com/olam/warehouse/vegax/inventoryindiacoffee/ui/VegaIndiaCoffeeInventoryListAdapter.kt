package com.olam.warehouse.vegax.inventoryindiacoffee.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryindiacoffee.R
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventoryindiacoffee.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryindiacoffee.databinding.ItemVegaIndiaCoffeeInventoryChildBinding
import com.olam.warehouse.vegax.inventoryindiacoffee.databinding.ItemVegaIndiaCoffeeInventoryParentBinding
import com.olam.warehouse.vegax.inventoryindiacoffee.databinding.ItemVegaIndiaCoffeeMaterialDetailsBinding
import com.olam.warehouse.vegax.inventoryindiacoffee.utils.KEY_CASHEW

class VegaIndiaCoffeeInventoryParentAdapter(
        private val items: List<VegaInventoryWarehouseModel>,
        private val materialList: List<VegaMaterial>,
        var listener: VegaIndiaCoffeeInventoryNavigateToDetailsListener
) :
        RecyclerView.Adapter<VegaIndiaCoffeeInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaIndiaCoffeeInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
                ItemVegaIndiaCoffeeInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_sesame)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_sesame)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_sesame)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_sesame)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_sesame)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_sesame)
                colorCode = R.color.card_blue
            }
        }
        /*if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
            holder.binding.tvHumidityData.text =
                if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits()
                    .plus(" ")
                    .plus("MT") else ""
        } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {*/
        holder.binding.tvHumidityData.text =
                if (!items[position].korRange.isNullOrEmpty()) items[position].korRange else ""
        holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits()
                        .plus(" ")
                        .plus("KG") else ""
//        }

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
                            holder.binding.ivToggle.context, R.drawable.ic_arrow_down_sesame
                    )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaIndiaCoffeeInventoryExpandableLayout.OnExpandListener {
            override fun onExpand(expanded: Boolean) {

                if (currentExpandPosition > -1 && currentExpandPosition != position) {
                    items[currentExpandPosition].isExpanded = false
                    notifyItemChanged(currentExpandPosition)
                }
                currentExpandPosition = position
                items[position].isExpanded = !items[position].isExpanded
                if (items[position].isExpanded) {
                    holder.binding.expandLayout.setExpand(items[position].isExpanded)
                }
            }
        })

        val layout = LinearLayoutManager(holder.itemView.context)
        layout.orientation = LinearLayoutManager.VERTICAL
        holder.binding.rvChild.layoutManager = layout
        holder.binding.rvChild.adapter =
            VegaIndiaCoffeeInventoryChildAdapter(
                items[position].storageLoc,
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

class VegaIndiaCoffeeInventoryChildAdapter(
        private val items: List<StorageLoc>,
        var listener: VegaIndiaCoffeeInventoryNavigateToDetailsListener, val code: Int,
        private val materialList: List<VegaMaterial>
) :
        RecyclerView.Adapter<VegaIndiaCoffeeInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaIndiaCoffeeInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChaild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
                ItemVegaIndiaCoffeeInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items[position].materialDetails as MutableList<MaterialDetails>
        holder.bindChaild.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationName.plus(" ")
                .plus(items[position].warehouseLocation.procureLocationCode)
        if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
            holder.bindChaild.tvHumidity.text = "KOR"
        }

        holder.bindChaild.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_india_coffee_material_details,
            ItemVegaIndiaCoffeeMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                /*if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
                    tvHumidityValue.text = if (!it.averageHumidity.isNullOrEmpty()) it.averageHumidity else ""
                    tvWeightValue.text = it.totalWeight.plus(" MT")
                } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {*/
                bindItem.tvHumidityValue.text =
                    if (!it.averageKor.isNullOrEmpty()) it.averageKor.toDouble()
                        .formatThreeDigits() else ""
                bindItem.tvWeightValue.text =
                    it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
//            }

                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode))
                        bindItem.tvMaterialValue.text = item.materialName
                }
            },
            itemClick = {
                listener.navigateToDetails(items[position], code)
            })
        holder.bindChaild.cvLotDetail.setOnClickListener {
            listener.navigateToDetails(
                items[position],
                code
            )
        }
        holder.bindChaild.view.background =
            ContextCompat.getDrawable(holder.bindChaild.view.context, code)
    }
}
