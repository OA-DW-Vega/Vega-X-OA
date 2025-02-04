package com.olam.warehouse.ginning.ui.ginninginprogress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.ginning_item_bale_confirm.view.*

/**
 * Created by SangiliPandian C on 16-03-2020.
 */
class GinningBaleListConfirmAdapter :
    ListAdapter<Bale, GinningBaleListConfirmAdapter.ViewHolder>(BaleConfirmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.ginning_item_bale_confirm, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(bale: Bale) {
            itemView.tvBaleId.text = bale.baleID
            itemView.tvWeight.text = bale.grossWeight?.format().plus("KG")
        }
    }

    fun getItems(): List<Bale> = currentList
}

private class BaleConfirmDiffCallback : DiffUtil.ItemCallback<Bale>() {
    override fun areItemsTheSame(oldItem: Bale, newItem: Bale): Boolean =
        oldItem.baleID == newItem.baleID

    override fun areContentsTheSame(oldItem: Bale, newItem: Bale): Boolean =
        oldItem == newItem
}
