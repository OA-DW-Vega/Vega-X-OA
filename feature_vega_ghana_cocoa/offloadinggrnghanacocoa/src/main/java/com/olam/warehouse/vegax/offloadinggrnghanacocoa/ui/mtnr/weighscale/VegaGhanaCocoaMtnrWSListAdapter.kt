package com.olam.warehouse.vegax.offloadinggrnghanacocoa.ui.mtnr.weighscale

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.R
import com.olam.warehouse.vegax.offloadinggrnghanacocoa.utils.PROCURE
import kotlinx.android.synthetic.main.item_vega_ghana_cocoa_mtnr_weigh_bridge_details.view.*

class VegaGhanaCocoaMtnrWSListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaGhanaCocoaMtnrWSListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_ghana_cocoa_mtnr_weigh_scale_details, parent, false)
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

    inner class WeighBridgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    itemView.tvdifference.text = itemView.context.getString(R.string.supplier)
                    itemView.tvSupplierName.text = it.supplierName
                } else {
//                    itemView.tvdifference.visibility = View.GONE
//                    itemView.tvSupplierName.visibility = View.GONE
//                    itemView.tvdifference.text = WAREHOUSE
//                    itemView.tvSupplierName.text = "-"
                }
                if (it.gateEntry?.isNullOrEmpty() == true) {
                    itemView.tvWeight.setTextColor(Color.GREEN)
                } else {
                    itemView.tvWeight.setTextColor(Color.BLACK)
                }
                itemView.tvWeighBridgeId.text = it.vehicleNumber
                itemView.tvProcureType.text = it.materialName
                itemView.tvSupplierName.text = it.plant
                if (it.erdat?.isNotEmpty() == true) {
                    val times = it.erdat?.split('(', ')')
                    itemView.tvDate.text = times?.get(1).let { it1 ->
                        it1.let { it2 ->
                            it2?.let { it3 ->
                                DateUtils.getUTCDateTime(
                                    it3,
                                    App.getAppContext()
                                )
                            }
                        }
                    }
                    itemView.tvDate.text = it.erdat
                }
                /*if (it.purchaseDocNum.isNullOrEmpty()) itemView.context.getString(R.string.spot_purchase) else itemView.context.getString(
                    R.string.fixed_purchase
                )*/

                //itemView.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
//                itemView.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure)
                itemView.tvWeight.text = it.delivery
//                if ("null" != it.erdat) {
//                    val times = it.erdat?.split('(', ')')
//                    itemView.tvDate.text = times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
//                }
            }
            itemView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
