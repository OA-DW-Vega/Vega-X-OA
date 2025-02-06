package com.olam.warehouse.vegax.inventoryghana.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.inventoryghana.R
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.ReportsList
import com.olam.warehouse.vegax.inventoryghana.data.domain.model.VegaGhanaInventoryReportResponse
import com.olam.warehouse.vegax.inventoryghana.databinding.ItemVegaGhanaInventoryBalanceChildBinding
import com.olam.warehouse.vegax.inventoryghana.databinding.ItemVegaGhanaInventoryParentBinding
import java.util.*

class VegaGhanaInventoryBalanceParentAdapter(
    private val items: ArrayList<VegaGhanaInventoryReportResponse>,
     ctx : VegaGhanaInventoryBalanceFragment
) :
    RecyclerView.Adapter<VegaGhanaInventoryBalanceParentAdapter.ParentViewHolder>() {
    var currentExpandPosition = -1
    var totalweight = 0.0
    var context = ctx

    class ParentViewHolder(bind: ItemVegaGhanaInventoryParentBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val viewHolder =
            ItemVegaGhanaInventoryParentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParentViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val colorCode: Int

        updateCard(holder, R.drawable.ic_inventory_card1_ghana)
        colorCode = R.color.card_pink


        holder.binding.tvWhLbl.text = context.getString(R.string.storage_location)
        holder.binding.tvWhName.text = items.get(position).storageLocationCode.toString()

        holder.binding.tvWeightData.text = items.get(position).totalweight.toString()

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
                    holder.binding.ivToggle.context, R.drawable.ic_arrow_down_ghana
                )
            )

        }
        holder.binding.expandLayout.setOnExpandListener(object :
            VegaGhanaInventoryExpandableLayout.OnExpandListener {
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
            VegaGhanaInventoryBalanceChildAdapter(items.get(position).reportsList, colorCode)
    }

    private fun updateCard(holder: ParentViewHolder, id: Int) {
        holder.binding.ivWhLogo.setImageDrawable(
            ContextCompat.getDrawable(
                holder.binding.ivToggle.context, id
            )
        )
    }


}


class VegaGhanaInventoryBalanceChildAdapter(
    private val items: List<ReportsList>,
    val code: Int
) :
    RecyclerView.Adapter<VegaGhanaInventoryBalanceChildAdapter.ChildViewHolder>() {

    class ChildViewHolder(bind: ItemVegaGhanaInventoryBalanceChildBinding) :
        RecyclerView.ViewHolder(bind.root) {
        val bindChild = bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val viewHolder =
            ItemVegaGhanaInventoryBalanceChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {

        holder.bindChild.tvStorageLocationValue.text =
            items.get(position).storageLocationCode.toString()

        if (items.get(position).totalFgrnQty.isNotEmpty())
            holder.bindChild.tvtotalFgrnQty.text =
                items.get(position).totalFgrnQty.toString().toDouble().formatThreeDigits()
                    .plus("   MT")
        else holder.bindChild.llfgrnqty.visibility = View.GONE

        if (items.get(position).totalDryingLossQty.isNotEmpty())
            holder.bindChild.tvtotaldringlossquality.text =
                items.get(position).totalDryingLossQty.toString().toDouble().formatThreeDigits()
                    .plus("   MT")
        else holder.bindChild.lldryinglossquality.visibility = View.GONE

        if (items.get(position).totalGrnQty.isNotEmpty())
            holder.bindChild.tvtotalGrnQty.text =
                items.get(position).totalGrnQty.toString().toDouble().formatThreeDigits()
                    .plus("   MT")
        else holder.bindChild.llgrnqty.visibility = View.GONE

        if (items.get(position).totalRminQty.isNotEmpty())
            holder.bindChild.tvtotalRminQty.text =
                items.get(position).totalRminQty.toString().toDouble().formatThreeDigits()
                    .plus("   MT")
        else holder.bindChild.llrminqty.visibility = View.GONE

        if (items.get(position).totalMtntQty.isNotEmpty())
            holder.bindChild.tvtotalMtntQty.text =
                items.get(position).totalMtntQty.toString().toDouble().formatThreeDigits()
                    .plus("   MT")
        else holder.bindChild.llmtntty.visibility = View.GONE

        holder.bindChild.view.background =
            ContextCompat.getDrawable(holder.bindChild.view.context, code)
    }


}
