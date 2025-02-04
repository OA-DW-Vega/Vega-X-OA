package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.item_mtn_dispatch_add_bale.view.*

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class MtnDispatchConfirmAdapter :
    RecyclerView.Adapter<MtnDispatchConfirmAdapter.ViewHolder>() {

    private var dataSet = HashMap<String, List<MtnBale>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mtn_dispatch_add_bale, parent, false)
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


    fun updateData(dataSet: HashMap<String, List<MtnBale>>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
