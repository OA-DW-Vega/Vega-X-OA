package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.ginningwarehouse.databinding.RowGinningVerifyBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.enums.BaleStatus

class GinningIncomingVerifyBaleAdapter(
    private val onClick: (MtnBales) -> Unit,
    private val onClickEdit: (MtnBales) -> Unit
) :
    RecyclerView.Adapter<GinningIncomingVerifyBaleAdapter.ItemViewHolder>() {

    private var mBales: List<MtnBales> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        /* val view = LayoutInflater.from(parent.context)
             .inflate(R.layout.row_ginning_verify_bale, parent, false)*/
        val view =
            RowGinningVerifyBaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mBales.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val mtnBale = mBales[position]

        holder.bindRow(mtnBale)
    }

    inner class ItemViewHolder(itemView: RowGinningVerifyBaleBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindRow(mtnBale: MtnBales) {

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
