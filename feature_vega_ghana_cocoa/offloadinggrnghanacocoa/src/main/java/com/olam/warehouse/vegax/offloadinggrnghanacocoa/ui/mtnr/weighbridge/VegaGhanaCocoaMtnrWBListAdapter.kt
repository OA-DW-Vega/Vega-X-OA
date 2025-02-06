package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.ItemVegaGhanaCocoaMtnrWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.PROCURE

class VegaGhanaCocoaMtnrWBListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaGhanaCocoaMtnrWBListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /*val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_ghana_cocoa_mtnr_weigh_bridge_details, parent, false)*/
        val v = ItemVegaGhanaCocoaMtnrWeighBridgeDetailsBinding.inflate(
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

    inner class WeighBridgeViewHolder(itemView: ItemVegaGhanaCocoaMtnrWeighBridgeDetailsBinding) :
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
//                    binding.tvdifference.text = WAREHOUSE
//                    binding.tvSupplierName.text = "-"
                }

                if (it.challan?.isNotEmpty() == true) {
                    binding.tvSDWaybillNumberValue.text = it.challan
                }
                if (it.bagCount?.isNotEmpty() == true) {
                    binding.tvQty.text = it.bagCount
                }
                binding.tvWeighBridgeId.text = it.vehicleNumber
                binding.tvProcureType.text = it.materialName
                /*if (it.purchaseDocNum.isNullOrEmpty()) itemView.context.getString(R.string.spot_purchase) else itemView.context.getString(
                    R.string.fixed_purchase
                )*/
                binding.tvDate.text = it.erdat
                //binding.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                binding.tvWeight.text = it.delivery
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
