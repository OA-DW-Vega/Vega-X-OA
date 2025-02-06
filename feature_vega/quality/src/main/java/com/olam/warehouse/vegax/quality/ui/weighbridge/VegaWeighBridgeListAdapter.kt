package com.olam.warehouse.vegax.quality.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.quality.databinding.ItemVegaQualityWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.quality.utils.PROCURE
import com.olam.warehouse.vegax.quality.utils.SUPPLIER
import com.olam.warehouse.vegax.quality.utils.WAREHOUSE

/**
 * Created by Baskaran Kannan on 12/27/2019.
 */
class VegaWeighBridgeListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaWeighBridgeListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_quality_weigh_bridge_details, parent, false)
        val v = ItemVegaQualityWeighBridgeDetailsBinding.inflate(
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
//        mWeighBridgeList.addAll(weighbridge.filter {
//                data -> !data.qcStatus!!.contains("X")
//        })
        mWeighBridgeList.distinctBy { Pair(it?.weighBridgeId, it?.weighBridgeId) }
        mWeighBridgeList.reverse()
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: ItemVegaQualityWeighBridgeDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    binding.tvdifference.text = SUPPLIER
                    binding.tvSupplierName.text = it.supplierName
                } else {
                    binding.tvdifference.visibility = View.GONE
                    binding.tvSupplierName.visibility = View.GONE
                    binding.tvdifference.text = WAREHOUSE
                    binding.tvSupplierName.text = "-"
                }
                binding.tvTruckNo.text = it.vehicleNumber
                binding.tvWeighBridgeId.text = it.weighBridgeId

                binding.tvDate.text = it.erdat
                //binding.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                binding.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    binding.tvDate.text =
                        times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
                }
            }
            itemView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
