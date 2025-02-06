package com.olam.warehouse.odquality.ui.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.databinding.ItemDoQtyOfflineBinding
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible

/**
 * Created by Baskaran Kannan on 12/30/2019.
 */
class DOQualityOfflineAdapter(
    private val onClick: (DOQualityWBDetails?) -> Unit,
    private val onClickView: (DOQualityWBDetails?) -> Unit
) :
    RecyclerView.Adapter<DOQualityOfflineAdapter.ParamsViewHolder>() {

    private val mQtyOfflineList = arrayListOf<DOQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        // val v = LayoutInflater.from(parent.context).inflate(R.layout.item_do_qty_offline, parent, false)
        val binding =
            ItemDoQtyOfflineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParamsViewHolder(binding)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyOfflineList.size
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        holder.bindItems(mQtyOfflineList[position], position, holder)
    }

    fun addItems(quality: MutableList<DOQualityWBDetails?>) {
        mQtyOfflineList.clear()
        quality.let { mQtyOfflineList.addAll(quality) }
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<DOQualityWBDetails?> {
        return mQtyOfflineList
    }

    fun removeItems(quality: DOQualityWBDetails?) {
        quality.let { mQtyOfflineList.remove(quality) }
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: ItemDoQtyOfflineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(quality: DOQualityWBDetails?, position: Int, holder: ParamsViewHolder) {
            quality?.let {
                val status = it.status ?: 1
                holder.binding.tvGradeItem.text = it.materialName
                holder.binding.tvWeighBridgeNo.text = it.weighBridgeId

                val isError = it.isErrorStatus
                if (isError) {
                    holder.binding.llError.gone()
                } else {
                    holder.binding.tvErrorMsg.text = it.message
                    holder.binding.llError.visible()
                }

                holder.binding.ivStatus.setImageResource(getSyncStatusIcon(status))
                holder.binding.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    holder.binding.ivDeleteData.gone()
                    holder.binding.tvViewWeighBridge.isEnabled = false
                    holder.binding.tvViewWeighBridge.text = "Lot Id \n".plus(it.message)
                } else {
                    holder.binding.ivDeleteData.visible()
                    holder.binding.tvViewWeighBridge.isEnabled = true
                    holder.binding.tvViewWeighBridge.text =
                        itemView.context.getString(R.string.view_details)
                }
                holder.binding.tvViewWeighBridge.setOnClickListener { onClickView(quality) }
                holder.binding.ivDeleteData.setOnClickListener { onClick(quality) }
            }
        }
    }

    fun setSyncStatus(position: Int, syncStatus: Boolean, Status: Int, outputData: String?) {
        val data = mQtyOfflineList[position]
        data?.status = Status
        data?.isErrorStatus = syncStatus
        data?.message = outputData
        notifyItemChanged(position, data)
    }
}
