package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGiningPileSummaryBinding

class GinningPileAddBaleAdapter(private val onClick: (List<PileBale>?) -> Unit) :
    RecyclerView.Adapter<GinningPileAddBaleAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<PileBale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gining_pile_summary, parent, false)*/
        val v =
            ItemGiningPileSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItems()[position])
    }

    inner class ViewHolder(itemView: ItemGiningPileSummaryBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(key: String) {
            if (key.isEmpty()) return
            val value = dataSet[key]
            binding.tvDispatchGrade.text = key
            binding.tvNoOfBales.text = value?.size.toString()
            binding.tvWeight.text =
                value?.sumByDouble { it.netWeight?.toDouble() ?: 0.0 }?.formatTwoDigits().toString()
                    .plus(" ")
                    .plus("KG")
            binding.btnViewBale.setOnClickListener {
                value?.let { it1 -> onClick(it1) }
            }
        }
    }

    fun getItems(): MutableList<String> = dataSet.keys.toMutableList()


    fun updateData(dataSet: HashMap<String, List<PileBale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
