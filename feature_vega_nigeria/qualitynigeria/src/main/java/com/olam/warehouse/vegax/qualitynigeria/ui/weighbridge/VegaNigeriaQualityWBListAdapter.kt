package com.olam.warehouse.vegax.qualitynigeria.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.utils.PROCURE
import com.olam.warehouse.vegax.qualitynigeria.utils.STO
import com.olam.warehouse.vegax.qualitynigeria.utils.WAREHOUSE
import kotlinx.android.synthetic.main.item_vega_nigeria_quality_weigh_bridge_details.view.*

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaQualityWBListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaNigeriaQualityWBListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()
    private var mSelectedPlantId: String? = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_nigeria_quality_weigh_bridge_details, parent, false)
        return WeighBridgeViewHolder(v)
    }

    override fun getItemCount() = mWeighBridgeList.size

    override fun onBindViewHolder(holder: WeighBridgeViewHolder, position: Int) {
        holder.bindItems(mWeighBridgeList[position])
    }

    fun addItems(weighbridge: List<VegaQualityWBDetails>, selectedPlantId: String) {
        mWeighBridgeList.clear()
        mWeighBridgeList.addAll(weighbridge)
        mWeighBridgeList.distinctBy { Pair(it?.weighBridgeId, it?.weighBridgeId) }
        mWeighBridgeList.reverse()
        mSelectedPlantId = selectedPlantId
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    itemView.tvdifference.text = itemView.context.getString(R.string.supplier)
                    itemView.tvSupplierName.text = it.supplierName
                } else {
                    itemView.tvdifference.visibility = View.GONE
                    itemView.tvSupplierName.visibility = View.GONE
                    itemView.tvdifference.text = WAREHOUSE
                    itemView.tvSupplierName.text = "-"
                }

                if (it.weighBridgeType == STO) {
                    itemView.weighbridgeText.text =
                        itemView.context.getString(R.string.weigh_bridge_id)
                    itemView.tvWeighBridgeId.text = it.weighBridgeId
                } else if (PreferenceHelper.get(Constants.WERKS, "")
                        .equals("6154") || PreferenceHelper.get(Constants.WERKS, "").equals("6155")
                ) {
                    itemView.weighbridgeText.text = itemView.context.getString(R.string.challan_id)
                    itemView.tvWeighBridgeId.text = it.challan

                    itemView.materialText.visibility = View.GONE
                    itemView.tvProcureType.visibility = View.GONE
                    itemView.tvdifference.visibility = View.GONE
                    itemView.tvSupplierName.visibility = View.GONE
                    itemView.weightText.visibility = View.GONE
                    itemView.tvWeight.visibility = View.GONE

                } else {
                    itemView.weighbridgeText.text =
                        itemView.context.getString(R.string.weigh_bridge_id)
                    itemView.tvWeighBridgeId.text = it.weighBridgeId
                }
                itemView.tvProcureType.text = it.materialName
                /*if (it.purchaseDocNum.isNullOrEmpty()) itemView.context.getString(R.string.spot_purchase) else itemView.context.getString(
                    R.string.fixed_purchase
                )*/
                itemView.tvDate.text = it.erdat
                //itemView.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                itemView.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    itemView.tvDate.text =
                        times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
                }
            }
            itemView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
