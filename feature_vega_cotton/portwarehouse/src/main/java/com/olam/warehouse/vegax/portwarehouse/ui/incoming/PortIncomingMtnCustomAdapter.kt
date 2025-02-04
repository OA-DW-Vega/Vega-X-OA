package com.olam.warehouse.portwarehouse.ui.incoming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.MtnNumber
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.my_custom_list.view.*


class PortIncomingMtnCustomAdapter(val itemClickListener: OnItemClickListener,
    private var incomingList: MutableList<MtnNumber>
) : RecyclerView.Adapter<PortIncomingMtnCustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.my_custom_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return incomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(incomingList[position],itemClickListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(mtn: MtnNumber,itemClickListener: OnItemClickListener) {
            itemView.textViewName.text = mtn.baleID
            if (!mtn.isScaned!!)
                itemView.textViewName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            itemView.buttonDelete.setOnClickListener {
                itemClickListener.onItemClicked(mtn,false)
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

