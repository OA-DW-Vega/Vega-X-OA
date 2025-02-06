package com.olam.warehouse.vegax.portwarehouse.ui.pile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemPortPileSummaryBinding

class PortPileAddBaleAdapter(private val onClick: (List<PortPileBale>?) -> Unit) :
    RecyclerView.Adapter<PortPileAddBaleAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<PortPileBale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_port_pile_summary, parent, false)*/
        val v =
            ItemPortPileSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItems()[position])
    }

    inner class ViewHolder(itemView: ItemPortPileSummaryBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(key: String) {
            if (key.isEmpty()) return
            val value = dataSet[key]
            binding.tvDispatchGrade.text = key
            binding.tvNoOfBales.text = value?.size.toString()
            binding.tvWeight.text =
                value?.sumByDouble { it.netWeight ?: 0.0 }?.formatTwoDigits().toString().plus(" ")
                    .plus("KG")
            binding.btnViewBale.setOnClickListener {
                value?.let { it1 -> onClick(it1) }
            }
        }
    }

    fun getItems(): MutableList<String> = dataSet.keys.toMutableList()


    fun updateData(dataSet: HashMap<String, List<PortPileBale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
