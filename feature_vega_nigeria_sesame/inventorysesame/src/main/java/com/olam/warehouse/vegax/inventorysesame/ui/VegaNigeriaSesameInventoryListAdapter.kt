package com.olam.warehouse.vegax.inventorysesame.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventorysesame.R
import com.olam.warehouse.vegax.inventorysesame.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorysesame.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventorysesame.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventorysesame.databinding.ItemVegaNigeriaSesameInventoryChildBinding
import com.olam.warehouse.vegax.inventorysesame.databinding.ItemVegaNigeriaSesameInventoryParentBinding
import com.olam.warehouse.vegax.inventorysesame.databinding.ItemVegaNigeriaSesameMaterialDetailsBinding
import com.olam.warehouse.vegax.inventorysesame.utils.KEY_CASHEW
import com.olam.warehouse.vegax.inventorysesame.utils.KEY_SESAME

class VegaNigeriaSesameInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaNigeriaSesameInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaNigeriaSesameInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaNigeriaSesameInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaNigeriaSesameInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_sesame)
                colorCode = R.color.card_brown
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_sesame)
                colorCode = R.color.card_green
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_sesame)
                colorCode = R.color.card_orange
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_sesame)
                colorCode = R.color.card_blue
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_sesame)
                colorCode = R.color.card_pink
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_sesame)
                colorCode = R.color.card_blue
            }
        }
        if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
            holder.binding.tvHumidityData.text =
                if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits()
                    .plus(" ")
                    .plus("MT") else ""
        } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
            holder.binding.tvHumidityRange.text = "KOR"
            holder.binding.tvHumidityData.text =
                if (!items[position].korRange.isNullOrEmpty()) items[position].korRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits()
                    .plus(" ")
                    .plus("KG") else ""
        }

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
            VegaNigeriaSesameInventoryExpandableLayout.OnExpandListener {
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
            VegaNigeriaSesameInventoryChildAdapter(
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

class VegaNigeriaSesameInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: VegaNigeriaSesameInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaNigeriaSesameInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaNigeriaSesameInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindingChild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaNigeriaSesameInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items[position].materialDetails as MutableList<MaterialDetails>
        holder.bindingChild.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationName.plus(" ")
                .plus(items[position].warehouseLocation.procureLocationCode)
        if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
            holder.bindingChild.tvHumidity.text = "KOR"
        }

        holder.bindingChild.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_nigeria_sesame_material_details,
            ItemVegaNigeriaSesameMaterialDetailsBinding::inflate,
            { it, pos, bindingItem ->
                if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
                    bindingItem.tvHumidityValue.text =
                        if (!it.averageHumidity.isNullOrEmpty()) it.averageHumidity else ""
                    bindingItem.tvWeightValue.text = it.totalWeight.plus(" MT")
                } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
                    bindingItem.tvHumidityValue.text =
                        if (!it.averageKor.isNullOrEmpty()) it.averageKor.toDouble()
                            .formatThreeDigits() else ""
                    bindingItem.tvWeightValue.text =
                        it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
                }

                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode))
                        bindingItem.tvMaterialValue.text = item.materialName
                }
            }, itemClick = {
                listener.navigateToDetails(items[position], code)
            })
        holder.bindingChild.cvLotDetail.setOnClickListener {
            listener.navigateToDetails(
                items[position],
                code
            )
        }
        holder.bindingChild.view.background =
            ContextCompat.getDrawable(holder.bindingChild.view.context, code)
    }
}
