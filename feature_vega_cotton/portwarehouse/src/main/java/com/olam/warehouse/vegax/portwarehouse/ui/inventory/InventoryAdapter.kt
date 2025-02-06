package com.olam.warehouse.vegax.portwarehouse.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.vegax.portwarehouse.databinding.InventoryRowItemBinding
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus

class InventoryAdapter(private val onClick: (PortBale?) -> Unit) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    private val mBales = arrayListOf<PortBale?>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.inventory_row_item, parent, false)
        val v = InventoryRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class InventoryViewHolder(itemView: InventoryRowItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(bale: PortBale?) {

            bale?.let {
                binding.tvIvenBaleId.text = it.baleId
                binding.tvIvenBaleMark.text = it.baleMark
                if (!bale.baleMark.equals(null) && bale.baleMark?.isNotEmpty()!!)
                    binding.tvIvenBaleMark.text = bale.baleMark
                else binding.tvIvenBaleMark.text = ": Not available"
                binding.tvIvenBaleGrade.text = it.grade
                binding.tvIvenBaleType.text = BaleStatus.isGoodOrDamaged(it.typeofBale)
                binding.tvIvenBaleWeight.text = it.netWeight.toString() + " kg"
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
