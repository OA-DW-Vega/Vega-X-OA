package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerInventoryAdapter.VegaCameroonContainerViewHolder
import kotlinx.android.synthetic.main.item_vega_cameroon_container_inventory_details.view.*

class VegaCameroonContainerInventoryAdapter(private val onClick: (ContainerInventory?) -> Unit)
    : RecyclerView.Adapter<VegaCameroonContainerViewHolder>() {
    private val minventoryList = arrayListOf<ContainerInventory?>()

    inner class VegaCameroonContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(containerInventory: ContainerInventory?) {
                containerInventory.let {
                    itemView.tvContainerIdValue.text = it?.containerNum ?: ""
                    itemView.tvContainerStatusValue.text = it?.status ?: ""
                    itemView.tvContainerSizeValue.text = it?.containerSize ?: ""
                    itemView.tvShippingValue.text = it?.shippingLine ?: ""
                    itemView.tvdateOFEntry.text = it?.entryDate?.split(" ")?.get(0) ?: ""

                }
            itemView.setOnClickListener {
                onClick(containerInventory)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VegaCameroonContainerViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_cameroon_container_inventory_details, parent, false)
        return VegaCameroonContainerViewHolder(v)
    }

    override fun getItemCount(): Int = minventoryList.size

    override fun onBindViewHolder(
        holder: VegaCameroonContainerViewHolder,
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
