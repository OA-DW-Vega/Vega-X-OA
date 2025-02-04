package com.olam.warehouse.ginning.ui.incominglots.incomingmtn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.ginning.utils.enums.BaleStatus
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.row_ginning_verify_bale.view.*

class GinningIncomingVerifyBaleAdapter(
    private val onClick: (MtnBales) -> Unit,
    private val onClickEdit: (MtnBales) -> Unit
) :
    RecyclerView.Adapter<GinningIncomingVerifyBaleAdapter.ItemViewHolder>() {

    private var mBales: List<MtnBales> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_ginning_verify_bale, parent, false)

        return ItemViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mBales.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val mtnBale = mBales[position]

        holder.bindRow(mtnBale)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindRow(mtnBale: MtnBales) {

            itemView.tvBaleId.text = mtnBale.baleId
            itemView.tvBaleStatus.text =
                getBaleStatus(mtnBale.baleStatus).plus(" /\n").plus(mtnBale.grade)
            itemView.tvDelete.setOnClickListener {
                onClick(mtnBale)
            }

            itemView.tvEdit.setOnClickListener {
                onClickEdit(mtnBale)
                /* val intent = Intent(itemView.context, IncomingBaleStatusActivity::class.java)
                 intent.putExtra(BALE, mtnBale)
                 itemView.context.startActivity(intent)*/
            }
        }
    }

    fun updateData(verifiedBales: List<MtnBales>) {
        this.mBales = verifiedBales
        notifyDataSetChanged()
    }

    private fun getBaleStatus(status: String?): String? {

        status?.let {
            val baleStatus = BaleStatus.from(status)
            return when (baleStatus) {
                BaleStatus.Good -> "Good"
                BaleStatus.CottonDirty, BaleStatus.CottonClean, BaleStatus.TieDamage, BaleStatus.WetBale, BaleStatus.NoBaleTag -> "Damaged"
                else -> ""
            }
        }
        return ""
    }
}
