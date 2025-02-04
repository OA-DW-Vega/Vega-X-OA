package com.olam.warehouse.vegax.dispatch.ui.stock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.dispatch.R
import kotlinx.android.synthetic.main.item_vega_lots.view.*

/**
 * Created by Baskaran Kannan on 2/20/2020.
 */
class VegaDispatchLotsAdapter(
    private val onClick: (Int) -> Unit,
    private val onUnClick: (Int) -> Unit
) :
    RecyclerView.Adapter<VegaDispatchLotsAdapter.ViewHolder>() {

    private var mStockList = arrayListOf<VegaDispatchLots>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_lots, parent, false)
        return ViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mStockList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(mStockList[position], position)
    }

    fun addItems(stock: MutableList<VegaDispatchLots>) {
        mStockList.clear()
        mStockList.addAll(stock)
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<VegaDispatchLots> {
        return mStockList
    }

    fun removeItems(stock: VegaDispatchLots?) {
        stock.let { mStockList.remove(stock) }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(stock: VegaDispatchLots?, position: Int) {
            stock?.let {
                //itemView.isEnabled = it.isAdded!!
                itemView.tvLotNo.text = it.batchNumber
                itemView.tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                itemView.tvKor.text = it.kor
                itemView.tvOrigin.text = it.region
                if (it.isProgress!!) itemView.llProgressBar.visible() else itemView.llProgressBar.gone()
                itemView.cbLotSelect.isChecked = it.isChecked!!
                itemView.cbLotSelect.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        if (isChecked) {
                            it.isChecked = true
                            onClick(adapterPosition)
                        } else {
                            it.isChecked = false
                            onUnClick(adapterPosition)
                        }
                    }
                }
            }
        }
    }

    fun setStatus(position: Int, progress: Boolean, region: String?, kor: String?) {
        val data = mStockList[position]
        data.isProgress = progress
        data.region = region
        data.kor = kor
        notifyItemChanged(position, data)
    }


}
