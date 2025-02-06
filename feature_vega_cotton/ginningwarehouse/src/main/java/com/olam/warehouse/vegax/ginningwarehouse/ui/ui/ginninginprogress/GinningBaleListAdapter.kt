package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ginningwarehouse.databinding.GinningItemBaleListBinding

/**
 * Created by SangiliPandian C on 16-03-2020.
 */
class GinningBaleListAdapter(private var listener: BaleChangeListener) :
    RecyclerView.Adapter<GinningBaleListAdapter.ViewHolder>() {

    private val dataSet = arrayListOf<Bale?>()

    interface BaleChangeListener {
        fun update(bale: Bale?, position: Int)
    }

    override fun getItemCount() = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.ginning_item_bale_list, parent, false)*/
        val v =
            GinningItemBaleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(position))
    }

    private fun getItem(position: Int) = dataSet[position]

    inner class ViewHolder(itemView: GinningItemBaleListBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(bale: Bale?) {
            val ivClose = binding.ivClose
            ivClose.tag = bale
            binding.tvBaleId.text = bale?.baleID
            binding.tvShift.text = "Shift ".plus(bale?.shift)
            binding.tvBaleWeight.text = bale?.grossWeight?.format().plus("KG")
            if (bale?.isHold!!) ivClose.invisible() else ivClose.visible()
            ivClose.setOnClickListener {
//                dataSet.removeAt(adapterPosition)
//                notifyDataSetChanged()
                listener.update(it.tag as Bale, adapterPosition)
            }
        }
    }

    fun addItem(data: Bale) {
        if(!dataSet.contains(data)) {
            dataSet.add(data)
        }
        notifyItem()
    }

    private fun notifyItem() {
        dataSet.reverse()
        notifyDataSetChanged()
    }

    fun addItems(bales: List<Bale>) {
        dataSet.clear()
        bales.forEach {
            if(!dataSet.contains(it)) {
                dataSet.add(it)
            }
        }
        notifyItem()
    }

    fun isAtleastOneBaleAdded() = dataSet.size > 0

    fun isMoreBaleAdded() = dataSet.any { it?.isHold == false }

    fun getData() = dataSet

    fun clear() {
        dataSet.clear()
        notifyDataSetChanged()
    }
}
