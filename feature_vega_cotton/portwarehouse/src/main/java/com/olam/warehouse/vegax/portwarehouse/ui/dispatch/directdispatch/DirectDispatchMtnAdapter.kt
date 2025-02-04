package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.directdispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.item_mtn_direct_dispatch.view.*

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
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mtn_direct_dispatch, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(mtn: PortMtn) {
            itemView.rbMtn.isChecked = mtn.isChecked
            itemView.tvMtnNumber.text = mtn.mtnNumber
            itemView.tvNoOfBales.text = mtn.mtnBales?.size.toString()
            itemView.tvTruckNumber.text = mtn.truckNumber
            val supplierName = mtn.suplierPlantDesc.split("Cotton", ignoreCase = true)
            val grades = mtn.grades?.toSet()?.map { it.grade }
            itemView.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
            itemView.tvSupplyPlantId.text = supplierName[0]
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
