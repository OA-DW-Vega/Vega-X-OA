package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.bale_summary_row.view.*

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class PortDispatchAddBaleAdapter(private val onClick: (PortBale?) -> Unit, private var isBreak: Boolean) :
    RecyclerView.Adapter<PortDispatchAddBaleAdapter.ItemViewHolder>() {

    private val mBales = arrayListOf<PortBale?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.bale_summary_row,
            parent, false
        )
        return ItemViewHolder(v)
    }

    override fun getItemCount() = mBales.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(mBales[position])
        if (isBreak) {
            holder.itemView.lDelete.visibility = View.GONE
        }
        holder.itemView.lDelete.setOnClickListener { onClick(mBales[position]) }
    }

    class ItemViewHolder(private val iv: View) : RecyclerView.ViewHolder(iv) {

        fun bindItems(bale: PortBale?) {
            bale?.let {
                iv.tvBaleId.text = it.baleId
                iv.tvGrade.text = it.grade
                iv.tvBaleWeight.text = it.netWeight.toString() + " kg"
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun addItem(bale: PortBale?) {
        bale?.let {
            if (!mBales.contains(bale)) {
                mBales.add(it)
            }
            notifyDataSetChanged()
        }
    }

    fun addItems(bale: List<PortBale>) {
        mBales.clear()
        bale.let { mBales.addAll(bale) }
        notifyDataSetChanged()
    }

    fun removeItem(bale: PortBale?) {
        bale?.let { mBales.remove(bale) }
        notifyDataSetChanged()
    }

    fun getBaleWeight(): Double {
        var totalBaleWeight = 0.0
        mBales.forEach {
            it?.netWeight?.let { weight ->
                totalBaleWeight += weight
            }
        }
        return totalBaleWeight
    }

    fun getBales(): List<PortBale> {
        val baleList = arrayListOf<PortBale>()
        mBales.forEach { it?.let { Bale -> baleList.add(Bale) } }
        return baleList
    }
}
