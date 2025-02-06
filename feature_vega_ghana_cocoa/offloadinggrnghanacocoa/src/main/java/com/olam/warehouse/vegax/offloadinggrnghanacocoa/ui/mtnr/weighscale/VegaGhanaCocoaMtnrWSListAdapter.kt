package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.databinding.ItemVegaGhanaCocoaMtnrWeighScaleDetailsBinding
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.PROCURE

class VegaGhanaCocoaMtnrWSListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaGhanaCocoaMtnrWSListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /*val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_ghana_cocoa_mtnr_weigh_scale_details, parent, false)*/
        val v = ItemVegaGhanaCocoaMtnrWeighScaleDetailsBinding.inflate(
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

    inner class WeighBridgeViewHolder(itemView: ItemVegaGhanaCocoaMtnrWeighScaleDetailsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    binding.tvdifference.text = itemView.context.getString(R.string.supplier)
                    binding.tvSupplierName.text = it.supplierName
                } else {
//                    binding.tvdifference.visibility = View.GONE
//                    binding.tvSupplierName.visibility = View.GONE
//                    binding.tvdifference.text = WAREHOUSE
//                    binding.tvSupplierName.text = "-"
                }
                if (it.gateEntry?.isNullOrEmpty() == true) {
                    binding.tvWeight.setTextColor(Color.GREEN)
                } else {
                    binding.tvWeight.setTextColor(Color.BLACK)
                }
                binding.tvWeighBridgeId.text = it.bagCount
                binding.tvProcureType.text = it.materialName
                binding.tvSupplierName.text = it.plant
                if (it.erdat?.isNotEmpty() == true) {
                    val times = it.erdat?.split('(', ')')
                    binding.tvDate.text = times?.get(1).let { it1 ->
                        it1.let { it2 ->
                            it2?.let { it3 ->
                                DateUtils.getUTCDateTime(
                                    it3,
                                    App.getAppContext()
                                )
                            }
                        }
                    }
                    binding.tvDate.text = it.erdat
                }
                /*if (it.purchaseDocNum.isNullOrEmpty()) itemView.context.getString(R.string.spot_purchase) else itemView.context.getString(
                    R.string.fixed_purchase
                )*/

                //binding.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
//                binding.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure)
                binding.tvWeight.text = it.delivery
//                if ("null" != it.erdat) {
//                    val times = it.erdat?.split('(', ')')
//                    binding.tvDate.text = times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
//                }
            }
            itemView.rootView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
