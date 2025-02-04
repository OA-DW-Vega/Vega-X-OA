package com.olam.warehouse.ginning.ui.ginninginprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.invisible
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.ginning_item_bale_list.view.*

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
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.ginning_item_bale_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(position))
    }

    private fun getItem(position: Int) = dataSet[position]

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(bale: Bale?) {
            val ivClose = itemView.ivClose
            ivClose.tag = bale
            itemView.tvBaleId.text = bale?.baleID
            itemView.tvShift.text = "Shift ".plus(bale?.shift)
            itemView.tvBaleWeight.text = bale?.grossWeight?.format().plus("KG")
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
