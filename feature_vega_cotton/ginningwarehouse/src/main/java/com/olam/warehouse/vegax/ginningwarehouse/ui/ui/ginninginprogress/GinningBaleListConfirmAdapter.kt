package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.ginninginprogress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.vegax.ginningwarehouse.databinding.GinningItemBaleConfirmBinding

/**
 * Created by SangiliPandian C on 16-03-2020.
 */
class GinningBaleListConfirmAdapter :
    ListAdapter<Bale, GinningBaleListConfirmAdapter.ViewHolder>(BaleConfirmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.ginning_item_bale_confirm, parent, false)*/
        val v = GinningItemBaleConfirmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(position))
    }

    inner class ViewHolder(itemView: GinningItemBaleConfirmBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(bale: Bale) {
            binding.tvBaleId.text = bale.baleID
            binding.tvWeight.text = bale.grossWeight?.format().plus("KG")
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
