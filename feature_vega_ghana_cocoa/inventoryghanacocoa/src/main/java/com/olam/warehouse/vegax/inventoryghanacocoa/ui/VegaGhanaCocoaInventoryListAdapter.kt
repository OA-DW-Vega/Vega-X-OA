package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.model.VegaUomDetails
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.StorageLoc
import com.olam.warehouse.vegax.inventoryghanacocoa.data.domain.model.VegaInventoryWarehouseModel
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaInventoryChildBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaInventoryParentBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaMaterialDetailsBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.BAG
import com.olam.warehouse.vegax.inventoryghanacocoa.utils.MATERIAL_CODE

class VegaGhanaCocoaInventoryParentAdapter(
    private val items: List<VegaInventoryWarehouseModel>,
    private val materialList: List<VegaMaterial>,
    private var uomDetails: List<VegaUomDetails>,
    var listener: VegaGhanaCocoaInventoryNavigateToDetailsListener
) :
    RecyclerView.Adapter<VegaGhanaCocoaInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaGhanaCocoaInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaGhanaCocoaInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int
        when {
            position == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ghana_cocoa)
                colorCode = R.color.card_pink
            }
            position % 2 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_ghana_cocoa)
                colorCode = com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            }
            position % 3 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card1_ghana_cocoa)
                colorCode = R.color.card_pink
            }
            position % 4 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_2_ghana_cocoa)
                colorCode = R.color.card_orange
            }

            position % 5 == 0 -> {
                updateCard(holder, R.drawable.ic_inventory_card_3_ghana_cocoa)
                colorCode = R.color.card_brown
            }
            else -> {
                updateCard(holder, R.drawable.ic_inventory_card_4_ghana_cocoa)
                colorCode = R.color.card_blue
            }
        }

        holder.binding.tvHumidityData.text =
            if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""
        var totalweight: Double = calculateWeight(items[position].storageLoc)?.toDouble() ?: 0.0
        holder.binding.tvWeightData.text =
            ((items[position].weight.toDouble()).div(totalweight)).formatThreeDigits().plus(" ")
                .plus("BAGS")
        /*holder.binding.tvWeightData.text =
            if (!items[position].weight.isNullOrEmpty()) items[position].weight.toDouble().formatThreeDigits().plus(" ")
                .plus("BAGS") else ""*/
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
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_ghana_cocoa
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaGhanaCocoaInventoryExpandableLayout.OnExpandListener {
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
            VegaGhanaInventoryChildAdapter(
                items[position].storageLoc,
                listener,
                colorCode,
                materialList,
                uomDetails
            )
    }

    private fun updateCard(holder: ParentViewHolder, id: Int) {
        holder.binding.ivWhLogo.setImageDrawable(
            ContextCompat.getDrawable(
                holder.binding.ivToggle.context, id
            )
        )
    }

    private fun calculateWeight(items: List<StorageLoc>): Double? {
        var netWeight: Double? = 0.0
        items.forEach { it2 ->
            it2.materialDetails.forEach { it1 ->
                var uom =
                    ((uomDetails.filter { (MATERIAL_CODE.plus(it.materialCode)).equals(it1.materialCode) }).filter {
                        it.fromUom.equals(BAG)
                    }).singleOrNull()
                netWeight = uom?.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()?:0.0)
            }
        }
        return netWeight
    }
}

class VegaGhanaInventoryChildAdapter(
    private val items: List<StorageLoc>,
    var listener: VegaGhanaCocoaInventoryNavigateToDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>,
    private val uomDetails: List<VegaUomDetails>
) :
    RecyclerView.Adapter<VegaGhanaInventoryChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaGhanaCocoaInventoryChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaGhanaCocoaInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        holder.bindChild.rvMaterial.setUpAdapter(materialsList,
            R.layout.item_vega_ghana_cocoa_material_details,
            ItemVegaGhanaCocoaMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvHumidityValue.text =
                    if (!it.averageHumidity.isNullOrEmpty()) it.averageHumidity else ""
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) {
                        bindItem.tvMaterialValue.text = item.materialName
                        bindItem.tvWeightValue.text =
                            ((it.totalWeight.toDouble()).div(calculateWeight(item.materialCode).toDouble())).formatThreeDigits()
                                .plus(" ").plus(resources.getString(R.string.inventory_bags))
                    }
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

    private fun calculateWeight(materialCode: String): String {
        var netWeight = ""
        var uom = ((uomDetails.filter { (it.materialCode).equals(materialCode) }).filter {
            it.fromUom.equals(BAG)
        }).single()
        netWeight =
            (uom.value1?.toInt()?.toDouble()?.div(uom.value2?.toInt()?.toDouble()!!)).toString()
        return netWeight
    }
}
