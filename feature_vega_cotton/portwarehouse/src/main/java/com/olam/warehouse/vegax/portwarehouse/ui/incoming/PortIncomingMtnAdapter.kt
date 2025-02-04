package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnWithGrades
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.incoming_mtn_row_item.view.*

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncomingMtnAdapter(
    private var incomingList: MutableList<MtnWithGrades>,
    private var listener: UpdateMtnList
) : RecyclerView.Adapter<PortIncomingMtnAdapter.ViewHolder>() {

    interface UpdateMtnList {
        fun update(incomingMtn: MtnWithGrades)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.incoming_mtn_row_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(mtn: MtnWithGrades) {
            itemView.rbMtn.isChecked = mtn.mtn.isChecked
            itemView.tvMtnNumber.text = mtn.mtn.mtnNumber
            itemView.tvNoOfBales.text = mtn.mtn.baleCount
            itemView.tvTruckNumber.text = mtn.mtn.truckNumber
            val supplierName = mtn.mtn.suplierPlantDesc.split("Cotton", ignoreCase = true)
            val grades = mtn.grades.map { it.grade }
            itemView.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
            itemView.tvSupplyPlantId.text = supplierName[0]
            itemView.setOnClickListener {
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
        notifyDataSetChanged()
    }
}
