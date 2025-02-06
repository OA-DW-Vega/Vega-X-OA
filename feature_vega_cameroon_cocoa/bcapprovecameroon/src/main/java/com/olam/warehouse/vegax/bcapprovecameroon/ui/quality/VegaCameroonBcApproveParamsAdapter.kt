package com.olam.warehouse.vegax.bcapprovecameroon.ui.quality

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.vegax.bcapprovecameroon.databinding.ItemVegaBcApproveCameroonParamsBinding
import com.olam.warehouse.vegax.bcapprovecameroon.utils.getColor


class VegaCameroonBcApproveParamsAdapter(private val onClick: (List<VegaQualityParamsWithQualitative?>) -> Unit) :
    RecyclerView.Adapter<VegaCameroonBcApproveParamsAdapter.ParamsViewHolder>() {
    private var mQtyParamsListNonModify = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var mQtyParamsList = arrayListOf<VegaQualityParamsWithQualitative?>()
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamsViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vega_bc_approve_cameroon_params, parent, false)
        val v = ItemVegaBcApproveCameroonParamsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParamsViewHolder(v)
    }

    override fun getItemViewType(position: Int) = position

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount(): Int {
        return mQtyParamsList.size
    }

    override fun onBindViewHolder(holder: ParamsViewHolder, position: Int) {
        try {
            holder.bindItems(mQtyParamsList[position], position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addItems(
        qualityParameter: List<VegaQualityParamsWithQualitative>,
        tarWeight: String?,
        netWeight: String?,
        challanNo: String?
    ) {
        mQtyParamsList.clear()
        this.tarWeight = tarWeight
        this.netWeight = netWeight
        this.challanNo = challanNo
        qualityParameter.let {
            mQtyParamsListNonModify.addAll(qualityParameter)
            mQtyParamsList.addAll(qualityParameter)
        }

        notifyDataSetChanged()
    }


    fun getItems(): ArrayList<VegaQualityParamsWithQualitative?> {
        return mQtyParamsList
    }

    fun updateMissedPos(
        missedPos: MutableList<Int>,
        data: ArrayList<VegaQualityParamsWithQualitative?>
    ) {
        mQtyParamsList = data
        notifyDataSetChanged()
    }

    inner class ParamsViewHolder(itemView: ItemVegaBcApproveCameroonParamsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        var i = 0
        fun bindItems(qualityParameter: VegaQualityParamsWithQualitative?, position: Int) {
            try {
                qualityParameter?.qualityParameter?.let { it1 ->
                    i++
                    if (i % 2 == 0) {
                        this.itemView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                    } else {
                        this.itemView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                    }
                    when(it1.descrChar){
                        "Bean Smoky" -> {
                            binding.tvQualityNameApprove.text = it1.descrChar
                            if (it1.qualityParameterValue == "IR-AB/PR 003")
                                binding.tvUnitApprove.text = "Absence"
                            else if (it1.qualityParameterValue == "IR-AB/PR 004")
                                binding.tvUnitApprove.text = "Presence"
                        }
                        "Classment" -> {
                            binding.tvQualityNameApprove.text = it1.descrChar
                            if (it1.qualityParameterValue == "CLASCOCO 0001")
                                binding.tvUnitApprove.text = "GF-G1"
                            else if (it1.qualityParameterValue == "CLASCOCO 0002")
                                binding.tvUnitApprove.text = "FF-G2"
                            else if (it1.qualityParameterValue == "CLASCOCO 0003")
                                binding.tvUnitApprove.text = "FAG-H/S"
                        }
                        else -> {
                            binding.tvQualityNameApprove.text = it1.descrChar
                            binding.tvUnitApprove.text = it1.qualityParameterValue
                        }
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}
