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

    class DetailsViewHolder(bind: ItemNicInventoryDetailsLayoutBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val viewHolder =
            ItemNicInventoryDetailsLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DetailsViewHolder(
            viewHolder
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        holder.binding.tvLotName.text = items[position].lotId
        holder.binding.tvWeightData.text =
            (items[position].openQuantity?.toDouble() ?: 0.0).formatThreeDigits().plus(" Kg")
        holder.binding.tvQualityGradeValue.text = items[position].gradeDesc
        holder.binding.tvCertificateValue.text = items[position].certification
        holder.binding.cbLotCard.isChecked = items[position].isChecked
        if (items[position].grnDate.isNotEmpty() && items[position].grnDate.length == 8) {
            holder.binding.tvDate.visible()
            holder.binding.tvDateLbl.visible()
            holder.binding.tvDate.text =
                items[position].grnDate.substring(4, 6).plus("/")
                    .plus(items[position].grnDate.substring(6)).plus("/")
                    .plus(items[position].grnDate.substring(0, 4))
        } else {
            holder.binding.tvDate.gone()
            holder.binding.tvDateLbl.gone()
        }
        materialList.forEach { item ->
            if (items[position].materialCode.contains(item.materialCode)) holder.binding.tvMaterialName.text =
                item.materialName
        }
        if (!items[position].storageLocationCode.isNullOrEmpty()) {
            holder.binding.tvStoLocValue.text = items[position].storageLocationCode
        } else {
            holder.binding.tvStoLocValue.text =
                items[position].warehouseLocation.procureLocationCode
        }
        if (lotId.isNotEmpty()) holder.binding.tvQualityParms.visible() else holder.binding.tvQualityParms.gone()
        holder.itemView.setOnClickListener {
            items[position].isChecked = !items[position].isChecked
            holder.binding.cbLotCard.isChecked = items[position].isChecked
            if (holder.binding.cbLotCard.isChecked) {
                items.forEach { it.isChecked = false }
                items[position].isChecked = true
                onClick(items as MutableList<NicInventory>)

            }
            // onClick(items[position])
        }
        holder.binding.cbLotCard.setOnClickListener {
            items[position].isChecked = !items[position].isChecked
            if (holder.binding.cbLotCard.isChecked) {
                items.forEach { it.isChecked = false }
                items[position].isChecked = true
                onClick(items as MutableList<NicInventory>)

            } else {
                items.forEach { it.isChecked = false }
                onClick(items as MutableList<NicInventory>)
            }
        }

        holder.binding.tvQualityParms.setOnClickListener { onClickParams(items[position]) }

    }
}
