package com.olam.warehouse.odreceiving.ui.blt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.dorigin.entity.Bag
import com.olam.warehouse.odreceiving.R

class MissingBagsAdapter(private val bags: MutableList<Bag>, private val onClick: (Bag) -> Unit) :
    RecyclerView.Adapter<MissingBagsAdapter.ParamsViewHolder>() {

    private val itemViews: MutableList<View> = mutableListOf()

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMissingQRCode: TextView = itemView.findViewById(R.id.tvMissQr)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.bag_missing_list_item, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemCount(): Int = bags.size

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        val bag = bags[position]
        holder.tvMissingQRCode.text = bag.bagQrCode.toString()
        if (!itemViews.contains(holder.itemView)) {
            itemViews.add(holder.itemView)
        }
    }

    fun remove(position: Int, bag: Bag) {
        bags.removeAt(position)
        bags[position] = bag
        notifyDataSetChanged()
    }

    fun getItemViewAtPosition(position: Int) : View {
        return itemViews[position]
    }
}
