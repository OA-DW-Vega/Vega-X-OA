package com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.item_incomming_mtn_offline.view.*

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncommingMtnOfflineAdapter(
    private val onClick: (PortMtn?) -> Unit,
    private val onClickView: (PortMtn?) -> Unit
) :
    RecyclerView.Adapter<PortIncommingMtnOfflineAdapter.ParamsViewHolder>() {

    private val mMtnOfflineList = arrayListOf<PortMtn?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incomming_mtn_offline, parent, false)
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

    fun addItems(quality: MutableList<PortMtn?>) {
        mMtnOfflineList.clear()
        quality.let { mMtnOfflineList.addAll(quality) }
        notifyDataSetChanged()
    }

    fun removeItems(quality: PortMtn?) {
        quality.let { mMtnOfflineList.remove(quality) }
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(quality: PortMtn?, position: Int) {
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

                itemView.ivStatus.setImageResource(PortWHUtil.getSyncStatusIcon(status))
                itemView.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        PortWHUtil.getSyncItemBackgroundColor(status)
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
