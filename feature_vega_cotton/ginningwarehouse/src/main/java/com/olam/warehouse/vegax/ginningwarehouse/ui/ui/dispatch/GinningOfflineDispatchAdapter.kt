package com.olam.warehouse.ginning.ui.dispatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.item_ginning_dispatch_offline.view.*

/**
 * Created by Baskaran Kannan on 3/26/2020.
 */
class GinningOfflineDispatchAdapter(
    private val onClick: (VegaCottonGinningDispatchDelivery?) -> Unit,
    private val onClickView: (VegaCottonGinningDispatchDelivery?) -> Unit
) :
    RecyclerView.Adapter<GinningOfflineDispatchAdapter.ParamsViewHolder>() {

    private val mOfflineList = arrayListOf<VegaCottonGinningDispatchDelivery>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_dispatch_offline, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mOfflineList.size
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        holder.bindItems(mOfflineList[position], position)
    }

    fun addItems(deliveryListVegaCotton: ArrayList<VegaCottonGinningDispatchDelivery>) {
        mOfflineList.clear()
        deliveryListVegaCotton.let { mOfflineList.addAll(it) }
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<VegaCottonGinningDispatchDelivery> {
        return mOfflineList
    }

    fun removeItems(deliveryVegaCotton: VegaCottonGinningDispatchDelivery?) {
        deliveryVegaCotton.let { mOfflineList.remove(it) }
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(deliveryVegaCotton: VegaCottonGinningDispatchDelivery?, position: Int) {
            deliveryVegaCotton?.let {
                val status = it.status ?: 1

                itemView.tvDeliveryBales.text = it.baleDTO.size.toString()
                itemView.tvDeliveryWeight.text =
                    it.baleDTO.sumByDouble { it.netWeight ?: 0.0 }.formatTwoDigits().plus(" ")
                        .plus("KG")
                val grades = it.baleDTO.map { it.grade }.distinct()
                itemView.tvDeliveryGrade.text = grades.toString().replace("[", "").replace("]", "")
                itemView.tvDeliveryNo.text = it.deliveryNumber

                val isError = it.isErrorStatus
                if (isError) {
                    itemView.llError.gone()
                } else {
                    itemView.tvErrorMsg.text = it.message
                    itemView.llError.visible()
                }

                itemView.ivStatus.setImageResource(getSyncStatusIcon(status))
                itemView.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    itemView.ivCloseDelivery.gone()
                    itemView.tvViewDelivery.isEnabled = false
                } else {
                    itemView.ivCloseDelivery.visible()
                    itemView.tvViewDelivery.isEnabled = true
                }
                itemView.tvViewDelivery.setOnClickListener { view -> onClickView(it) }
                itemView.ivCloseDelivery.setOnClickListener { view -> onClick(it) }
            }
        }
    }

    fun setSyncStatus(position: Int, syncStatus: Boolean, Status: Int, outputData: String?) {
        val data = mOfflineList[position]
        data.status = Status
        data.isErrorStatus = syncStatus
        data.message = outputData
        notifyItemChanged(position, data)
    }
}
