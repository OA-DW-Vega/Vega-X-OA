package com.olam.warehouse.ginning.ui.pile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.item_gining_pile_summary.view.*

/**
 * Created by Baskaran Kannan on 04/16/2020.
 */
class GinningPileConfirmAdapter(private val onClick: (List<PileBale>?) -> Unit) :
    RecyclerView.Adapter<GinningPileConfirmAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<PileBale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gining_pile_summary, parent, false)
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
            itemView.tvDispatchGrade.text = key
            itemView.tvNoOfBales.text = value?.size.toString()
            itemView.btnViewBale.setOnClickListener {
                value?.let { it1 -> onClick(it1) }
            }
            itemView.tvWeight.text =
                value?.sumByDouble { it.netWeight?.toDouble() ?: 0.0 }?.formatTwoDigits().toString().plus(" ")
                    .plus("KG")
        }
    }

    fun getItems(): MutableList<String> = dataSet.keys.toMutableList()


    fun updateData(dataSet: HashMap<String, List<PileBale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
