package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.ContainerWithBales
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.item_dispatch_complete.view.*

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class PortDispatchCompleteAdapter constructor(val mListener: OnViewContainerListener) :
    RecyclerView.Adapter<PortDispatchCompleteAdapter.ItemViewHolder>() {

    private var mContainers = arrayListOf<ContainerWithBales?>()

    interface OnViewContainerListener {
        fun onViewContainer(container: Container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.item_dispatch_complete,
            parent, false
        )
        return ItemViewHolder(v, mListener)
    }

    override fun getItemCount() = mContainers.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(mContainers[position])
    }

    class ItemViewHolder(
        private val iv: View,
        private val mListener: PortDispatchCompleteAdapter.OnViewContainerListener
    ) : RecyclerView.ViewHolder(iv) {
        fun bindItems(data: ContainerWithBales?) {
            data?.container?.let {
                iv.tvContainerNo.text = it.containerNumber
                iv.tvNoOfBales.text = it.baleCount.toString()
                iv.tvWeight.text = it.sumOfBaleWeights ?: "-"
                iv.tvDispatchGrade.text = it.grade
                iv.btnViewBale.setOnClickListener { view -> mListener.onViewContainer(it) }
            }
        }
    }

    /************ Dispatch utilities ****************/

    fun addItem(container: ContainerWithBales?) {
        mContainers.add(container)
        notifyDataSetChanged()
    }

    fun addItems(containers: List<ContainerWithBales>) {
        clearData()
        mContainers.addAll(containers)
        notifyDataSetChanged()
    }

    private fun clearData() {
        mContainers.clear()
    }
    /* ***********************/
}
