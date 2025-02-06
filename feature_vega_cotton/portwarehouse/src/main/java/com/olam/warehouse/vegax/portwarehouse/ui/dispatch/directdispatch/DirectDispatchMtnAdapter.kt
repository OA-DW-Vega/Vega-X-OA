package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.directdispatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemMtnDirectDispatchBinding

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */

class DirectDispatchMtnAdapter(
    private var incomingList: ArrayList<PortMtn>,
    private var listener: UpdateMtnList
) : RecyclerView.Adapter<DirectDispatchMtnAdapter.ViewHolder>() {

    interface UpdateMtnList {
        fun update()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mtn_direct_dispatch, parent, false)*/
        val v =
            ItemMtnDirectDispatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position])
    }

    inner class ViewHolder(itemView: ItemMtnDirectDispatchBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(mtn: PortMtn) {
            binding.rbMtn.isChecked = mtn.isChecked
            binding.tvMtnNumber.text = mtn.mtnNumber
            binding.tvNoOfBales.text = mtn.mtnBales?.size.toString()
            binding.tvTruckNumber.text = mtn.truckNumber
            val supplierName = mtn.suplierPlantDesc.split("Cotton", ignoreCase = true)
            val grades = mtn.grades?.toSet()?.map { it.grade }
            binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
            binding.tvSupplyPlantId.text = supplierName[0]
            itemView.setOnClickListener {
                incomingList[adapterPosition].isChecked = !incomingList[adapterPosition].isChecked
                notifyItemChanged(adapterPosition)
                listener.update()
            }
        }
    }

    fun updateData(incomingList: ArrayList<PortMtn>) {
        this.incomingList = incomingList
        notifyDataSetChanged()
    }
}
