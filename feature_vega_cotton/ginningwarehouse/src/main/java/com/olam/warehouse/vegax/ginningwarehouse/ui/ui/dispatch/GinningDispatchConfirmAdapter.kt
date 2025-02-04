package com.olam.warehouse.ginning.ui.dispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.item_ginning_dispatch_add_bale.view.*

/**
 * Created by Baskaran Kannan on 3/23/2020.
 */
class GinningDispatchConfirmAdapter :
    RecyclerView.Adapter<GinningDispatchConfirmAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<Bale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_dispatch_add_bale, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItems()[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(key: String) {
            if (key.isEmpty()) return
            val value = dataSet[key]
            itemView.llViewBale.gone()
            itemView.txt_gradevalue.text = key
            itemView.txt_balecount.text = value?.size.toString()
            itemView.txt_weight.text =
                value?.sumByDouble { it.netWeight ?: 0.0 }?.formatTwoDigits().toString().plus(" ")
                    .plus("KG")
        }
    }

    fun getItems(): MutableList<String> = dataSet.keys.toMutableList()


    fun updateData(dataSet: HashMap<String, List<Bale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
