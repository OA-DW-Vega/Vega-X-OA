package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemMtnDispatchAddBaleBinding

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */

class MtnDispatchAddBaleAdapter(
    private val onViewBales: (bales: List<MtnBale>) -> Unit
) : RecyclerView.Adapter<MtnDispatchAddBaleAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<MtnBale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mtn_dispatch_add_bale, parent, false)*/
        val v = ItemMtnDispatchAddBaleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItems()[position])
    }

    inner class ViewHolder(itemView: ItemMtnDispatchAddBaleBinding) :
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


    fun updateData(dataSet: HashMap<String, List<MtnBale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
