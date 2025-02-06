package com.olam.warehouse.vegax.qualitynigeria.ui.weighbridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.master.veganigeria.utils.portPlantIdList
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitynigeria.R
import com.olam.warehouse.vegax.qualitynigeria.databinding.ItemVegaNigeriaQualityWeighBridgeDetailsBinding
import com.olam.warehouse.vegax.qualitynigeria.utils.PROCURE
import com.olam.warehouse.vegax.qualitynigeria.utils.STO
import com.olam.warehouse.vegax.qualitynigeria.utils.WAREHOUSE

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaNigeriaQualityWBListAdapter(private val onClick: (VegaQualityWBDetails?) -> Unit) :
    RecyclerView.Adapter<VegaNigeriaQualityWBListAdapter.WeighBridgeViewHolder>() {
    private val mWeighBridgeList = arrayListOf<VegaQualityWBDetails?>()
    private var mSelectedPlantId: String? = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        /* val v =
             LayoutInflater.from(parent.context)
                 .inflate(R.layout.item_vega_nigeria_quality_weigh_bridge_details, parent, false)*/
        val v = ItemVegaNigeriaQualityWeighBridgeDetailsBinding.inflate(
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

    fun addItems(weighbridge: List<VegaQualityWBDetails>, selectedPlantId: String) {
        mWeighBridgeList.clear()
        mWeighBridgeList.addAll(weighbridge)
        mWeighBridgeList.distinctBy { Pair(it?.weighBridgeId, it?.weighBridgeId) }
        mWeighBridgeList.reverse()
        mSelectedPlantId = selectedPlantId
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: ItemVegaNigeriaQualityWeighBridgeDetailsBinding) :
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

                if (it.weighBridgeType == STO) {
                    binding.weighbridgeText.text =
                        itemView.context.getString(R.string.weigh_bridge_id)
                    binding.tvWeighBridgeId.text = it.weighBridgeId
                }  else if (portPlantIdList.contains(getPlantDetails().plantId) || (getCurrentKey().contains("VEGA_NG") && getCurrentKey().contains("CASH"))) {
                    binding.weighbridgeText.text = itemView.context.getString(R.string.challan_id)
                    binding.tvWeighBridgeId.text = it.challan

                    binding.materialText.visibility = View.GONE
                    binding.tvProcureType.visibility = View.GONE
                    binding.tvdifference.visibility = View.GONE
                    binding.tvSupplierName.visibility = View.GONE
                    binding.weightText.visibility = View.GONE
                    binding.tvWeight.visibility = View.GONE

                } else {
                    binding.weighbridgeText.text =
                        itemView.context.getString(R.string.weigh_bridge_id)
                    binding.tvWeighBridgeId.text = it.weighBridgeId
                }
                binding.tvProcureType.text = it.materialName

                binding.tvDate.text = it.erdat
                it.unitsOfMeasure = "MT"
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
