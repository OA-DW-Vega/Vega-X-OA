package com.olam.warehouse.vegax.qualityindiacoffee.ui.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.qualityindiacoffee.R
import com.olam.warehouse.vegax.qualityindiacoffee.databinding.ItemVegaIndiaCoffeeQualityOfflineBinding

class VegaIndiaCoffeeQualityOfflineAdapter(
    private val onClick: (VegaQualityWBDetails?) -> Unit,
    private val onClickView: (VegaQualityWBDetails?) -> Unit
) :
    RecyclerView.Adapter<VegaIndiaCoffeeQualityOfflineAdapter.ParamsViewHolder>() {

    private val mQtyOfflineList = arrayListOf<VegaQualityWBDetails?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_india_coffee_quality_offline, parent, false)
        val v = ItemVegaIndiaCoffeeQualityOfflineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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

    inner class ParamsViewHolder(itemView: ItemVegaIndiaCoffeeQualityOfflineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(quality: VegaQualityWBDetails?, position: Int) {
            quality?.let {
                val status = it.status ?: 1
                binding.tvGradeItem.text = it.materialName
                binding.tvWeighBridgeNo.text = it.weighBridgeId

                val isError = it.isErrorStatus
                if (isError) {
                    binding.llError.gone()
                } else {
                    binding.tvErrorMsg.text = it.message
                    binding.llError.visible()
                }

                binding.ivStatus.setImageResource(getSyncStatusIcon(status))
                binding.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    binding.ivDeleteData.gone()
                    binding.tvViewWeighBridge.isEnabled = false
                    binding.tvViewWeighBridge.text = "Lot Id \n".plus(it.message)
                } else {
                    binding.ivDeleteData.visible()
                    binding.tvViewWeighBridge.isEnabled = true
                    binding.tvViewWeighBridge.text =
                        itemView.context.getString(R.string.view_details)
                }
                binding.tvViewWeighBridge.setOnClickListener { onClickView(quality) }
                binding.ivDeleteData.setOnClickListener { onClick(quality) }
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
