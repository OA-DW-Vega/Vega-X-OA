package com.olam.warehouse.vegax.inventorynicaragua.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaMaterial
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.inventorynicaragua.data.domain.model.NicInventory
import com.olam.warehouse.vegax.inventorynicaragua.databinding.ItemNicInventoryDetailsLayoutBinding
import kotlinx.android.synthetic.main.item_nic_inventory_details_layout.view.*

/**
 * Created by Baskaran Kannan on 12/14/2020.
 */
class VegaNicInventoryDetailsAdapter(
    private val items: List<NicInventory>,
    private val lotId: String,
    private val materialList: MutableList<VegaMaterial>,
    private val onClick: (nicInventory: MutableList<NicInventory>) -> Unit,
    private val onClickParams: (nicInventory: NicInventory) -> Unit
) :
        RecyclerView.Adapter<VegaNicInventoryDetailsAdapter.DetailsViewHolder>() {

    class DetailsViewHolder(bind: ItemNicInventoryDetailsLayoutBinding) : RecyclerView.ViewHolder(bind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
                ItemNicInventoryDetailsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailsViewHolder(
                viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.itemView.tv_lot_name.text = items[position].lotId
        holder.itemView.tv_weight_data.text =
            (items[position].openQuantity?.toDouble() ?: 0.0).formatThreeDigits().plus(" Kg")
        holder.itemView.tv_quality_grade_value.text = items[position].gradeDesc
        holder.itemView.tv_certificate_value.text = items[position].certification
        holder.itemView.cbLotCard.isChecked = items[position].isChecked
        if (items[position].grnDate.isNotEmpty() && items[position].grnDate.length == 8) {
            holder.itemView.tvDate.visible()
            holder.itemView.tvDateLbl.visible()
            holder.itemView.tvDate.text =
                items[position].grnDate.substring(4, 6).plus("/").plus(items[position].grnDate.substring(6)).plus("/")
                    .plus(items[position].grnDate.substring(0, 4))
        } else {
            holder.itemView.tvDate.gone()
            holder.itemView.tvDateLbl.gone()
        }
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.itemView.tv_material_name.text =
                    item.materialName
        }
        if (!items[position].storageLocationCode.isNullOrEmpty()) {
            holder.itemView.tvStoLocValue.text = items[position].storageLocationCode
        } else {
            holder.itemView.tvStoLocValue.text = items[position].warehouseLocation.procureLocationCode
        }
        if (lotId.isNotEmpty()) holder.itemView.tvQualityParms.visible() else holder.itemView.tvQualityParms.gone()
        holder.itemView.setOnClickListener {
            items[position].isChecked = !items[position].isChecked
            holder.itemView.cbLotCard.isChecked = items[position].isChecked
            if (holder.itemView.cbLotCard.isChecked) {
                items.forEach { it.isChecked = false }
                items[position].isChecked = true
                onClick(items as MutableList<NicInventory>)

            }
            // onClick(items[position])
        }
        holder.itemView.cbLotCard.setOnClickListener {
            items[position].isChecked = !items[position].isChecked
            if (holder.itemView.cbLotCard.isChecked) {
                items.forEach { it.isChecked = false }
                items[position].isChecked = true
                onClick(items as MutableList<NicInventory>)

            } else {
                items.forEach { it.isChecked = false }
                onClick(items as MutableList<NicInventory>)
            }
        }

        holder.itemView.tvQualityParms.setOnClickListener { onClickParams(items[position]) }

    }
}
