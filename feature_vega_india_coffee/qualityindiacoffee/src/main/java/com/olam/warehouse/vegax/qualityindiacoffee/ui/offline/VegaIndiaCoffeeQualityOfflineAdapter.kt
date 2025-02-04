package com.olam.warehouse.vegax.qualityindiacoffee.ui.offline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityindiacoffee.R
import kotlinx.android.synthetic.main.item_vega_india_coffee_quality_offline.view.*

class VegaIndiaCoffeeQualityOfflineAdapter(
    private val onClick: (VegaQualityWBDetails?) -> Unit,
    private val onClickView: (VegaQualityWBDetails?) -> Unit
) :
    RecyclerView.Adapter<VegaIndiaCoffeeQualityOfflineAdapter.ParamsViewHolder>() {

    private val mQtyOfflineList = arrayListOf<VegaQualityWBDetails?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_vega_india_coffee_quality_offline, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyOfflineList.size
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        holder.bindItems(mQtyOfflineList[position], position)
    }

    fun addItems(quality: MutableList<VegaQualityWBDetails?>) {
        mQtyOfflineList.clear()
        quality.let { mQtyOfflineList.addAll(quality) }
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<VegaQualityWBDetails?> {
        return mQtyOfflineList
    }

    fun removeItems(quality: VegaQualityWBDetails?) {
        quality.let { mQtyOfflineList.remove(quality) }
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(quality: VegaQualityWBDetails?, position: Int) {
            quality?.let {
                val status = it.status ?: 1
                itemView.tvGradeItem.text = it.materialName
                itemView.tvWeighBridgeNo.text = it.weighBridgeId

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
                    itemView.ivDeleteData.gone()
                    itemView.tvViewWeighBridge.isEnabled = false
                    itemView.tvViewWeighBridge.text = "Lot Id \n".plus(it.message)
                } else {
                    itemView.ivDeleteData.visible()
                    itemView.tvViewWeighBridge.isEnabled = true
                    itemView.tvViewWeighBridge.text = itemView.context.getString(R.string.view_details)
                }
                itemView.tvViewWeighBridge.setOnClickListener { onClickView(quality) }
                itemView.ivDeleteData.setOnClickListener { onClick(quality) }
            }
        }
    }


    fun setSyncStatus(position: Int, syncStatus: Boolean, status: Int, outputData: String?) {
        val data = mQtyOfflineList[position]
        data?.status = status
        data?.isErrorStatus = syncStatus
        data?.message = outputData
        notifyItemChanged(position, data)
    }
}
