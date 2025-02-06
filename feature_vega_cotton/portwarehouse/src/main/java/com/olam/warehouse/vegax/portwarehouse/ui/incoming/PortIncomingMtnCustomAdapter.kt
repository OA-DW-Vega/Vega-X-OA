package com.olam.warehouse.vegax.portwarehouse.ui.incoming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnNumber
import com.olam.warehouse.vegax.portwarehouse.databinding.MyCustomListBinding


class PortIncomingMtnCustomAdapter(
    val itemClickListener: OnItemClickListener,
    private var incomingList: MutableList<MtnNumber>
) : RecyclerView.Adapter<PortIncomingMtnCustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //val v = LayoutInflater.from(parent.context).inflate(R.layout.my_custom_list, parent, false)
        val v = MyCustomListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position], itemClickListener)
    }

    inner class ViewHolder(itemView: MyCustomListBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun bindItems(mtn: MtnNumber, itemClickListener: OnItemClickListener) {
            binding.textViewName.text = mtn.baleID
            if (!mtn.isScaned!!)
                binding.textViewName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding.buttonDelete.setOnClickListener {
                itemClickListener.onItemClicked(mtn, false)
            }
        }
    }


    fun updateData(incomingList: MutableList<MtnNumber>) {
        this.incomingList = incomingList
        notifyDataSetChanged()
    }
    fun removedata(mtn: MtnNumber)
    {
        incomingList.remove(mtn)
        notifyDataSetChanged()
        itemClickListener.onItemClicked(mtn,true)
    }

}
interface OnItemClickListener{
    fun onItemClicked(user: MtnNumber,isdeleted:Boolean)
}

