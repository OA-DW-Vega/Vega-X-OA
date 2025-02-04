package com.olam.warehouse.odquality.ui.offline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import kotlinx.android.synthetic.main.item_do_qty_offline.view.*

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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_do_qty_offline, parent, false)
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

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(quality: DOQualityWBDetails?, position: Int) {
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

    fun setSyncStatus(position: Int, syncStatus: Boolean, Status: Int, outputData: String?) {
        val data = mQtyOfflineList[position]
        data?.status = Status
        data?.isErrorStatus = syncStatus
        data?.message = outputData
        notifyItemChanged(position, data)
    }
}
