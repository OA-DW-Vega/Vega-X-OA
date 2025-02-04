package com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerInventoryAdapter.VegaNigeriaContainerViewHolder
import kotlinx.android.synthetic.main.item_vega_nigeria_container_inventory_details.view.*

class VegaNigeriaContainerInventoryAdapter(private val onClick: (ContainerInventory?) -> Unit)
    : RecyclerView.Adapter<VegaNigeriaContainerViewHolder>() {
    private val minventoryList = arrayListOf<ContainerInventory?>()

    inner class VegaNigeriaContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(containerInventory: ContainerInventory?) {
                containerInventory.let {
                    itemView.tvContainerIdValue.text = it?.containerNum ?: ""
                    itemView.tvContainerStatusValue.text = it?.status ?: ""
                    itemView.tvContainerSizeValue.text = it?.containerSize ?: ""
                    itemView.tvShippingValue.text = it?.shippingLine ?: ""
                    itemView.tvdateOFEntry.text = it?.entryDate?.split(" ")?.get(0) ?: ""
//                    itemView.tvdateOFEntry.text = DateUtils.getNoOfDaysStanding(it?.entryDate.toString(), App.getAppContext()).toString().plus(" Days")
//                    itemView.tvDaysStandingValue.text = DateUtils.getNoOfDaysStanding(it?.entryDate.toString()).toString()
                }
            itemView.setOnClickListener {
                onClick(containerInventory)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VegaNigeriaContainerViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_nigeria_container_inventory_details, parent, false)
        return VegaNigeriaContainerViewHolder(v)
    }

    override fun getItemCount(): Int = minventoryList.size

    override fun onBindViewHolder(
        holder: VegaNigeriaContainerViewHolder,
        position: Int
    ) {
        holder.bindItems(minventoryList[position])
    }

    fun addItems(container: List<ContainerInventory>) {
        minventoryList.clear()
        minventoryList.addAll(container)
        minventoryList.distinctBy { Pair(it?.containerNum, it?.containerNum) }
        minventoryList.reverse()
        notifyDataSetChanged()
    }

}
