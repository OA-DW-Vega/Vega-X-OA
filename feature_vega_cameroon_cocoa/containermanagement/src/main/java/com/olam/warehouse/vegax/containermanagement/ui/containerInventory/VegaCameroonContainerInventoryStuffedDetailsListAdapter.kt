package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.data.domain.model.ContainerLotDetails
import kotlinx.android.synthetic.main.item_vega_cameroon_container_inventory_lot_details.view.*

class VegaCameroonContainerInventoryStuffedDetailsListAdapter(private val onClick: (ContainerLotDetails?) -> Unit) :
    RecyclerView.Adapter<VegaCameroonContainerInventoryStuffedDetailsListAdapter.ContainerInventoryLotViewHolder>() {
    private val mLotList = arrayListOf<ContainerLotDetails>()

   inner class ContainerInventoryLotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       fun bindItems(containerLotDetails: ContainerLotDetails) {
           containerLotDetails.let {
               itemView.tvLotIDValue.text = it.lotId
               itemView.tvMaterialValue.text = it.materialCode
               itemView.tvLocationValue.text = it.stLocation
               itemView.tvWeightValue.text = it.weight
           }
       }
   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerInventoryLotViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_cameroon_container_inventory_lot_details, parent, false)
        return ContainerInventoryLotViewHolder(v)
    }

    override fun getItemCount(): Int = mLotList.size

    override fun onBindViewHolder(holder: ContainerInventoryLotViewHolder, position: Int) {
        holder.bindItems(mLotList[position])
    }


}

