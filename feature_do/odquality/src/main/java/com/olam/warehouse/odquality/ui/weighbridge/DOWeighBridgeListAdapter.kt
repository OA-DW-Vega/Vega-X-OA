package com.olam.warehouse.odquality.ui.weighbridge

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.olam.warehouse.master.dorigin.entity.DOQualityWBDetails
import com.olam.warehouse.odquality.R
import com.olam.warehouse.odquality.data.domain.model.SelectedBags
import com.olam.warehouse.presentation.utils.DateUtils.getUTCDateTime
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.vegax.App
import kotlinx.android.synthetic.main.item_do_quality_weigh_bridge_details.view.*

/**
 * Created by Baskaran Kannan on 12/27/2019.
 */
class DOWeighBridgeListAdapter(private val onClick: (DOQualityWBDetails?, Boolean?) -> Unit) :
    RecyclerView.Adapter<DOWeighBridgeListAdapter.WeighBridgeViewHolder>() {

    private val mWeighBridgeList = arrayListOf<DOQualityWBDetails?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeighBridgeViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_do_quality_weigh_bridge_details, parent, false)
        return WeighBridgeViewHolder(v)
    }

    override fun getItemCount() = mWeighBridgeList.size

    override fun onBindViewHolder(holder: WeighBridgeViewHolder, position: Int) {
        holder.bindItems(mWeighBridgeList[position])
        holder.chooseQrCode.setOnClickListener {
            onClick(mWeighBridgeList[position], true)
        }
    }

    fun addItems(weighbridge: List<DOQualityWBDetails>) {
        mWeighBridgeList.clear()
        /*mWeighBridgeList.addAll(weighbridge.filter { data -> !data.qcStatus!!.contains("X") })
        mWeighBridgeList.distinctBy { Pair(it?.weighBridgeId, it?.weighBridgeId) }*/

        val str = PreferenceHelper.get("selectedBags", "")
        if (str != "") {
            val selectedBags = Gson().fromJson(str, SelectedBags::class.java)
            weighbridge.forEach {
                if (selectedBags.map.containsKey(it.weighBridgeId)) {
                    val selectedQrCodesObj = selectedBags.map[it.weighBridgeId]

                    selectedQrCodesObj?.forEach { qrBag->
                        if (qrBag.isSelected!!) {
                            it.missedQrCodes.add(qrBag.qrCode)
                        }
                    }

//                    it.missedQrCodes = selectedQrCodesObj!!
                }
                mWeighBridgeList.add(it)
            }
        } else {
            mWeighBridgeList.addAll(weighbridge)
        }

        mWeighBridgeList.reverse()
        notifyDataSetChanged()
    }

    inner class WeighBridgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var chooseQrCode: TextView = itemView.findViewById(R.id.chooseQrCode)
        fun bindItems(weighbridge: DOQualityWBDetails?) {
            weighbridge?.let {
                itemView.tvWeighBridgeId.text = it.weighBridgeId
                /*if (it.missedQrCodes?.isNullOrEmpty()) {
                    var spannableString: SpannableString? = null
                    spannableString = SpannableString(App.getAppContext().getString(R.string.choose_qr_code_for_samplings))
                    spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
                    itemView.chooseQrCode.text = spannableString
//                    itemView.chooseQrCode.text = App.getAppContext().getString(R.string.choose_qr_code_for_samplings)
                } else {
                    var spannableString: SpannableString? = null
                    spannableString = SpannableString(it.missedQrCodes.toString())
                    spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
                    itemView.chooseQrCode.text = spannableString
                }*/
                var spannableString: SpannableString? = null
                itemView.chooseQrCode.text = spannableString
                spannableString = if (it.missedQrCodes?.isNullOrEmpty()) {
                    SpannableString(itemView.context.getString(R.string.choose_qr_code_for_samplings))
                } else {
                    SpannableString(it.missedQrCodes.toString())
                }

                spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)

                Log.i("spannableString", spannableString.toString())
                itemView.chooseQrCode.text = spannableString


                itemView.tvLotTxnId.text = it.challan
                itemView.tvGrade.text = if (it.materialName.isNullOrEmpty()) "Not Available" else it.materialName
                itemView.tvWeighBridgeWeight.text = it.netWeight
                itemView.tvWeighBridgeUom.text = it.unitsOfMeasure
                if ("null" != it.erdat) {
                    val times = it.erdat?.split('(', ')')
                    itemView.tvReceivedOn.text = times?.get(1)?.let { it1 -> getUTCDateTime(it1, App.getAppContext()) }
                }
            }
            itemView.setOnClickListener {
                onClick(weighbridge, false)
            }
        }

    }
}
