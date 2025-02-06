package com.olam.warehouse.vegax.qualityecuador.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityecuador.R
import com.olam.warehouse.vegax.qualityecuador.databinding.ItemVegaEcuadorQualityWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.qualityecuador.utils.PROCURE
import com.olam.warehouse.vegax.qualityecuador.utils.WAREHOUSE

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaEcuadorQualityWBListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaEcuadorQualityWBListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /* val v =
             LayoutInflater.from(parent.context)
                 .inflate(R.layout.item_vega_ecuador_quality_weigh_bridge_details, parent, false)*/
        val v = ItemVegaEcuadorQualityWeighBridgeDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeighBridgeViewHolder(v)
    }

    override fun getItemCount() = mWeighBridgeList.size

    override fun onBindViewHolder(holder: WeighBridgeViewHolder, position: Int) {
        holder.bindItems(mWeighBridgeList[position])
    }

    fun addItems(weighbridge: List<VegaQualityWBDetails>) {
        mWeighBridgeList.clear()
        mWeighBridgeList.addAll(weighbridge)
        mWeighBridgeList.distinctBy { Pair(it?.weighBridgeId, it?.weighBridgeId) }
        mWeighBridgeList.reverse()
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: ItemVegaEcuadorQualityWeighBridgeDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    binding.tvdifference.text = itemView.context.getString(R.string.supplier)
                    binding.tvSupplierName.text = it.supplierName
                } else {
                    binding.tvdifference.visibility = View.GONE
                    binding.tvSupplierName.visibility = View.GONE
                    binding.tvdifference.text = WAREHOUSE
                    binding.tvSupplierName.text = "-"
                }
                binding.tvWeighBridgeId.text = it.weighBridgeId
                binding.tvProcureType.text = it.materialName
                /*if (it.purchaseDocNum.isNullOrEmpty()) itemView.context.getString(R.string.spot_purchase) else itemView.context.getString(
                    R.string.fixed_purchase
                )*/
                binding.tvDate.text = it.erdat

                if(it.sourceLotId?.isNotEmpty() == true) {
                    binding.tvSourceLotIdLabel.visible()
                    binding.tvSourceLotId.text = it.sourceLotId
                } else {
                    binding.tvSourceLotIdLabel.gone()
                }
                //binding.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                binding.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    binding.tvDate.text =
                        times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
                }
            }
            itemView.rootView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
