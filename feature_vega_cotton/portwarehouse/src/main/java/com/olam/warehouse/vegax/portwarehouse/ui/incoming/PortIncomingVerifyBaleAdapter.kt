package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.vegax.portwarehouse.databinding.RowVerifyBaleBinding
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus

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
        /*val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_verify_bale, parent, false)*/
        val v = RowVerifyBaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(v)
    }


    override fun getItemCount(): Int {
        return mBales.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val mtnBale = mBales[position]

        holder.bindRow(mtnBale)
    }

    inner class ItemViewHolder(itemView: RowVerifyBaleBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindRow(mtnBale: PortMtnBales) {

            binding.tvBaleId.text = mtnBale.baleId
            binding.tvBaleStatus.text =
                getBaleStatus(mtnBale.baleStatus).plus(" /\n").plus(mtnBale.grade)
            binding.tvDelete.setOnClickListener {
                onClick(mtnBale)
            }

            binding.tvEdit.setOnClickListener {
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
