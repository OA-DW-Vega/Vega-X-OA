package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagement.data.domain.model.ContainerLotDetails
import com.olam.warehouse.vegax.containermanagement.databinding.ItemVegaCameroonContainerInventoryLotDetailsBinding

class VegaCameroonContainerInventoryStuffedDetailsListAdapter(private val onClick: (ContainerLotDetails?) -> Unit) :
    RecyclerView.Adapter<VegaCameroonContainerInventoryStuffedDetailsListAdapter.ContainerInventoryLotViewHolder>() {
    private val mLotList = arrayListOf<ContainerLotDetails>()

    inner class ContainerInventoryLotViewHolder(itemView: ItemVegaCameroonContainerInventoryLotDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(containerLotDetails: ContainerLotDetails) {
            containerLotDetails.let {
                binding.tvLotIDValue.text = it.lotId
                binding.tvMaterialValue.text = it.materialCode
                binding.tvLocationValue.text = it.stLocation
                binding.tvWeightValue.text = it.weight
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContainerInventoryLotViewHolder {
        /*val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_cameroon_container_inventory_lot_details, parent, false)*/
        val v = ItemVegaCameroonContainerInventoryLotDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContainerInventoryLotViewHolder(v)
    }

    override fun getItemCount(): Int = mLotList.size

    override fun onBindViewHolder(holder: ContainerInventoryLotViewHolder, position: Int) {
        holder.bindItems(mLotList[position])
    }


}

