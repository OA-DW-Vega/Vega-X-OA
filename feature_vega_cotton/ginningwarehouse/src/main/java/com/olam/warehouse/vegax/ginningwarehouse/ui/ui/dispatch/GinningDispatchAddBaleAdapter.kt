package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGinningDispatchAddBaleBinding

/**
 * Created by Baskaran Kannan on 3/23/2020.
 */
class GinningDispatchAddBaleAdapter(
    private val onViewBales: (bales: List<Bale>) -> Unit
) : RecyclerView.Adapter<GinningDispatchAddBaleAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<Bale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_dispatch_add_bale, parent, false)*/
        val view = ItemGinningDispatchAddBaleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItems()[position])
    }

    inner class ViewHolder(itemView: ItemGinningDispatchAddBaleBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(key: String) {
            if (key.isEmpty()) return
            val value = dataSet[key]
            binding.txtGradevalue.text = key
            binding.txtBalecount.text = value?.size.toString()
            binding.txtWeight.text =
                value?.sumByDouble { it.netWeight ?: 0.0 }?.formatTwoDigits().toString().plus(" ")
                    .plus("KG")
            binding.btnViewbaleid.setOnClickListener {
                value?.let { it1 -> onViewBales(it1) }
            }
        }
    }

    fun getItems(): MutableList<String> = dataSet.keys.toMutableList()


    fun updateData(dataSet: HashMap<String, List<Bale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
