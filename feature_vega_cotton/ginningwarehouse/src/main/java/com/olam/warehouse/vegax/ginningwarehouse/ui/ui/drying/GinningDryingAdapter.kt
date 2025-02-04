package com.olam.warehouse.ginning.ui.drying

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.item_drying.view.*
import java.util.*

/**
 * Created by SangiliPandian C on 13-03-2020.
 */

class GinningDryingAdapter(
    private var incomingList: MutableList<IncomingLot>,
    private var listener: DryingListener
) :
    RecyclerView.Adapter<GinningDryingAdapter.ViewHolder>(), Filterable {

    private var mFilteredLots: MutableList<IncomingLot> = Collections.emptyList()

    interface DryingListener {
        fun update()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_drying, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = mFilteredLots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(mFilteredLots[position], position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(lot: IncomingLot, position: Int) {
            itemView.cbLot.isChecked = lot.isSelected
            itemView.tvTruckNumber.text = lot.truckNumber
            itemView.tvLotNumber.text = lot.lotNumber
            itemView.tvContainerNumber.text =
                if (lot.containerNumber.isNotEmpty()) lot.containerNumber else "Not Available"
            itemView.txt_drying_date.text = lot.createdTS.split("T").get(0)
            itemView.setOnClickListener {
                itemView.cbLot.isChecked = !itemView.cbLot.isChecked
                changeRadioSelection(adapterPosition)
            }
            itemView.cbLot.setOnClickListener {
                changeRadioSelection(adapterPosition)
            }
        }
    }

    private fun changeRadioSelection(position: Int) {
        val lotList = mFilteredLots[position]
        lotList.isSelected = !lotList.isSelected
//        mFilteredLots[position].isSelected = checked
        listener.update()
        notifyDataSetChanged()
    }

    fun getItems(): List<IncomingLot> = incomingList


    fun updateData(incomingList: MutableList<IncomingLot>) {
        this.incomingList = incomingList
        this.mFilteredLots = incomingList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSearch: CharSequence?): FilterResults {
                mFilteredLots = if (charSearch.isNullOrEmpty()) {
                    incomingList
                } else {
                    val resultList = arrayListOf<IncomingLot>()
                    for (row in incomingList) {
                        if (row.lotNumber.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toString().toLowerCase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    mFilteredLots = resultList
                    mFilteredLots
                }
                val filterResults = FilterResults()
                filterResults.values = mFilteredLots
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val result = results?.values
                result?.let {
                    mFilteredLots = it as MutableList<IncomingLot>
                    notifyDataSetChanged()
                }
            }

        }
    }
}
