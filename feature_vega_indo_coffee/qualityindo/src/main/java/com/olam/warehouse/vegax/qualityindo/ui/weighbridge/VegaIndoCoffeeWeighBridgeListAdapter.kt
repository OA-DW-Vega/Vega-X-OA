package com.olam.warehouse.vegax.qualityindo.ui.weighbridge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityindo.R
import com.olam.warehouse.vegax.qualityindo.databinding.ItemIndoCoffeeQualityWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.qualityindo.utils.PROCURE

/**
 * Created by Baskaran Kannan on 4/7/2021.
 */
class VegaIndoCoffeeWeighBridgeListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaIndoCoffeeWeighBridgeListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /*val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_indo_coffee_quality_weigh_bridge_details, parent, false)*/
        val v = ItemIndoCoffeeQualityWeighBridgeDetailsBinding.inflate(
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

    inner class WeighBridgeViewHolder(itemView: ItemIndoCoffeeQualityWeighBridgeDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    binding.tvdifference.text = App.getAppContext().getString(R.string.supplier)
                    binding.tvSupplierName.text = it.supplierName
                } else {
                    //binding.tvdifference.visibility = View.GONE
                    //binding.tvSupplierName.visibility = View.GONE
                    //binding.tvdifference.text = WAREHOUSE
                    binding.tvSupplierName.text = it.materialName
                    // binding.tvSupplierName.text = "-"
                }
                binding.tvTruckNo.text = it.vehicleNumber
                binding.tvWeighBridgeId.text = it.weighBridgeId

                binding.tvDate.text = it.erdat
                binding.tvMaterialValue.text = it.materialName
                //binding.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                binding.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    binding.tvDate.text = times?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                }
            }
            itemView.rootView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}

