package com.olam.warehouse.ginning.ui.incominglots.incomingmtn.offline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.item_ginning_incoming_mtn_offline.view.*

class GinningIncomingMtnOfflineAdapter(
    private val onClick: (Mtn?) -> Unit,
    private val onClickView: (Mtn?) -> Unit
) :
    RecyclerView.Adapter<GinningIncomingMtnOfflineAdapter.ParamsViewHolder>() {

    private val mMtnOfflineList = arrayListOf<Mtn?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_incoming_mtn_offline, parent, false)
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mMtnOfflineList.size
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        holder.bindItems(mMtnOfflineList[position], position)
    }

    fun addItems(quality: MutableList<Mtn?>) {
        mMtnOfflineList.clear()
        quality.let { mMtnOfflineList.addAll(quality) }
        notifyDataSetChanged()
    }

    fun removeItems(quality: Mtn?) {
        quality.let { mMtnOfflineList.remove(quality) }
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(quality: Mtn?, position: Int) {
            quality?.let {
                val status = it.status ?: 1
                itemView.tvMtnItem.text = it.mtnNumber
                itemView.tvTruckNumber.text = it.truckNumber
                itemView.tvNoOfBales.text = it.baleCount

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
                    itemView.tvViewMtnDetails.isEnabled = false
                    itemView.tvViewMtnDetails.text = "".plus(it.message)
                } else {
                    itemView.ivDeleteData.visible()
                    itemView.tvViewMtnDetails.isEnabled = true
                    itemView.tvViewMtnDetails.text =
                        itemView.context.getString(R.string.view_details)
                }
                itemView.tvViewMtnDetails.setOnClickListener { onClickView(quality) }
                itemView.ivDeleteData.setOnClickListener { onClick(quality) }
            }
        }
    }

    fun setSyncStatus(position: Int, syncStatus: Boolean, Status: Int, outputData: String?) {
        val data = mMtnOfflineList[position]
        data?.status = Status
        data?.isErrorStatus = syncStatus
        data?.message = outputData
        notifyItemChanged(position, data)
    }
}
