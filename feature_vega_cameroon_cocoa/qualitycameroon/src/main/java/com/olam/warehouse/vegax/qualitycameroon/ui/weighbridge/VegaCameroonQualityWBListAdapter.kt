package com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTimeCameroonGivenDate
import com.olam.warehouse.vegax.qualitycameroon.databinding.ItemVegaCameroonQualityWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.qualitycameroon.utils.PROCURE
import com.olam.warehouse.vegax.qualitycameroon.utils.WAREHOUSE

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaCameroonQualityWBListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaCameroonQualityWBListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /* val v =
             LayoutInflater.from(parent.context)
                 .inflate(R.layout.item_vega_cameroon_quality_weigh_bridge_details, parent, false)*/
        val v = ItemVegaCameroonQualityWeighBridgeDetailsBinding.inflate(
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
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: ItemVegaCameroonQualityWeighBridgeDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {
                if (it.weighBridgeType == PROCURE) {
                    binding.tvdifference.text = "GRN Status"
                    if (!it.netWeight.equals("0.000"))
                        binding.tvSupplierName.text = "Complete"
                    else
                        binding.tvSupplierName.text = "Pending"

                } else {
                    binding.tvdifference.visibility = View.GONE
                    binding.tvSupplierName.visibility = View.GONE
                    binding.tvdifference.text = WAREHOUSE
                    binding.tvSupplierName.text = "-"
                }
                binding.tvWeighBridgeId.text = it.challan

                if (!it.erdat.isNullOrEmpty()) {
                    binding.tvDate.text = getUTCDateTimeCameroonGivenDate(it.erdat!!)
                    /*val date = it.erdat?.split('(', ')')
                    binding.tvDate.text = date?.get(1)?.let { it1 -> getUTCDateTimeCameroonGivenDate(it1) }*/
                }

                /*if(!it.erdat.isNullOrEmpty()){
                    val times = it.erdat?.split('(', ')')
                    binding.tvDate.text = times?.get(1)?.let { it1 -> getUTCDateTimeCameroonGivenDate(it1) }
                }*/
                binding.tvProcureType.text = it.materialName
                binding.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())

            }
            itemView.rootView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
