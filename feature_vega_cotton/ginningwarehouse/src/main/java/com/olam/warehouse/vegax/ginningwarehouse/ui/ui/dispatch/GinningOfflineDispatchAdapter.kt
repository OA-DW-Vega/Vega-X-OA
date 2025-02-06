package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.VegaCottonGinningDispatchDelivery
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.UIUtils.getSyncStatusIcon
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGinningDispatchOfflineBinding

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
        /*val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ginning_dispatch_offline, parent, false)*/
        val v = ItemGinningDispatchOfflineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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

    inner class ParamsViewHolder(itemView: ItemGinningDispatchOfflineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(deliveryVegaCotton: VegaCottonGinningDispatchDelivery?, position: Int) {
            deliveryVegaCotton?.let {
                val status = it.status ?: 1

                binding.tvDeliveryBales.text = it.baleDTO.size.toString()
                binding.tvDeliveryWeight.text =
                    it.baleDTO.sumByDouble { it.netWeight ?: 0.0 }.formatTwoDigits().plus(" ")
                        .plus("KG")
                val grades = it.baleDTO.map { it.grade }.distinct()
                binding.tvDeliveryGrade.text = grades.toString().replace("[", "").replace("]", "")
                binding.tvDeliveryNo.text = it.deliveryNumber

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
                        itemView.rootView.context,
                        getSyncItemBackgroundColor(status)
                    )
                )
                if (status == 4) {
                    binding.ivCloseDelivery.gone()
                    binding.tvViewDelivery.isEnabled = false
                } else {
                    binding.ivCloseDelivery.visible()
                    binding.tvViewDelivery.isEnabled = true
                }
                binding.tvViewDelivery.setOnClickListener { view -> onClickView(it) }
                binding.ivCloseDelivery.setOnClickListener { view -> onClick(it) }
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
