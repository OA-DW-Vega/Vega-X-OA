package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnWithGrades
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGinningIncomingMtnBinding

class GinningIncomingMtnAdapter(
    private var incomingList: MutableList<MtnWithGrades>,
    private var listener: UpdateMtnList
) : RecyclerView.Adapter<GinningIncomingMtnAdapter.ViewHolder>() {

    interface UpdateMtnList {
        fun update(incomingMtn: MtnWithGrades)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_incoming_mtn, parent, false)*/
        val v = ItemGinningIncomingMtnBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position])
    }

    inner class ViewHolder(itemView: ItemGinningIncomingMtnBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(mtn: MtnWithGrades) {
            binding.rbMtn.isChecked = mtn.mtn.isChecked
            binding.tvMtnNumber.text = mtn.mtn.mtnNumber
            binding.tvNoOfBales.text = mtn.mtn.baleCount
            binding.tvTruckNumber.text = mtn.mtn.truckNumber
            val supplierName = mtn.mtn.suplierPlantDesc.split("Cotton", ignoreCase = true)
            val grades = mtn.grades.map { it.grade }
            binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
            binding.tvSupplyPlantId.text = supplierName[0]
            itemView.rootView.setOnClickListener {
                changeRadioSelection(incomingList[adapterPosition])
            }
        }
    }

    private fun changeRadioSelection(mtn: MtnWithGrades) {
        incomingList.forEach {
            it.mtn.isChecked = it.mtn.mtnNumber == mtn.mtn.mtnNumber
        }
        listener.update(mtn)
        notifyDataSetChanged()
    }

    fun updateData(incomingList: MutableList<MtnWithGrades>) {
        this.incomingList = incomingList
        this.incomingList.reverse()
        notifyDataSetChanged()
    }
}


