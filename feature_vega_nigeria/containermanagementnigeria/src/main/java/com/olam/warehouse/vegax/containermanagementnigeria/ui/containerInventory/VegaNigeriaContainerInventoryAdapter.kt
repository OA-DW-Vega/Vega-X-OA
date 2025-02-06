package com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.ContainerInventory
import com.olam.warehouse.vegax.containermanagementnigeria.databinding.ItemVegaNigeriaContainerInventoryDetailsBinding
import com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory.VegaNigeriaContainerInventoryAdapter.VegaNigeriaContainerViewHolder

class VegaNigeriaContainerInventoryAdapter(private val onClick: (ContainerInventory?) -> Unit)
    : RecyclerView.Adapter<VegaNigeriaContainerViewHolder>() {
    private val minventoryList = arrayListOf<ContainerInventory?>()

    inner class VegaNigeriaContainerViewHolder(itemView: ItemVegaNigeriaContainerInventoryDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(containerInventory: ContainerInventory?) {
            containerInventory.let {
                binding.tvContainerIdValue.text = it?.containerNum ?: ""
                binding.tvContainerStatusValue.text = it?.status ?: ""
                binding.tvContainerSizeValue.text = it?.containerSize ?: ""
                binding.tvShippingValue.text = it?.shippingLine ?: ""
                binding.tvdateOFEntry.text = it?.entryDate?.split(" ")?.get(0) ?: ""
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
        /*val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_nigeria_container_inventory_details, parent, false)*/
        val v = ItemVegaNigeriaContainerInventoryDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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
