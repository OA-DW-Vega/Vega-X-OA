package com.olam.warehouse.vegax.inventorynigeria.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventorynigeria.R
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventorynigeria.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventorynigeria.databinding.ItemVegaNigeriaCocoaInventoryChildBinding
import com.olam.warehouse.vegax.inventorynigeria.databinding.ItemVegaNigeriaCocoaInventoryParentBinding
import com.olam.warehouse.vegax.inventorynigeria.databinding.ItemVegaNigeriaCocoaMaterialDetailsBinding
import com.olam.warehouse.vegax.inventorynigeria.utils.KEY_CASHEW
import com.olam.warehouse.vegax.inventorynigeria.utils.KEY_NIGERIA_COCOA
import com.olam.warehouse.vegax.inventorynigeria.utils.KEY_SESAME

class VegaNigeriaSesameInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaNigeriaCocoaInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaNigeriaSesameInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaNigeriaCocoaInventoryParentBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaNigeriaCocoaInventoryParentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_nigeria)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_nigeria)
                colorCode = com.olam.warehouse.presentation.R.color.green
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_nigeria)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_nigeria)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_nigeria)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_nigeria)
                colorCode = R.color.card_blue
            }
        }
        if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
            holder.binding.tvHumidityData.text =
                if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble()
                    .formatThreeDigits()
                    .plus(" ")
                    .plus("MT") else ""
        } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
            holder.binding.tvHumidityRange.text = "KOR"
            holder.binding.tvHumidityData.text =
                if (!items[position].korRange.isNullOrEmpty()) items[position].korRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble()
                    .formatThreeDigits()
                    .plus(" ")
                    .plus("KG") else ""
        } else if (getCurrentKey().split("_")[2].contains(KEY_NIGERIA_COCOA)) {
            holder.binding.tvHumidityData.text =
                if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
            holder.binding.tvWeightData.text =
                if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble()
                    .formatThreeDigits()
                    .plus(" ")
                    .plus("MT") else ""
        }

        holder.binding.tvWhName.text = items[position].warehouse.plant.plantName
        holder.binding.expandLayout.setExpand(items[position].isExpanded)
        if (items[position].isExpanded) {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context,
                    com.olam.warehouse.presentation.R.drawable.ic_arrow_up
                )
            )
        } else {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_nigeria
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaNigeriaCocoaInventoryExpandableLayout.OnExpandListener {
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
            VegaNigeriaCocoaInventoryChildAdapter(
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

class VegaNigeriaCocoaInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: VegaNigeriaCocoaInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaNigeriaCocoaInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaNigeriaCocoaInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaNigeriaCocoaInventoryChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items[position].materialDetails as MutableList<MaterialDetails>
        holder.bindChild.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationName.plus(" ")
                .plus(items[position].warehouseLocation.procureLocationCode)
        if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
            holder.bindChild.tvHumidity.text = "KOR"
        }

        holder.bindChild.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_nigeria_cocoa_material_details,
            ItemVegaNigeriaCocoaMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                if (getCurrentKey().split("_")[2].contains(KEY_SESAME)) {
                    bindItem.tvHumidityValue.text =
                        if (!it.averageHumidity.isNullOrEmpty()) it.averageHumidity else ""
                    bindItem.tvWeightValue.text = it.totalWeight.plus(" MT")
                } else if (getCurrentKey().split("_")[2].contains(KEY_CASHEW)) {
                    bindItem.tvHumidityValue.text =
                        if (!it.averageKor.isNullOrEmpty()) it.averageKor.toDouble()
                            .formatThreeDigits() else ""
                    bindItem.tvWeightValue.text =
                        it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
                } else if (getCurrentKey().split("_")[2].contains(KEY_NIGERIA_COCOA)) {
                    bindItem.tvHumidityValue.text =
                        if (!it.averageKor.isNullOrEmpty()) it.averageKor.toDouble()
                            .formatThreeDigits() else ""
                    bindItem.tvWeightValue.text =
                        it.totalWeight.toDouble().formatThreeDigits().plus(" KG")
                }

                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode))
                        bindItem.tvMaterialValue.text = item.materialName
                }
            }, itemClick = {
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
