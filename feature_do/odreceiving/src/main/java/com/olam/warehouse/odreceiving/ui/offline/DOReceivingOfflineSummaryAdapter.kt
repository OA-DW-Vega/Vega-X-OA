package com.olam.warehouse.odreceiving.ui.offline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.dorigin.model.DOReceivingWithLineItems
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.ItemDoReceivingOfflineSummaryBinding
import com.olam.warehouse.odreceiving.utils.RECEIVING_OUTPUT_DATA
import com.olam.warehouse.presentation.enums.Status
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.UIUtils.getSyncItemBackgroundColor
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingOfflineSummaryAdapter(
    private val onClickDelete: (DOReceivingWithLineItems?) -> Unit,
    private val onClickView: (DOReceivingWithLineItems?) -> Unit
) : RecyclerView.Adapter<DOReceivingOfflineSummaryAdapter.ItemViewHolder>() {

    private val mData = arrayListOf<DOReceivingWithLineItems>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        /* val v = LayoutInflater.from(parent.context).inflate(
             R.layout.item_do_receiving_offline_summary,
             parent, false
         )*/
        val binding = ItemDoReceivingOfflineSummaryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(mData[position], holder)
    }

    inner class ItemViewHolder(private val iv: ItemDoReceivingOfflineSummaryBinding) :
        RecyclerView.ViewHolder(iv.root) {
        val binding = iv
        fun bindItems(data: DOReceivingWithLineItems, holder: ItemViewHolder) {
            val receiving = data.receiving
            iv.tvNoOfBags.text = receiving.bagCount
            iv.tvWeight.text = receiving.netWeight.formatThreeDigits().plus(" ").plus(receiving.uom)
            iv.tvProductType.text = receiving.materialName
            iv.tvWbId.text = if (receiving.wbId.isNotEmpty()) receiving.wbId else receiving.tmpWbId
            //iv.viewStatus.isVisible = receiving.isSynced
            when (receiving.status) {
                Status.RECEVING_COMPLETED -> {
                    iv.ivStatus.setImageDrawable(iv.root.context.getDrawable(com.olam.warehouse.presentation.R.drawable.do_icon_completed))
                    iv.viewStatus.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            getSyncItemBackgroundColor(4)
                        )
                    )
                    iv.tvError.gone()
                    iv.tvViewDetails.isEnabled = false
                }
                Status.SYNC_ERROR -> {
                    iv.tvViewDetails.isEnabled = true
                    iv.tvError.visible()
                    iv.tvError.text = receiving.syncStatusMsg
                    iv.ivStatus.setImageDrawable(ContextCompat.getDrawable(holder.iv.root.context, com.olam.warehouse.presentation.R.drawable.ic_do_icon_error))
                    iv.viewStatus.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            getSyncItemBackgroundColor(3)
                        )
                    )
                }
                else -> {
                    iv.ivStatus.setImageDrawable(ContextCompat.getDrawable(holder.iv.root.context, com.olam.warehouse.presentation.R.drawable.icon_no_progresss))
                    iv.viewStatus.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            getSyncItemBackgroundColor(1)
                        )
                    )
                    iv.tvError.gone()
                    iv.tvViewDetails.isEnabled = true
                }
            }
            iv.ivDelete.setOnClickListener {
                onClickDelete(data)
            }
            iv.tvViewDetails.setOnClickListener {
                if (receiving.status != Status.RECEVING_COMPLETED) onClickView(data)
            }
        }

        private fun showDeleteDialog(receiving: DOReceivingWithLineItems, adapterPosition: Int) {
            MaterialDialog(iv.root.context).show {
                message(R.string.delete_msg)
                getMetirialCustomView(
                    this,
                    iv.root.context.getString(com.olam.warehouse.presentation.R.string.confirm),
                    iv.root.context.getString(com.olam.warehouse.presentation.R.string.cancel),
                    {
                        mData.remove(receiving)
                        notifyItemRemoved(adapterPosition)
                    },
                    { dismiss() })
            }
        }
    }


    //////////////////    Utility    /////////////////////////

    fun addItems(receiving: ArrayList<DOReceivingWithLineItems>) {
        mData.addAll(receiving)
        notifyDataSetChanged()
    }

    fun removeItems(item: DOReceivingWithLineItems?) {
        item.let { mData.remove(item) }
        notifyDataSetChanged()
    }

    fun getItems(): ArrayList<DOReceivingWithLineItems> {
        return mData
    }

    fun setSyncStatus(position: Int, syncStatus: Boolean, outputData: Data) {
        val data = mData[position]
        data.receiving.isSynced = syncStatus
        data.receiving.status = if (syncStatus) Status.RECEVING_COMPLETED else Status.SYNC_ERROR
        data.receiving.syncStatusMsg = outputData.getString(RECEIVING_OUTPUT_DATA)
        data.receiving.wbId = outputData.getString(RECEIVING_OUTPUT_DATA).toString()
        notifyItemChanged(position, data)
    }

}
