package com.olam.warehouse.vegax.inventoryghanacocoa.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.master.vega.entity.VegaReceivingMtnLots
import com.olam.warehouse.master.vega.entity.VegaReceivingWarehouse
import com.olam.warehouse.master.vega.entity.VegaSupplyStorageLocation
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryghanacocoa.R
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaInventoryChildBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaInventoryParentBinding
import com.olam.warehouse.vegax.inventoryghanacocoa.databinding.ItemVegaGhanaCocoaMaterialDetailsBinding

class VegaGhanaCocoaInventoryTransitParentAdapter (
    private val data: List<VegaReceivingMtnLots>,
    private val items: List<VegaReceivingWarehouse>,
    private val storageLocation: List<VegaSupplyStorageLocation>,
    private val materialList: List<VegaMaterial>,
    var listener: VegaGhanaCocoaInventoryTransitDetailsListener
) :
    RecyclerView.Adapter<VegaGhanaCocoaInventoryTransitParentAdapter.ParentViewHolder>() {
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

       /* holder.binding.tvHumidityData.text =
            if (!items[position].admixtureRange.isNullOrEmpty()) items[position].admixtureRange else ""*/
        var uom = ""
        var filteredData =
            data.filter { it.supplyingPlantId.equals(items[position].supplyingPlantId) }
       // var filteredPlantData = data.filter { it.supplyingPlantId.equals(items[position].plant) }
        filteredData.forEach { it1->
            uom =  it1.uom
        }
        val weightSum = filteredData.sumByDouble { it.weight.toDouble() }
        holder.binding.tvWeightData.text =
            weightSum.formatThreeDigits().plus(" ")
                .plus(uom)
        holder.binding.tvWhName.text = filteredData[position].supplyingPlantName
        items[position].isExpanded?.let { holder.binding.expandLayout.setExpand(it) }
        if (items[position].isExpanded == true) {
            holder.binding.ivToggle.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.ivToggle.context, com.olam.warehouse.presentation.R.drawable.ic_arrow_up
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
                items[position].isExpanded = !(items[position].isExpanded == true)
                if (items[position].isExpanded == true) {
                    items[position].isExpanded?.let { holder.binding.expandLayout.setExpand(it) }
                }
            }
        })

        val layout = LinearLayoutManager(holder.itemView.context)
        layout.orientation = LinearLayoutManager.VERTICAL
        holder.binding.rvChild.layoutManager = layout


        var storagecodes = storageLocation.distinct().filter {it.plant.equals(items[position].supplyingPlantId)}.map { it.storageLocationCode }
        var storageNames = storageLocation.distinct().filter {it.plant.equals(items[position].supplyingPlantId)}.map { it.storageLocationName }
        var filteredMaterialList =  arrayListOf<String>()
        filteredMaterialList.clear()
        storagecodes.forEach { it1->
            var materialName = (filteredData.distinct().filter { it.storageLocationCode.equals(it1.toString()) }).map { it.materialName }
            materialName.forEach { it2->
                if((!(filteredMaterialList.contains(it2.toString()))))
                filteredMaterialList.add(it2.toString())
            }
        }



        var filteredMaterialData = filteredData.distinct().filter { it.supplyingPlantId.equals(items[position].supplyingPlantId) }
        holder.binding.rvChild.adapter =
            VegaGhanaInventoryTransitChildAdapter(
                storagecodes,
                storageNames,
                filteredMaterialList,
                (filteredMaterialData.distinct()),
                items[position],
                storageLocation[position],
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

class VegaGhanaInventoryTransitChildAdapter(
    private val storageCodesList: List<String>,
    private val storageNamesList: List<String?>,
    private val filteredMaterialList: List<String>,
    private val items: List<VegaReceivingMtnLots>,
    private val VegaReceivingWarehouse: VegaReceivingWarehouse,
    private val vegaSupplyStorageLocation: VegaSupplyStorageLocation,
    var listener: VegaGhanaCocoaInventoryTransitDetailsListener, val code: Int,
    private val materialList: List<VegaMaterial>
) :
    RecyclerView.Adapter<VegaGhanaInventoryTransitChildAdapter.ChildViewHolder>() {

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
        return storageCodesList.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val materialsList = items as MutableList<VegaReceivingMtnLots>
        holder.bindChild.tvStorageLocationValue.text =
            storageCodesList[position].plus(" ")
                .plus(storageNamesList[position])
        var filteredList = materialsList.filter { it.storageLocationCode.equals(storageCodesList[position]) }.filter { it.supplyingPlantId.equals(VegaReceivingWarehouse.supplyingPlantId) }.filter { it.materialName.equals(filteredMaterialList[position]) }

        var filteredMaterialList =  arrayListOf<String>()
        var totalWeight: Double? = 0.0
        val vegaMaterialList = mutableListOf<VegaReceivingMtnLots>()
        filteredList.forEach {
            totalWeight = ((totalWeight!!) + (it.weight.toDouble()))
        }
        filteredList.forEach{
        var vegaReceivingMtnLot = VegaReceivingMtnLots()
        // totalWeight = ((totalWeight!!) + (it.weight?.toDouble()!!))
        if((!(filteredMaterialList.contains(it.materialNumber)))) {
            filteredMaterialList.add(it.materialNumber)
            vegaReceivingMtnLot.weight = totalWeight!!
            vegaReceivingMtnLot.uom = it.uom
            vegaReceivingMtnLot.materialNumber = it.materialNumber
            //  totalWeight = 0.0
            vegaMaterialList.add(vegaReceivingMtnLot)
        }
        }
        val finalMaterialList = vegaMaterialList

        holder.bindChild.rvMaterial.setUpAdapter(
            finalMaterialList,
            R.layout.item_vega_ghana_cocoa_material_details,
            ItemVegaGhanaCocoaMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvWeightValue.text = it.weight.formatThreeDigits().plus(" ").plus(it.uom)
                materialList.forEach { item ->
                    if ((it.materialNumber).contains(item.materialCode))
                        bindItem.tvMaterialValue.text = item.materialName
                }
            },
            itemClick = {
                listener.navigateTransitToDetails(items[position], code)
            })
        holder.bindChild.cvLotDetail.setOnClickListener {
            listener.navigateTransitToDetails(
                items[position],
                code
            )
        }
        holder.bindChild.view.background =
            ContextCompat.getDrawable(holder.bindChild.view.context, code)
    }
}


