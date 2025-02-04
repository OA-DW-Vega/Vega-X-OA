package com.olam.warehouse.portwarehouse.ui.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.portwarehouse.utils.enums.BaleStatus
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.inventory_row_item.view.*

class InventoryAdapter(private val onClick: (PortBale?) -> Unit) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private val mBales = arrayListOf<PortBale?>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.inventory_row_item, parent, false)
        return InventoryViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mBales.size
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bindItems(mBales[position])
    }

    fun addItems(bale: List<PortBale>) {
        //mBales.clear()
        bale.let { mBales.addAll(bale) }
        mBales.distinctBy { Pair(it?.baleId, it?.baleId) }
        notifyDataSetChanged()
    }


    fun removeItems() {
        mBales.clear()
        notifyDataSetChanged()
    }

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(bale: PortBale?) {

            bale?.let {
                itemView.tvIvenBaleId.text = it.baleId
                itemView.tvIvenBaleMark.text = it.baleMark
                if (!bale.baleMark.equals(null) && bale.baleMark?.isNotEmpty()!!)
                    itemView.tvIvenBaleMark.text = bale.baleMark
                else itemView.tvIvenBaleMark.text = ": Not available"
                itemView.tvIvenBaleGrade.text = it.grade
                itemView.tvIvenBaleType.text = BaleStatus.isGoodOrDamaged(it.typeofBale)
                itemView.tvIvenBaleWeight.text = it.netWeight.toString() + " kg"
            }

            itemView.setOnClickListener {
                onClick(bale)

            }
        }

    }

    fun reverse() {
        mBales.reverse()
        notifyDataSetChanged()
    }
}
