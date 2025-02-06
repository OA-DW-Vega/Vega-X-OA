package com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemIncommingMtnOfflineBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil

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
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incomming_mtn_offline, parent, false)*/
        val v = ItemIncommingMtnOfflineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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

    inner class ParamsViewHolder(itemView: ItemIncommingMtnOfflineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(quality: PortMtn?, position: Int) {
            quality?.let {
                val status = it.status ?: 1
                binding.tvMtnItem.text = it.mtnNumber
                binding.tvTruckNumber.text = it.truckNumber
                binding.tvNoOfBales.text = it.baleCount

                val isError = it.isErrorStatus
                if (isError) {
                    binding.llError.gone()
                } else {
                    binding.tvErrorMsg.text = it.message
                    binding.llError.visible()
                }

                binding.ivStatus.setImageResource(PortWHUtil.getSyncStatusIcon(status))
                binding.vStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        PortWHUtil.getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    binding.ivDeleteData.gone()
                    binding.tvViewMtnDetails.isEnabled = false
                    binding.tvViewMtnDetails.text = "".plus(it.message)
                } else {
                    binding.ivDeleteData.visible()
                    binding.tvViewMtnDetails.isEnabled = true
                    binding.tvViewMtnDetails.text =
                        itemView.context.getString(R.string.view_details)
                }
                binding.tvViewMtnDetails.setOnClickListener { onClickView(quality) }
                binding.ivDeleteData.setOnClickListener { onClick(quality) }
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
