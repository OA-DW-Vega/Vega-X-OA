package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.drying

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.IncomingLot
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemDryingBinding
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
        //val v = LayoutInflater.from(parent.context).inflate(R.layout.item_drying, parent, false)
        val v = ItemDryingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = mFilteredLots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(mFilteredLots[position], position)
    }

    inner class ViewHolder(itemView: ItemDryingBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(lot: IncomingLot, position: Int) {
            binding.cbLot.isChecked = lot.isSelected
            binding.tvTruckNumber.text = lot.truckNumber
            binding.tvLotNumber.text = lot.lotNumber
            binding.tvContainerNumber.text = lot.containerNumber?.ifEmpty { "Not Available" }
            if(!lot.createdTS.isNullOrEmpty() && lot.createdTS.contains("T"))
                binding.txtDryingDate.text = lot.createdTS.split("T").get(0)
            itemView.rootView.setOnClickListener {
                binding.cbLot.isChecked = !binding.cbLot.isChecked
                changeRadioSelection(adapterPosition)
            }
            binding.cbLot.setOnClickListener {
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
                        if (row.lotNumber.lowercase(Locale.ROOT)
                                .contains(charSearch.toString().lowercase(Locale.ROOT))
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
