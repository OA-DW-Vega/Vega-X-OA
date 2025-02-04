package com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.work.convertKgToMT
import com.olam.warehouse.master.work.convertMtToKg
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.utils.PROCURE
import com.olam.warehouse.vegax.qualitycameroon.utils.SUPPLIER
import com.olam.warehouse.vegax.qualitycameroon.utils.WAREHOUSE
import kotlinx.android.synthetic.main.item_vega_cameroon_mtnr_quality_weigh_bridge_details.view.*

/**
 * Created by Baskaran Kannan on 12/27/2019.
 */
class VegaCameroonMtnrWeighBridgeListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaCameroonMtnrWeighBridgeListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vega_cameroon_mtnr_quality_weigh_bridge_details, parent, false)
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

    inner class WeighBridgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(weighbridge: VegaQualityWBDetails?) {
            weighbridge?.let {

                if (it.weighBridgeType == PROCURE) {
                    itemView.tvdifference.text = SUPPLIER
                    itemView.tvSupplierName.text = it.supplierName
                } else {
//                    itemView.tvdifference.visibility = View.GONE
//                    itemView.tvSupplierName.visibility = View.GONE
//                    itemView.tvdifference.text = WAREHOUSE
                    itemView.tvSupplierName.text = it.materialName
                }
                itemView.tvTruckNo.text = it.vehicleNumber
                itemView.tvWeighBridgeId.text = it.weighBridgeId

                itemView.tvDate.text = it.erdat
                //itemView.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
//                itemView.tvWeight.text = it.netWeight.toString().plus(it.unitsOfMeasure.toString())
                when(it.unitsOfMeasure){
                    "KG" -> {
                        itemView.tvWeight.text = it.netWeight.toString().toDouble()?.formatThreeDigits().plus(it.unitsOfMeasure.toString())
                    }
                    "MT" -> {
                        itemView.tvWeight.text = convertMtToKg(it.netWeight.toString()).toDouble()?.formatThreeDigits().plus(" KG")
                    }
                }
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    itemView.tvDate.text = times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
                }
            }
            itemView.setOnClickListener {
                onClick(weighbridge)
            }
        }

    }
}
