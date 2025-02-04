package com.olam.warehouse.ginning.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.ginning_inventory_row_item.view.*
import java.util.*

class InventoryAdapter(
    private val onClick: (Bale?) -> Unit,
    private val onUpdateBaleCount: (count: Int) -> Unit
) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>(), Filterable {

    private val mBales = arrayListOf<Bale?>()
    private var mFilteredBales = arrayListOf<Bale?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.ginning_inventory_row_item, parent, false)
        return InventoryViewHolder(v)
    }

    override fun getItemCount() = mFilteredBales.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bindItems(mFilteredBales[position])
    }

    fun addItems(bale: List<Bale>) {
        //mBales.clear()
        bale.let { mBales.addAll(bale) }
        mBales.distinctBy { Pair(it?.baleID, it?.baleID) }
        mFilteredBales.addAll(mBales)
        onUpdateBaleCount(mFilteredBales.size)
        notifyDataSetChanged()
    }


    fun removeItems() {
        mFilteredBales.clear()
        notifyDataSetChanged()
    }

    fun reverse() {
        mFilteredBales.reverse()
        notifyDataSetChanged()
    }

    fun getData() = mBales

    fun update(bales: List<Bale>) {
        mBales.clear()
        mFilteredBales.clear()
        mBales.addAll(bales)
        mFilteredBales.addAll(bales)
        onUpdateBaleCount(mFilteredBales.size)
        notifyDataSetChanged()
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(bale: Bale?) {

            bale?.let {
                itemView.tvIvenBaleId.text = it.baleID
                itemView.tvIvenBaleGrade.text = it.grade
                itemView.tvDate.text =
                    it.createdTS!!.split("T").get(0)
                itemView.tvIvenBaleWeight.text = it.netWeight?.formatTwoDigits().plus(" kg")
            }

            itemView.setOnClickListener {
                onClick(bale)

            }
        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSearch: CharSequence?): FilterResults {
                mFilteredBales = if (charSearch.isNullOrEmpty()) {
                    mBales
                } else {
                    val resultList = arrayListOf<Bale?>()
                    for (row in mBales) {
                        row?.baleID?.let {
                            if (it.toLowerCase(Locale.ROOT)
                                    .contains(charSearch.toString().toLowerCase(Locale.ROOT))
                            ) {
                                resultList.add(row)
                            }
                        }
                    }
                    mFilteredBales = resultList
                    mFilteredBales
                }
                val filterResults = FilterResults()
                filterResults.values = mFilteredBales
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val result = results?.values
                result?.let {
                    mFilteredBales = it as ArrayList<Bale?>
                    notifyDataSetChanged()
                }
            }

        }
    }
}
