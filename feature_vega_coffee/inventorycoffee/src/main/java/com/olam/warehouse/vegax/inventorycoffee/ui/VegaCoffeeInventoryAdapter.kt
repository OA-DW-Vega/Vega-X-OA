package com.olam.warehouse.vegax.inventorycoffee.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventorycoffee.R
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.CoffeeStorageLoc
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorycoffee.data.domain.model.VegaCoffeeInventoryWarehouseModel
import com.olam.warehouse.vegax.inventorycoffee.databinding.ItemVegaCoffeeInventoryChildBinding
import com.olam.warehouse.vegax.inventorycoffee.databinding.ItemVegaCoffeeInventoryParentBinding
import com.olam.warehouse.vegax.inventorycoffee.databinding.ItemVegaCoffeeMaterialDetailsBinding
import com.olam.warehouse.vegax.inventorycoffee.utils.CoffeeNavigateToDetailsListener

class VegaInventoryParentAdapter(
    private val items: List<VegaCoffeeInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: CoffeeNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaCoffeeInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaCoffeeInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card1)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card1)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card_2)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card_3)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_coffee_inventory_card_4)
                colorCode = R.color.card_blue
            }
        }
        holder.binding.tvKorData.text =
            if (!items[position].averageKor.isNullOrEmpty()) items[position].averageKor.toDouble()
                .formatThreeDigits() else ""
        holder.binding.tvWeightData.text =
            if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits().plus(" ")
                .plus("Kg") else ""
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
                    holder.binding.ivToggle.context, R.drawable.ic_coffee_arrow_down
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
                    holder.binding.expandLayout.setExpand(items[position].isExpanded)
                }
            }
        })

        val layout = LinearLayoutManager(holder.itemView.context)
        layout.orientation = LinearLayoutManager.VERTICAL
        holder.binding.rvChild.layoutManager = layout
        holder.binding.rvChild.adapter =
            VegaInventoryChildAdapter(items[position].storageLoc, listener, colorCode, materialList)
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
    private val items: List<CoffeeStorageLoc>,
    var listener: CoffeeNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaCoffeeInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder = ItemVegaCoffeeInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList =
            items[position].materialDetails.distinctBy { it.materialCode } as MutableList<MaterialDetails>
        /*holder.itemView.tv_kor_value.text =
            if (items[position].averageKor == null || items[position].averageKor?.isEmpty()!!) " "
            else items[position].averageKor?.toDouble()?.formatThreeDigits()
        holder.itemView.tv_origin_value.text = items[position].origin
        holder.itemView.tv_weight_value.text =
            if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits()
                .plus(" Kg") else ""*/
        holder.bindChild.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationName + " " + items[position].warehouseLocation.procureLocationCode

        holder.bindChild.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_coffee_material_details,
            ItemVegaCoffeeMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvWeightValue.text =
                    it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) bindItem.tvMaterialValue.text =
                        item.materialName
                }
            },
            itemClick = {
                listener.navigateToDetails(items[position], code)
            })
        holder.bindChild.cvLotDetail.setOnClickListener {
            listener.navigateToDetails(
                items[position],
                code
            )
        }
        holder.bindChild.view.background =
            ContextCompat.getDrawable(holder.bindChild.view.context, code)
    }
}
