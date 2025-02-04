package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.portwarehouse.utils.enums.BaleStatus
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.row_verify_bale.view.*

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncomingVerifyBaleAdapter(
    private val onClick: (PortMtnBales) -> Unit,
    private val onClickEdit: (PortMtnBales) -> Unit
) :
    RecyclerView.Adapter<PortIncomingVerifyBaleAdapter.ItemViewHolder>() {

    private var mBales: List<PortMtnBales> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_verify_bale, parent, false)

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

        fun bindRow(mtnBale: PortMtnBales) {

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

    fun updateData(verifiedBales: List<PortMtnBales>) {
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
