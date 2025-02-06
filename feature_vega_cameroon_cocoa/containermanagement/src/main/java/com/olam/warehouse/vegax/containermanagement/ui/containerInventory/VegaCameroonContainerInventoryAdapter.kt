package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagement.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagement.databinding.ItemVegaCameroonContainerInventoryDetailsBinding
import com.olam.warehouse.vegax.containermanagement.ui.containerInventory.VegaCameroonContainerInventoryAdapter.VegaCameroonContainerViewHolder

class VegaCameroonContainerInventoryAdapter(private val onClick: (ContainerInventory?) -> Unit)
    : RecyclerView.Adapter<VegaCameroonContainerViewHolder>() {
    private val minventoryList = arrayListOf<ContainerInventory?>()

    inner class VegaCameroonContainerViewHolder(itemView: ItemVegaCameroonContainerInventoryDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(containerInventory: ContainerInventory?) {
            containerInventory.let {
                binding.tvContainerIdValue.text = it?.containerNum ?: ""
                binding.tvContainerStatusValue.text = it?.status ?: ""
                binding.tvContainerSizeValue.text = it?.containerSize ?: ""
                binding.tvShippingValue.text = it?.shippingLine ?: ""
                binding.tvdateOFEntry.text = it?.entryDate?.split(" ")?.get(0) ?: ""

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
        /* val v =
             LayoutInflater.from(parent.context)
                 .inflate(R.layout.item_vega_cameroon_container_inventory_details, parent, false)*/
        val v = ItemVegaCameroonContainerInventoryDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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
