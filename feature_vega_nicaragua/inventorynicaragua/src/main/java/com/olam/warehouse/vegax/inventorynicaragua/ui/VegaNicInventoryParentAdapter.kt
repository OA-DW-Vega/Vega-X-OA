package com.olam.warehouse.vegax.inventorynicaragua.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventorynicaragua.R
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.MaterialDetails
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicStorageLoc
import com.olam.warehouse.vegax.inventorynicaragua.databinding.ItemVegaNicInventoryChildBinding
import com.olam.warehouse.vegax.inventorynicaragua.databinding.ItemVegaNicMaterialDetailsBinding

/**
 * Created by Baskaran Kannan on 12/12/2020.
 */
class VegaNicInventoryParentAdapter(
        private val items: List<NicStorageLoc>,
        private val materialList: List<VegaMaterial>,
        private val onClick: (nicStorageLoc: NicStorageLoc) -> Unit) :
        RecyclerView.Adapter<VegaNicInventoryParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1

    class ParentViewHolder(bind: ItemVegaNicInventoryChildBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
                ItemVegaNicInventoryChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val materialsList =
                items[position].materialDetails.distinctBy { it.materialCode } as MutableList<MaterialDetails>
        holder.binding.tvStorageLocationValue.text =
            items[position].warehouseLocation.procureLocationCode + " - " + items[position].warehouseLocation.procureLocationName
        holder.binding.cbStoragLoc.isChecked = items[position].isChecked
        /*holder.itemView.cbStoragLoc.setOnCheckedChangeListener { buttonView, isChecked ->
            items[position].isChecked = !isChecked
            holder.itemView.cbStoragLoc.isChecked = items[position].isChecked
            onClick(items[position])
        }*/
        holder.itemView.setOnClickListener {
            items[position].isChecked = !items[position].isChecked
            holder.binding.cbStoragLoc.isChecked = items[position].isChecked
            onClick(items[position])
        }
        holder.binding.rvMaterial.setUpAdapter(
            materialsList,
            R.layout.item_vega_nic_material_details,
            ItemVegaNicMaterialDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvWeightValue.text =
                    if (it.totalWeight.isNotEmpty()) it.totalWeight.toDouble().formatThreeDigits()
                        .plus(it.uom) else ""
                bindItem.tvQualityValue.text = it.qualityGrade
                materialList.forEach { item ->
                    if (it.materialCode.contains(item.materialCode)) bindItem.tvMaterialValue.text =
                        item.materialName
                }
            },
            itemClick = {
//            listener.navigateToDetails(items[position], code)
            })
//        holder.itemView.cvLotDetail.setOnClickListener { listener.navigateToDetails(items[position], code) }
//        holder.itemView.view.background = ContextCompat.getDrawable(holder.itemView.view.context, code)
    }

}
