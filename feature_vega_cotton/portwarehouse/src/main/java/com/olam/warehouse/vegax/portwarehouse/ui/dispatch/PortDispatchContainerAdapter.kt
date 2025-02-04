package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.Container
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.ContainerWithBales
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.enums.ContainerStatus
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.item_disptach_container.view.*


/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class PortDispatchContainerAdapter constructor(val mListener: OnContainerRemoveListener) :
    RecyclerView.Adapter<PortDispatchContainerAdapter.ItemViewHolder>() {

    private var mContainers = arrayListOf<ContainerWithBales?>()
    private var isDirect: Boolean = false

    fun setDispatchType(type: Boolean) {
        isDirect = type
    }

    interface OnContainerRemoveListener {
        fun onDelete(container: Container)
        fun onResumeStuffing(container: Container)
        fun onOTSplit(container: Container, isAdd: Boolean, isSelectedAll: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
                R.layout.item_disptach_container,
                parent, false
        )
        return ItemViewHolder(v, mListener, isDirect)
    }

    override fun getItemCount() = mContainers.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(mContainers[position])
    }

    class ItemViewHolder(
            private val iv: View,
            private val mListener: OnContainerRemoveListener,
            private val dispatchType: Boolean
    ) : RecyclerView.ViewHolder(iv) {
        fun bindItems(data: ContainerWithBales?) {
            data?.container?.let { it ->
                val data = it
                //iv.iv_check_box.visibility = View.GONE
                val status = it.status ?: 0
                iv.tvContainerNo.text = it.containerNumber
                iv.tvNoOfBales.text = it.baleCount.toString()
                iv.tvContainerStatus.text = ContainerStatus.from(status)?.value
                iv.tvWeight.text = it.sumOfBaleWeights ?: "-"
                iv.btnStatus.text = PortWHUtil.getContainerStatus(status)
                iv.btnStatus.isClickable = status != ContainerStatus.InProgress.ordinal
                if (PortWHUtil.canDeleteContainer(status)) iv.ivClose.visible() else iv.ivClose.gone()
                iv.ivContainerStatus.setImageResource(PortWHUtil.getContainerStatusIcon(status))
                iv.ll_parent.setBackgroundColor(
                        ContextCompat.getColor(
                                iv.context,
                                PortWHUtil.getContainerBackgroundColor(status)
                        )
                )
                if (it.isSelected == true) {
                    iv.iv_check_box.visibility = View.VISIBLE
                } else {
                    iv.iv_check_box.visibility = View.GONE
                }
                iv.btnStatus.setOnClickListener { moveToNext(data, dispatchType) }
                iv.ivClose.setOnClickListener { mListener.onDelete(data) }
                iv.ll_parent.setOnLongClickListener { v ->
                    if (iv.iv_check_box.visibility == View.GONE) {
                        iv.iv_check_box.visibility = View.VISIBLE
                        mListener.onOTSplit(data, isAdd = true, isSelectedAll = false)
                    } else {
                        mListener.onOTSplit(data, isAdd = false, isSelectedAll = false)
                        iv.iv_check_box.visibility = View.GONE
                    }
                    true
                }
            }
        }

        private fun moveToNext(data: Container, isDirect: Boolean) {
            when (data.status?.let { it1 -> ContainerStatus.from(it1) }) {
                ContainerStatus.OnHold -> {
                    mListener.onResumeStuffing(data)
                }
                ContainerStatus.Completed -> {
                    val intent = Intent(iv.context, PortDispatchBreakSealActivity::class.java)
                    intent.putExtra(Constants.SEAL_ID, data.sealNumber)
                    intent.putExtra(PortWHUtil.CONTAINER_ID, data.containerNumber)
                    intent.putExtra(PortWHUtil.OT_NUMBER, data.otNumber)
                    intent.putExtra(PortWHUtil.DISPATCH_TYPE, isDirect)
                    iv.context.startActivity(intent)
                }
                else -> return
            }
        }
    }

    /************ Dispatch utilities ****************/

    fun addItem(container: ContainerWithBales?) {
        if (!mContainers.contains(container)) {
            mContainers.add(container)
            mContainers.reverse()
        }
        notifyDataSetChanged()
    }

    fun addItems(containers: List<ContainerWithBales>) {
        clearData()
        mContainers.addAll(containers)
        notifyDataSetChanged()
    }

    fun clearData() {
        mContainers.clear()
        notifyDataSetChanged()
    }

    fun getContainers(): List<Container> {
        val containers = arrayListOf<Container>()
        mContainers.forEach { it?.container?.let { container -> containers.add(container) } }
        return containers
    }

    fun getBaleWeight(): String {
        var totalWeight = 0.0
        var uom = ""
        mContainers.forEach { containerWithBales ->
            uom = containerWithBales?.container?.unitOfMeasure.toString()
            containerWithBales?.bales?.forEach { bale ->
                bale.netWeight?.let {
                    totalWeight += it
                }

            }
        }
        return PortWHUtil.getBaleWeightWithUOM(totalWeight, uom)
    }

    fun getTotalBaleCount(): String {
        var count = 0

        mContainers.forEach {
            count += it?.container?.baleCount ?: 0
        }
        return count.toString()
    }

    fun deleteContainer(data: ContainerWithBales) {
        mContainers.remove(data)
        notifyDataSetChanged()
    }

    fun deleteSplitContainer(data: String) {
        val iterator = mContainers.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when{
                item?.container?.containerNumber?.compareTo(data) == 0->{
                    iterator.remove()
                    if(mContainers.size==0){
                        mListener.onOTSplit(item.container, isAdd = false, isSelectedAll = true)
                    }else{
                        mListener.onOTSplit(item.container, isAdd = false, isSelectedAll = false)
                    }

                }
            }
            notifyDataSetChanged()
        }
    }
    fun notifyUI() {
        notifyDataSetChanged()
    }
}


