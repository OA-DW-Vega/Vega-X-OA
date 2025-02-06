package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.seedcotton

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemIncomingLotBinding
import java.util.*

/**
 * Created by SangiliPandian C on 05-03-2020.
 */

class GinningIncomingLotAdapter(
    private var incomingList: MutableList<IncomingLot>,
    private var listener: Listener
) :
    RecyclerView.Adapter<GinningIncomingLotAdapter.ViewHolder>(), Filterable {

    private var mSelectedItem = -1
    private var mFilteredLots: MutableList<IncomingLot> = Collections.emptyList()

    interface Listener {
        fun update(incomingLot: IncomingLot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_incoming_lot, parent, false)*/
        val v = ItemIncomingLotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = mFilteredLots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(mFilteredLots[position], position == mSelectedItem)
    }

    inner class ViewHolder(itemView: ItemIncomingLotBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(lot: IncomingLot, b: Boolean) {
            val rbLot = binding.rbLot
            rbLot.isChecked = b
            binding.tvTruck.text = lot.truckNumber
            binding.tvLotNo.text = lot.lotNumber
            binding.tvContainer.text =
                if (lot.containerNumber?.isNotEmpty() == true) lot.containerNumber else "Not Available"
            rbLot.setOnClickListener { changeRadioSelection(adapterPosition) }
            itemView.setOnClickListener {
                changeRadioSelection(adapterPosition)
            }
        }
    }

    private fun changeRadioSelection(position: Int) {
        val lot = mFilteredLots[position]
        mSelectedItem = position
        mFilteredLots.forEach {
            it.isSelected = it.lotNumber == lot.lotNumber
        }
        listener.update(lot)
        notifyItemRangeChanged(0, mFilteredLots.size)
    }

    fun updateData(incomingList: MutableList<IncomingLot>) {
        this.incomingList = incomingList
        mFilteredLots = incomingList
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
